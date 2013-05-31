package com.nhn.pinpoint.web.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.web.calltree.span.SpanAlign;
import com.nhn.pinpoint.web.calltree.span.SpanAligner2;
import com.nhn.pinpoint.web.dao.AgentInfoDao;
import com.nhn.pinpoint.web.dao.ApiMetaDataDao;
import com.nhn.pinpoint.web.dao.SqlMetaDataDao;
import com.nhn.pinpoint.web.dao.TraceDao;
import com.nhn.pinpoint.web.vo.TraceId;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.common.bo.AnnotationBo;
import com.nhn.pinpoint.common.bo.ApiMetaDataBo;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SqlMetaDataBo;
import com.nhn.pinpoint.common.mapping.ApiMappingTable;
import com.nhn.pinpoint.common.mapping.ApiUtils;
import com.nhn.pinpoint.common.mapping.MethodMapping;
import com.nhn.pinpoint.common.util.OutputParameterParser;
import com.nhn.pinpoint.common.util.SqlParser;

/**
 *
 */
@Service
public class SpanServiceImpl implements SpanService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TraceDao traceDao;

	@Autowired
	private SqlMetaDataDao sqlMetaDataDao;

	@Autowired
	private ApiMetaDataDao apiMetaDataDao;

	@Autowired
	private AgentInfoDao agentInfoDao;

	private SqlParser sqlParser = new SqlParser();
	private OutputParameterParser outputParameterParser = new OutputParameterParser();

	@Override
	public List<SpanAlign> selectSpan(TraceId traceId) {

		List<SpanBo> spans = traceDao.selectSpanAndAnnotation(traceId);
		if (spans == null || spans.isEmpty()) {
			return Collections.emptyList();
		}

		List<SpanAlign> order = order(spans);
		transitionApiId(order);
		transitionDynamicApiId(order);
		transitionSqlId(order);
		// TODO root span not found시 row data라도 보여줘야 됨.

		return order;
	}

	private void transitionAnnotation(List<SpanAlign> spans, AnnotationReplacementCallback annotationReplacementCallback) {
		for (SpanAlign spanAlign : spans) {
			List<AnnotationBo> annotationBoList;
			if (spanAlign.isSpan()) {
				annotationBoList = spanAlign.getSpanBo().getAnnotationBoList();
				annotationReplacementCallback.replacement(spanAlign, annotationBoList);
			} else {
				annotationBoList = spanAlign.getSpanEventBo().getAnnotationBoList();
				annotationReplacementCallback.replacement(spanAlign, annotationBoList);
			}
		}
	}

	private void transitionSqlId(final List<SpanAlign> spans) {
		this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
			@Override
			public void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList) {
				AnnotationBo sqlIdAnnotation = findAnnotation(annotationBoList, AnnotationKey.SQL_ID.getCode());
				if (sqlIdAnnotation == null) {
					return;
				}

				AgentInfoBo agentInfoBo = null;
				try {
					agentInfoBo = findAgentInfoBoBeforeStartTime(spanAlign);
					logger.info("{} Agent StartTime found:{}", agentInfoBo.getAgentId(), agentInfoBo);
				} catch (AgentIdNotFoundException ex) {
					AnnotationBo agentInfoNotFound = new AnnotationBo();
					agentInfoNotFound.setKey(AnnotationKey.SQL.getCode());
					agentInfoNotFound.setValue("SQL-ID not found. Cause:agentInfo not found. agentId:" + ex.getAgentId() + " startTime:" + ex.getStartTime());
					annotationBoList.add(agentInfoNotFound);
					return;
				}

				// TODO 일단 시간까지 조회는 하지 말고 하자.
				// 미리 sqlMetaDataList를 indentifier로 필터치는 로직이 더 좋을것으로 생각됨.
				int hashCode = (Integer) sqlIdAnnotation.getValue();
				List<SqlMetaDataBo> sqlMetaDataList = sqlMetaDataDao.getSqlMetaData(agentInfoBo.getAgentId(), hashCode, agentInfoBo.getTimestamp());
				int size = sqlMetaDataList.size();
				if (size == 0) {
					AnnotationBo api = new AnnotationBo();
					api.setKey(AnnotationKey.SQL.getCode());
					api.setValue("SQL-ID not found hashCode:" + hashCode);
					annotationBoList.add(api);
				} else if (size == 1) {
					AnnotationBo sqlParamAnnotationBo = findAnnotation(annotationBoList, AnnotationKey.SQL_PARAM.getCode());
					final SqlMetaDataBo sqlMetaDataBo = sqlMetaDataList.get(0);
					if (sqlParamAnnotationBo == null) {
						AnnotationBo sqlMeta = new AnnotationBo();
						sqlMeta.setKey(AnnotationKey.SQL_METADATA.getCode());
						sqlMeta.setValue(sqlMetaDataBo.getSql());
						annotationBoList.add(sqlMeta);

//						AnnotationBo checkFail = checkIdentifier(spanAlign, sqlMetaDataBo);
//						if (checkFail != null) {
//							// 실패
//							annotationBoList.add(checkFail);
//							return;
//						}

						AnnotationBo sql = new AnnotationBo();
						sql.setKey(AnnotationKey.SQL.getCode());
						sql.setValue(sqlMetaDataBo.getSql());
						annotationBoList.add(sql);
					} else {
						logger.debug("sqlMetaDataBo:{}", sqlMetaDataBo);
						String outputParams = (String) sqlParamAnnotationBo.getValue();
						List<String> parsedOutputParams = outputParameterParser.parseOutputParameter(outputParams);
						logger.debug("outputPrams:{}, parsedOutputPrams:{}", outputParams, parsedOutputParams);
						String originalSql = sqlParser.combineOutputParams(sqlMetaDataBo.getSql(), parsedOutputParams);
						logger.debug("outputPrams{}, originalSql:{}", outputParams, originalSql);

						AnnotationBo sqlMeta = new AnnotationBo();
						sqlMeta.setKey(AnnotationKey.SQL_METADATA.getCode());
						sqlMeta.setValue(sqlMetaDataBo.getSql());
						annotationBoList.add(sqlMeta);


						AnnotationBo sql = new AnnotationBo();
						sql.setKey(AnnotationKey.SQL.getCode());
						sql.setValue(originalSql);
						annotationBoList.add(sql);

					}
				} else {
					// TODO 보완해야됨.
					AnnotationBo api = new AnnotationBo();
					api.setKey(AnnotationKey.SQL.getCode());
					api.setValue(collisionSqlHashCodeMessage(hashCode, sqlMetaDataList));
					annotationBoList.add(api);
				}

			}

		});
	}

	private AnnotationBo findAnnotation(List<AnnotationBo> annotationBoList, int key) {
		for (AnnotationBo annotationBo : annotationBoList) {
			if (key == annotationBo.getKey()) {
				return annotationBo;
			}
		}
		return null;
	}

	private String collisionSqlHashCodeMessage(int hashCode, List<SqlMetaDataBo> sqlMetaDataList) {
		// TODO 이거 체크하는 테스트를 따로 만들어야 될듯 하다. 왠간하면 확율상 hashCode 충돌 케이스를 쉽게 만들수 없음.
		StringBuilder sb = new StringBuilder(64);
		sb.append("Collision Sql hashCode:");
		sb.append(hashCode);
		sb.append('\n');
		for (int i = 0; i < sqlMetaDataList.size(); i++) {
			if (i != 0) {
				sb.append("or\n");
			}
			SqlMetaDataBo sqlMetaDataBo = sqlMetaDataList.get(i);
			sb.append(sqlMetaDataBo.getSql());
		}
		return sb.toString();
	}

	private String getAgentId(SpanAlign spanAlign) {
		if (spanAlign.isSpan()) {
			return spanAlign.getSpanBo().getAgentId();
		} else {
			return spanAlign.getSpanEventBo().getAgentId();
		}
	}

	private void transitionDynamicApiId(List<SpanAlign> spans) {
		this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
			@Override
			public void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList) {
				AnnotationBo apiIdAnnotation = findAnnotation(annotationBoList, AnnotationKey.API_DID.getCode());
				if (apiIdAnnotation == null) {
					return;
				}

				AgentInfoBo agentInfoBo = null;
				try {
					agentInfoBo = findAgentInfoBoBeforeStartTime(spanAlign);
					logger.info("{} Agent StartTime found:{}", agentInfoBo.getAgentId(), agentInfoBo);
				} catch (AgentIdNotFoundException ex) {
					AnnotationBo agentInfoNotFound = new AnnotationBo();
					agentInfoNotFound.setKey(AnnotationKey.ERROR_API_METADATA_AGENT_INFO_NOT_FOUND.getCode());
					agentInfoNotFound.setValue("API-DynamicID not found. Cause:agentInfo not found. agentId:" + ex.getAgentId() + " startTime:" + ex.getStartTime());
					annotationBoList.add(agentInfoNotFound);
					return;
				}

				int apiId = (Integer) apiIdAnnotation.getValue();
				List<ApiMetaDataBo> apiMetaDataList = apiMetaDataDao.getApiMetaData(agentInfoBo.getAgentId(), agentInfoBo.getIdentifier(), apiId, agentInfoBo.getTimestamp());
				int size = apiMetaDataList.size();
				if (size == 0) {
					AnnotationBo api = new AnnotationBo();
					api.setKey(AnnotationKey.ERROR_API_METADATA_NOT_FOUND.getCode());
					api.setValue("API-DynamicID not found. api:" + apiId);
					annotationBoList.add(api);
				} else if (size == 1) {
					ApiMetaDataBo apiMetaDataBo = apiMetaDataList.get(0);
					AnnotationBo apiMetaData = new AnnotationBo();
					apiMetaData.setKey(AnnotationKey.API_METADATA.getCode());
					apiMetaData.setValue(apiMetaDataBo);
					annotationBoList.add(apiMetaData);

					AnnotationBo apiAnnotation = new AnnotationBo();
					apiAnnotation.setKey(AnnotationKey.API.getCode());
					String apiInfo = getApiInfo(apiMetaDataBo);
					apiAnnotation.setValue(apiInfo);
					annotationBoList.add(apiAnnotation);
				} else {
					AnnotationBo apiAnnotation = new AnnotationBo();
					apiAnnotation.setKey(AnnotationKey.ERROR_API_METADATA_DID_COLLSION.getCode());
					String collisonMessage = collisionApiDidMessage(apiId, apiMetaDataList);
					apiAnnotation.setValue(collisonMessage);
					annotationBoList.add(apiAnnotation);
				}

			}

		});
	}

	private AgentInfoBo findAgentInfoBoBeforeStartTime(SpanAlign spanAlign) {
		String agentId = getAgentId(spanAlign);
		long startTime = spanAlign.getSpanBo().getStartTime();
		AgentInfoBo agentInfoBeforeStartTime = agentInfoDao.findAgentInfoBeforeStartTime(agentId, startTime);
		if (agentInfoBeforeStartTime == null) {
			throw new AgentIdNotFoundException(agentId, startTime);
		}
		return agentInfoBeforeStartTime;
	}

	private String collisionApiDidMessage(int apidId, List<ApiMetaDataBo> apiMetaDataList) {
		// TODO 이거 체크하는 테스트를 따로 만들어야 될듯 하다. 왠간하면 확율상 hashCode 충돌 케이스를 쉽게 만들수 없음.
		StringBuilder sb = new StringBuilder(64);
		sb.append("Collision Api DynamicId:");
		sb.append(apidId);
		sb.append('\n');
		for (int i = 0; i < apiMetaDataList.size(); i++) {
			if (i != 0) {
				sb.append("or\n");
			}
			ApiMetaDataBo apiMetaDataBo = apiMetaDataList.get(i);
			sb.append(getApiInfo(apiMetaDataBo));
		}
		return sb.toString();
	}

	private String getApiInfo(ApiMetaDataBo apiMetaDataBo) {
		if (apiMetaDataBo.getLineNumber() != -1) {
			return apiMetaDataBo.getApiInfo() + ":" + apiMetaDataBo.getLineNumber();
		} else {
			return apiMetaDataBo.getApiInfo();
		}
	}

	private void transitionApiId(List<SpanAlign> spans) {
		this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
			@Override
			public void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList) {
				AnnotationBo apiIdAnnotation = findAnnotation(annotationBoList, AnnotationKey.API_ID.getCode());
				if (apiIdAnnotation == null) {
					return;
				}

				MethodMapping methodMapping = ApiMappingTable.findMethodMapping((Integer) apiIdAnnotation.getValue());
				if (methodMapping == null) {
					return;
				}
				String className = methodMapping.getClassMapping().getClassName();
				String methodName = methodMapping.getMethodName();
				String[] parameterType = methodMapping.getParameterType();
				String[] parameterName = methodMapping.getParameterName();
				String args = ApiUtils.mergeParameterVariableNameDescription(parameterType, parameterName);
				AnnotationBo api = new AnnotationBo();
				api.setKey(AnnotationKey.API.getCode());
				api.setValue(className + "." + methodName + args);
				annotationBoList.add(api);
			}
		});
	}

	public static interface AnnotationReplacementCallback {
		void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList);
	}

	private List<SpanAlign> order(List<SpanBo> spans) {
		SpanAligner2 spanAligner = new SpanAligner2(spans);
		return spanAligner.sort();

	}
}
