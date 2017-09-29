/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import java.util.ArrayList;
import java.util.List;

import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.server.util.AnnotationUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;
import com.navercorp.pinpoint.common.util.DefaultSqlParser;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.OutputParameterParser;
import com.navercorp.pinpoint.common.util.SqlParser;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.web.calltree.span.CallTree;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.SpanAlign;
import com.navercorp.pinpoint.web.calltree.span.SpanAligner2;
import com.navercorp.pinpoint.web.dao.ApiMetaDataDao;
import com.navercorp.pinpoint.web.dao.SqlMetaDataDao;
import com.navercorp.pinpoint.web.dao.StringMetaDataDao;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.security.MetaDataFilter;
import com.navercorp.pinpoint.web.security.MetaDataFilter.MetaData;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author emeroad
 * @author jaehong.kim
 * @author minwoo.jung
 */
//@Service
public class SpanServiceImpl implements SpanService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("hbaseTraceDaoFactory")
    private TraceDao traceDao;

//    @Autowired
    private SqlMetaDataDao sqlMetaDataDao;
    
    @Autowired(required=false)
    private MetaDataFilter metaDataFilter;

    @Autowired
    private ApiMetaDataDao apiMetaDataDao;

    @Autowired
    private StringMetaDataDao stringMetaDataDao;

    private final SqlParser sqlParser = new DefaultSqlParser();
    private final OutputParameterParser outputParameterParser = new OutputParameterParser();

    public void setSqlMetaDataDao(SqlMetaDataDao sqlMetaDataDao) {
        this.sqlMetaDataDao = sqlMetaDataDao;
    }

    @Override
    public SpanResult selectSpan(TransactionId transactionId, long selectedSpanHint) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }

        final List<SpanBo> spans = traceDao.selectSpan(transactionId);
        if (CollectionUtils.isEmpty(spans)) {
            return new SpanResult(SpanAligner2.FAIL_MATCH, new CallTreeIterator(null));
        }

        final SpanResult result = order(spans, selectedSpanHint);
        final CallTreeIterator callTreeIterator = result.getCallTree();
        final List<SpanAlign> values = callTreeIterator.values();
        
        transitionDynamicApiId(values);
        transitionSqlId(values);
        transitionCachedString(values);
        transitionException(values);
        // TODO need to at least show the row data when root span is not found. 
        return result;
    }



    private void transitionAnnotation(List<SpanAlign> spans, AnnotationReplacementCallback annotationReplacementCallback) {
        for (SpanAlign spanAlign : spans) {
            List<AnnotationBo> annotationBoList = spanAlign.getAnnotationBoList();
            if (annotationBoList == null) {
                annotationBoList = new ArrayList<>();
                spanAlign.setAnnotationBoList(annotationBoList);
            }
            annotationReplacementCallback.replacement(spanAlign, annotationBoList);
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
                if (metaDataFilter != null && metaDataFilter.filter(spanAlign, MetaData.SQL)) {
                    AnnotationBo annotationBo = metaDataFilter.createAnnotationBo(spanAlign, MetaData.SQL);
                    annotationBoList.add(annotationBo);
                    return;
                }

                // value of sqlId's annotation contains multiple values.
                final IntStringStringValue sqlValue = (IntStringStringValue) sqlIdAnnotation.getValue();
                final int sqlId = sqlValue.getIntValue();
                final String sqlParam = sqlValue.getStringValue1();
                final List<SqlMetaDataBo> sqlMetaDataList = sqlMetaDataDao.getSqlMetaData(spanAlign.getAgentId(), spanAlign.getAgentStartTime(), sqlId);
                final int size = sqlMetaDataList.size();
                if (size == 0) {
                    AnnotationBo api = new AnnotationBo();
                    api.setKey(AnnotationKey.SQL.getCode());
                    api.setValue("SQL-ID not found sqlId:" + sqlId);
                    annotationBoList.add(api);
                } else if (size == 1) {
                    final SqlMetaDataBo sqlMetaDataBo = sqlMetaDataList.get(0);
                    if (StringUtils.isEmpty(sqlParam)) {
                        AnnotationBo sqlMeta = new AnnotationBo();
                        sqlMeta.setKey(AnnotationKey.SQL_METADATA.getCode());
                        sqlMeta.setValue(sqlMetaDataBo.getSql());
                        annotationBoList.add(sqlMeta);

//                        AnnotationBo checkFail = checkIdentifier(spanAlign, sqlMetaDataBo);
//                        if (checkFail != null) {
//                            // fail
//                            annotationBoList.add(checkFail);
//                            return;
//                        }

                        AnnotationBo sql = new AnnotationBo();
                        sql.setKey(AnnotationKey.SQL.getCode());
                        sql.setValue(sqlMetaDataBo.getSql().trim());
                        annotationBoList.add(sql);
                    } else {
                        logger.debug("sqlMetaDataBo:{}", sqlMetaDataBo);
                        final String outputParams = sqlParam;
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
                        sql.setValue(originalSql.trim());
                        annotationBoList.add(sql);

                    }
                } else {
                    // TODO need improvement
                    AnnotationBo api = new AnnotationBo();
                    api.setKey(AnnotationKey.SQL.getCode());
                    api.setValue(collisionSqlIdCodeMessage(sqlId, sqlMetaDataList));
                    annotationBoList.add(api);
                }
                // add if bindValue exists
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

    private String collisionSqlIdCodeMessage(int sqlId, List<SqlMetaDataBo> sqlMetaDataList) {
        // TODO need a separate test case to test for hashCode collision (probability way too low for easy replication)
        StringBuilder sb = new StringBuilder(64);
        sb.append("Collision Sql sqlId:");
        sb.append(sqlId);
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

                final int apiId = spanAlign.getApiId();
                if (apiId == 0) {
                    String apiString = AnnotationUtils.findApiAnnotation(annotationBoList);
                    // annotation base api
                    if (apiString != null) {
                        ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo(spanAlign.getAgentId(), spanAlign.getStartTime(), apiId);
                        apiMetaDataBo.setApiInfo(apiString);
                        apiMetaDataBo.setLineNumber(-1);
                        apiMetaDataBo.setMethodTypeEnum(MethodTypeEnum.DEFAULT);

                        AnnotationBo apiAnnotation = new AnnotationBo();
                        apiAnnotation.setKey(AnnotationKey.API_METADATA.getCode());
                        apiAnnotation.setValue(apiMetaDataBo);
                        annotationBoList.add(apiAnnotation);
                        return;
                    }
                }

                // may be able to get a more accurate data using agentIdentifier.
                List<ApiMetaDataBo> apiMetaDataList = apiMetaDataDao.getApiMetaData(spanAlign.getAgentId(), spanAlign.getAgentStartTime(), apiId);
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

                    if (apiMetaDataBo.getMethodTypeEnum() == MethodTypeEnum.DEFAULT) {

                        AnnotationBo apiAnnotation = new AnnotationBo();
                        apiAnnotation.setKey(AnnotationKey.API.getCode());
                        String apiInfo = getApiInfo(apiMetaDataBo);
                        apiAnnotation.setValue(apiInfo);
                        annotationBoList.add(apiAnnotation);
                    } else {
                        AnnotationBo apiAnnotation = new AnnotationBo();
                        apiAnnotation.setKey(AnnotationKey.API_TAG.getCode());
                        apiAnnotation.setValue(getApiTagInfo(apiMetaDataBo));
                        annotationBoList.add(apiAnnotation);
                    }
                } else {
                    AnnotationBo apiAnnotation = new AnnotationBo();
                    apiAnnotation.setKey(AnnotationKey.ERROR_API_METADATA_DID_COLLSION.getCode());
                    String collisionMessage = collisionApiDidMessage(apiId, apiMetaDataList);
                    apiAnnotation.setValue(collisionMessage);
                    annotationBoList.add(apiAnnotation);
                }

            }

        });
    }

    private void transitionCachedString(List<SpanAlign> spans) {
        this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
            @Override
            public void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList) {

                List<AnnotationBo> cachedStringAnnotation = findCachedStringAnnotation(annotationBoList);
                if (cachedStringAnnotation.isEmpty()) {
                    return;
                }
                for (AnnotationBo annotationBo : cachedStringAnnotation) {
                    final int cachedArgsKey = annotationBo.getKey();
                    int stringMetaDataId = (Integer) annotationBo.getValue();
                    List<StringMetaDataBo> stringMetaList = stringMetaDataDao.getStringMetaData(spanAlign.getAgentId(), spanAlign.getAgentStartTime(), stringMetaDataId);
                    int size = stringMetaList.size();
                    if (size == 0) {
                        logger.warn("StringMetaData not Found {}/{}/{}", spanAlign.getAgentId(), stringMetaDataId, spanAlign.getAgentStartTime());
                        AnnotationBo api = new AnnotationBo();
                        api.setKey(AnnotationKey.ERROR_API_METADATA_NOT_FOUND.getCode());
                        api.setValue("CACHED-STRING-ID not found. stringId:" + cachedArgsKey);
                        annotationBoList.add(api);
                    } else if (size >= 1) {
                        // key collision shouldn't really happen (probability too low)
                        StringMetaDataBo stringMetaDataBo = stringMetaList.get(0);

                        AnnotationBo stringMetaData = new AnnotationBo();
                        stringMetaData.setKey(AnnotationKeyUtils.cachedArgsToArgs(cachedArgsKey));
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
        List<AnnotationBo> findAnnotationBoList = new ArrayList<>(annotationBoList.size());
        for (AnnotationBo annotationBo : annotationBoList) {
            if (AnnotationKeyUtils.isCachedArgsKey(annotationBo.getKey())) {
                findAnnotationBoList.add(annotationBo);
            }
        }
        return findAnnotationBoList;
    }

    private void transitionException(List<SpanAlign> spanAlignList) {
        for (SpanAlign spanAlign : spanAlignList) {
            if (spanAlign.hasException()) {
                StringMetaDataBo stringMetaData = selectStringMetaData(spanAlign.getAgentId(), spanAlign.getExceptionId(), spanAlign.getAgentStartTime());
                spanAlign.setExceptionClass(stringMetaData.getStringValue());
            }
        }

    }

    private StringMetaDataBo selectStringMetaData(String agentId, int cacheId, long agentStartTime) {
        final List<StringMetaDataBo> metaDataList = stringMetaDataDao.getStringMetaData(agentId, agentStartTime, cacheId);
        if (CollectionUtils.isEmpty(metaDataList)) {
            logger.warn("StringMetaData not Found agent:{}, cacheId{}, agentStartTime:{}", agentId, cacheId, agentStartTime);
            StringMetaDataBo stringMetaDataBo = new StringMetaDataBo(agentId, agentStartTime, cacheId);
            stringMetaDataBo.setStringValue("STRING-META-DATA-NOT-FOUND");
            return stringMetaDataBo;
        }
        if (metaDataList.size() == 1) {
            return metaDataList.get(0);
        } else {
            logger.warn("stringMetaData size not 1 :{}", metaDataList);
            return metaDataList.get(0);
        }
    }

    private String collisionApiDidMessage(int apidId, List<ApiMetaDataBo> apiMetaDataList) {
        // TODO need a separate test case to test for apidId collision (probability way too low for easy replication)
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
    
    private String getApiTagInfo(ApiMetaDataBo apiMetaDataBo) {
        return apiMetaDataBo.getApiInfo();
    }

    public interface AnnotationReplacementCallback {
        void replacement(SpanAlign spanAlign, List<AnnotationBo> annotationBoList);
    }

    private SpanResult order(List<SpanBo> spans, long selectedSpanHint) {
        SpanAligner2 spanAligner = new SpanAligner2(spans, selectedSpanHint);
        final CallTree callTree = spanAligner.sort();

        return new SpanResult(spanAligner.getMatchType(), callTree.iterator());
    }

}

