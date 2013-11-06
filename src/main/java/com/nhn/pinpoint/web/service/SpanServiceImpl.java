package com.nhn.pinpoint.web.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.nhn.pinpoint.common.bo.*;
import com.nhn.pinpoint.web.dao.StringMetaDataDao;
import com.nhn.pinpoint.web.vo.TransactionId;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nhn.pinpoint.web.calltree.span.SpanAlign;
import com.nhn.pinpoint.web.calltree.span.SpanAligner2;
import com.nhn.pinpoint.web.dao.ApiMetaDataDao;
import com.nhn.pinpoint.web.dao.SqlMetaDataDao;
import com.nhn.pinpoint.web.dao.TraceDao;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.util.OutputParameterParser;
import com.nhn.pinpoint.common.util.SqlParser;

/**
 * @author emeroad
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
    private StringMetaDataDao stringMetaDataDao;

	private final SqlParser sqlParser = new SqlParser();
	private final OutputParameterParser outputParameterParser = new OutputParameterParser();

	@Override
	public SpanResult selectSpan(TransactionId transactionId, long selectedSpanHint) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }

        List<SpanBo> spans = traceDao.selectSpanAndAnnotation(transactionId);
		if (spans == null || spans.isEmpty()) {
			return new SpanResult(SpanAligner2.FAIL_MATCH, Collections.<SpanAlign>emptyList());
		}

        SpanResult result = order(spans, selectedSpanHint);
        List<SpanAlign> order = result.getSpanAlign();
//		transitionApiId(order);
		transitionDynamicApiId(order);
		transitionSqlId(order);
        transitionCachedString(order);
        transitionException(order);
		// TODO root span not found시 row data라도 보여줘야 됨.
		return result;
	}



    private void transitionAnnotation(List<SpanAlign> spans, AnnotationReplacementCallback annotationReplacementCallback) {
        for (SpanAlign spanAlign : spans) {
			List<AnnotationBo> annotationBoList;
			if (spanAlign.isSpan()) {
				annotationBoList = spanAlign.getSpanBo().getAnnotationBoList();
                if (annotationBoList == null) {
                    annotationBoList = new ArrayList<AnnotationBo>();
                    spanAlign.getSpanBo().setAnnotationBoList(annotationBoList);
                }
				annotationReplacementCallback.replacement(spanAlign, annotationBoList);
			} else {
				annotationBoList = spanAlign.getSpanEventBo().getAnnotationBoList();
                if (annotationBoList == null) {
                    annotationBoList = new ArrayList<AnnotationBo>();
                    spanAlign.getSpanBo().setAnnotationBoList(annotationBoList);
                }
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

                final AgentKey agentKey = getAgentKey(spanAlign);

                // sqlId에 대한 annotation은 멀티 value가 날라옴.
                final IntStringStringValue sqlValue = (IntStringStringValue) sqlIdAnnotation.getValue();
                final int hashCode = sqlValue.getIntValue();
                final String sqlParam = sqlValue.getStringValue1();
				final List<SqlMetaDataBo> sqlMetaDataList = sqlMetaDataDao.getSqlMetaData(agentKey.getAgentId(), agentKey.getAgentStartTime(), hashCode);
				int size = sqlMetaDataList.size();
				if (size == 0) {
					AnnotationBo api = new AnnotationBo();
					api.setKey(AnnotationKey.SQL.getCode());
					api.setValue("SQL-ID not found hashCode:" + hashCode);
					annotationBoList.add(api);
				} else if (size == 1) {
					final SqlMetaDataBo sqlMetaDataBo = sqlMetaDataList.get(0);
					if (StringUtils.isEmpty(sqlParam)) {
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
						String outputParams = sqlParam;
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
                // bindValue가 존재할 경우 따라 넣어준다.
                final String bindValue = sqlValue.getStringValue2();
                if (StringUtils.isNotEmpty(bindValue)) {
                    AnnotationBo bindValueAnnotation = new AnnotationBo();
                    bindValueAnnotation.setKey(AnnotationKey.SQL_BINDVALUE.getCode());
                    bindValueAnnotation.setValue(bindValue);
                    annotationBoList.add(bindValueAnnotation);
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


	private void transitionDynamicApiId(List<SpanAlign> spans) {
		this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
			@Override
			public void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList) {
                final AgentKey key = getAgentKey(spanAlign);
                final int apiId = getApiId(spanAlign);
                // agentIdentifer를 기준으로 좀더 정확한 데이터를 찾을수 있을 듯 하다.
				List<ApiMetaDataBo> apiMetaDataList = apiMetaDataDao.getApiMetaData(key.getAgentId(), key.getAgentStartTime(), apiId);
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

    private void transitionCachedString(List<SpanAlign> spans) {
        this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
            @Override
            public void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList) {
                final AgentKey key = getAgentKey(spanAlign);
                List<AnnotationBo> cachedStringAnnotation = findCachedStringAnnotation(annotationBoList);
                if (cachedStringAnnotation.isEmpty()) {
                    return;
                }
                for (AnnotationBo annotationBo : cachedStringAnnotation) {
                    final int cachedArgsKey = annotationBo.getKey();
                    int stringMetaDataId = (Integer) annotationBo.getValue();
                    List<StringMetaDataBo> stringMetaList = stringMetaDataDao.getStringMetaData(key.getAgentId(), key.getAgentStartTime(), stringMetaDataId);
                    int size = stringMetaList.size();
                    if (size == 0) {
                        logger.warn("StringMetaData not Found {}/{}/{}", key.getAgentId(), stringMetaDataId, key.getAgentStartTime());
                        AnnotationBo api = new AnnotationBo();
                        // API METADATA ERROR가 아님. 추후 수정.
                        api.setKey(AnnotationKey.ERROR_API_METADATA_NOT_FOUND.getCode());
                        api.setValue("CACHED-STRING-ID not found. stringId:" + cachedArgsKey);
                        annotationBoList.add(api);
                    } else if (size >= 1) {
                        // key 충돌 경우는 후추 처리한다. 실제 상황에서는 일부러 만들지 않는한 발생할수 없다.
                        StringMetaDataBo stringMetaDataBo = stringMetaList.get(0);

                        AnnotationBo stringMetaData = new AnnotationBo();
                        stringMetaData.setKey(AnnotationKey.cachedArgsToArgs(cachedArgsKey));
                        stringMetaData.setValue(stringMetaDataBo.getStringValue());
                        annotationBoList.add(stringMetaData);
                        if (size > 1) {
                            logger.warn("stringMetaData size not 1 :{}", stringMetaList);
                        }
                    }
                }
            }

        });
    }

    private List<AnnotationBo> findCachedStringAnnotation(List<AnnotationBo> annotationBoList) {
        List<AnnotationBo> findAnnotationBoList = new ArrayList<AnnotationBo>(annotationBoList.size());
        for (AnnotationBo annotationBo : annotationBoList) {
            if (AnnotationKey.isCachedArgsKey(annotationBo.getKey())) {
                findAnnotationBoList.add(annotationBo);
            }
        }
        return findAnnotationBoList;
    }

    private void transitionException(List<SpanAlign> spanAlignList) {
        for (SpanAlign spanAlign : spanAlignList) {
            if (spanAlign.isSpan()) {
                final SpanBo spanBo = spanAlign.getSpanBo();
                if (spanBo.hasException()) {
                    StringMetaDataBo stringMetaData = selectStringMetaData(spanBo.getAgentId(), spanBo.getExceptionId(), spanBo.getAgentStartTime());
                    spanBo.setExceptionClass(stringMetaData.getStringValue());
                }
            } else {
                final SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
                if (spanEventBo.hasException()) {
                    StringMetaDataBo stringMetaData = selectStringMetaData(spanEventBo.getAgentId(), spanEventBo.getExceptionId(), spanEventBo.getAgentStartTime());
                    if (stringMetaData != null) {
                        spanEventBo.setExceptionClass(stringMetaData.getStringValue());
                    }
                }
            }
        }

    }

    private StringMetaDataBo selectStringMetaData(String agentId, int cacheId, long agentStartTime) {
        final List<StringMetaDataBo> metaDataList = stringMetaDataDao.getStringMetaData(agentId, agentStartTime, cacheId);
        if (metaDataList == null || metaDataList.isEmpty()) {
            logger.warn("StringMetaData not Found agent:{}, cacheId{}, agentStartTime:{}", agentId, cacheId, agentStartTime);
            StringMetaDataBo stringMetaDataBo = new StringMetaDataBo(agentId, agentStartTime, cacheId);
            stringMetaDataBo.setStringValue("STRING-META-DATA-NOT-FOUND");
            return stringMetaDataBo;
        }
        if (metaDataList.size() == 1) {
            return metaDataList.get(0);
        } else {
            // 일단 로그 찍고 처리.
            logger.warn("stringMetaData size not 1 :{}", metaDataList);
            return metaDataList.get(0);
        }
    }

    private int getApiId(SpanAlign spanAlign) {
        if (spanAlign.isSpan()) {
            return spanAlign.getSpanBo().getApiId();
        } else {
            return spanAlign.getSpanEventBo().getApiId();
        }
    }

    private AgentKey getAgentKey(SpanAlign spanAlign) {
        if (spanAlign.isSpan()) {
            SpanBo spanBo = spanAlign.getSpanBo();
            return new AgentKey(spanBo.getAgentId(), spanBo.getAgentStartTime());
        } else {
            final SpanEventBo spanEventBo = spanAlign.getSpanEventBo();
            return new AgentKey(spanEventBo.getAgentId(), spanEventBo.getAgentStartTime());
        }
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

	public static interface AnnotationReplacementCallback {
		void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList);
	}

	private SpanResult order(List<SpanBo> spans, long selectedSpanHint) {
		SpanAligner2 spanAligner = new SpanAligner2(spans, selectedSpanHint);
        List<SpanAlign> sort = spanAligner.sort();

        logger.trace("SpanAlignList:{}", sort);
        return new SpanResult(spanAligner.getMatchType(), sort);

	}


    private static final class AgentKey {

        private final String agentId;
        private final long agentStartTime;

        private AgentKey(String agentId, long agentStartTime) {
            if (agentId == null) {
                throw new NullPointerException("agentId must not be null");
            }
            this.agentId = agentId;
            this.agentStartTime = agentStartTime;
        }

        private String getAgentId() {
            return agentId;
        }

        private long getAgentStartTime() {
            return agentStartTime;
        }
    }
}

