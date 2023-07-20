/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCount;
import com.navercorp.pinpoint.common.profiler.sql.DefaultSqlParser;
import com.navercorp.pinpoint.common.profiler.sql.OutputParameterParser;
import com.navercorp.pinpoint.common.profiler.sql.SqlParser;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.server.util.AnnotationUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.LineNumber;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;
import com.navercorp.pinpoint.web.calltree.span.Align;
import com.navercorp.pinpoint.web.calltree.span.CallTree;
import com.navercorp.pinpoint.web.calltree.span.CallTreeIterator;
import com.navercorp.pinpoint.web.calltree.span.SpanAligner;
import com.navercorp.pinpoint.web.calltree.span.TraceState;
import com.navercorp.pinpoint.web.dao.ApiMetaDataDao;
import com.navercorp.pinpoint.web.dao.SqlMetaDataDao;
import com.navercorp.pinpoint.web.dao.StringMetaDataDao;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.security.MetaDataFilter;
import com.navercorp.pinpoint.web.security.MetaDataFilter.MetaData;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author emeroad
 * @author jaehong.kim
 * @author minwoo.jung
 */
@Service
public class SpanServiceImpl implements SpanService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TraceDao traceDao;

    private final SqlMetaDataDao sqlMetaDataDao;

    private final MetaDataFilter metaDataFilter;

    private final ApiMetaDataDao apiMetaDataDao;

    private final StringMetaDataDao stringMetaDataDao;

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final AgentInfoService agentInfoService;

    private final SqlParser sqlParser = new DefaultSqlParser();
    private final OutputParameterParser outputParameterParser = new OutputParameterParser();

    public SpanServiceImpl(TraceDao traceDao,
                           SqlMetaDataDao sqlMetaDataDao,
                           Optional<MetaDataFilter> metaDataFilter,
                           ApiMetaDataDao apiMetaDataDao,
                           StringMetaDataDao stringMetaDataDao,
                           ServiceTypeRegistryService serviceTypeRegistryService,
                           AgentInfoService agentInfoService) {
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.sqlMetaDataDao = Objects.requireNonNull(sqlMetaDataDao, "sqlMetaDataDao");
        this.metaDataFilter = Objects.requireNonNull(metaDataFilter, "metaDataFilter").orElse(null);
        this.apiMetaDataDao = Objects.requireNonNull(apiMetaDataDao, "apiMetaDataDao");
        this.stringMetaDataDao = Objects.requireNonNull(stringMetaDataDao, "stringMetaDataDao");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
    }

    @Override
    public SpanResult selectSpan(TransactionId transactionId, Predicate<SpanBo> filter) {
        return selectSpan(transactionId, filter, ColumnGetCount.UNLIMITED_COLUMN_GET_COUNT);
    }

    @Override
    public SpanResult selectSpan(TransactionId transactionId, Predicate<SpanBo> filter, ColumnGetCount columnGetCount) {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(columnGetCount, "columnGetCount");

        final FetchResult<List<SpanBo>> fetchResult = traceDao.selectSpan(transactionId, columnGetCount);
        final List<SpanBo> spans = fetchResult.getData();
        logger.debug("selectSpan spans:{}", spans.size());

        populateAgentName(spans);
        if (CollectionUtils.isEmpty(spans)) {
            return new SpanResult(TraceState.State.ERROR, new CallTreeIterator(null));
        }

        final boolean isReachedLimit = columnGetCount.isReachedLimit(fetchResult.getFetchCount());

        final SpanResult result = order(spans, filter, isReachedLimit);
        final CallTreeIterator callTreeIterator = result.getCallTree();
        final List<Align> values = callTreeIterator.values();

        transitionDynamicApiId(values);
        transitionSqlId(values);
        transitionMongoJson(values);
        transitionCachedString(values);
        transitionException(values);

        // TODO need to at least show the row data when root span is not found.
        return result;
    }

    @Override
    public void populateAgentName(List<SpanBo> spanBoList) {
        if (CollectionUtils.isEmpty(spanBoList)) {
            return;
        }
        List<AgentIdStartTimeKey> query = spanBoList.stream()
                .map(this::newAgentStartTimeKey)
                .collect(Collectors.toList());

        Map<AgentIdStartTimeKey, Optional<String>> agentNameMap = this.getAgentName(query);

        bindAgentName(spanBoList, agentNameMap);
    }

    public AgentIdStartTimeKey newAgentStartTimeKey(SpanBo spanBo) {
        return new AgentIdStartTimeKey(spanBo.getAgentId(), spanBo.getAgentStartTime());
    }

    private void bindAgentName(List<SpanBo> list, Map<AgentIdStartTimeKey, Optional<String>> agentNameMap) {
        for (SpanBo spanBo : list) {
            AgentIdStartTimeKey key = new AgentIdStartTimeKey(spanBo.getAgentId(), spanBo.getAgentStartTime());
            Optional<String> agentName = agentNameMap.get(key);
            spanBo.setAgentName(agentName.orElse(StringUtils.EMPTY));
        }
    }

    private Map<AgentIdStartTimeKey, Optional<String>> getAgentName(List<AgentIdStartTimeKey> spanBoList) {
        if (CollectionUtils.isEmpty(spanBoList)) {
            return Collections.emptyMap();
        }

        Map<AgentIdStartTimeKey, Optional<String>> nameMap = new HashMap<>(spanBoList.size());
        for (AgentIdStartTimeKey key : spanBoList) {
            if (!nameMap.containsKey(key)) {
                Optional<String> agentName = getAgentName(key.getAgentId(), key.getAgentStartTime());
                nameMap.put(key, agentName);
            }
        }
        return nameMap;
    }


    private void transitionAnnotation(List<Align> spans, AnnotationReplacementCallback annotationReplacementCallback) {
        for (Align align : spans) {
            List<AnnotationBo> annotationBoList = align.getAnnotationBoList();
            if (annotationBoList == null) {
                annotationBoList = new ArrayList<>();
                align.setAnnotationBoList(annotationBoList);
            }
            annotationReplacementCallback.replacement(align, annotationBoList);
        }
    }

    private void transitionSqlId(final List<Align> spans) {
        this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
            @Override
            public void replacement(Align align, List<AnnotationBo> annotationBoList) {
                AnnotationBo sqlIdAnnotation = findAnnotation(annotationBoList, AnnotationKey.SQL_ID.getCode());
                if (sqlIdAnnotation == null) {
                    return;
                }
                if (metaDataFilter != null && metaDataFilter.filter(align, MetaData.SQL)) {
                    AnnotationBo annotationBo = metaDataFilter.createAnnotationBo(align, MetaData.SQL);
                    annotationBoList.add(annotationBo);
                    return;
                }

                // value of sqlId's annotation contains multiple values.
                final IntStringStringValue sqlValue = (IntStringStringValue) sqlIdAnnotation.getValue();
                final int sqlId = sqlValue.getIntValue();
                final String sqlParam = sqlValue.getStringValue1();
                final List<SqlMetaDataBo> sqlMetaDataList = sqlMetaDataDao.getSqlMetaData(align.getAgentId(), align.getAgentStartTime(), sqlId);
                final int size = sqlMetaDataList.size();
                if (size == 0) {
                    String errorMessage = "SQL-ID not found sqlId:" + sqlId;
                    AnnotationBo api = AnnotationBo.of(AnnotationKey.SQL.getCode(), errorMessage);
                    annotationBoList.add(api);
                } else if (size == 1) {
                    final SqlMetaDataBo sqlMetaDataBo = sqlMetaDataList.get(0);
                    if (StringUtils.isEmpty(sqlParam)) {
                        AnnotationBo sqlMeta = AnnotationBo.of(AnnotationKey.SQL_METADATA.getCode(), sqlMetaDataBo.getSql());
                        annotationBoList.add(sqlMeta);

//                        AnnotationBo checkFail = checkIdentifier(spanAlign, sqlMetaDataBo);
//                        if (checkFail != null) {
//                            // fail
//                            annotationBoList.add(checkFail);
//                            return;
//                        }

                        AnnotationBo sql = AnnotationBo.of(AnnotationKey.SQL.getCode(), StringUtils.trim(sqlMetaDataBo.getSql()));
                        annotationBoList.add(sql);
                    } else {
                        logger.debug("sqlMetaDataBo:{}", sqlMetaDataBo);
                        final String outputParams = sqlParam;
                        List<String> parsedOutputParams = outputParameterParser.parseOutputParameter(outputParams);
                        logger.debug("outputPrams:{}, parsedOutputPrams:{}", outputParams, parsedOutputParams);
                        String originalSql = sqlParser.combineOutputParams(sqlMetaDataBo.getSql(), parsedOutputParams);
                        logger.debug("outputPrams{}, originalSql:{}", outputParams, originalSql);

                        AnnotationBo sqlMeta = AnnotationBo.of(AnnotationKey.SQL_METADATA.getCode(), sqlMetaDataBo.getSql());
                        annotationBoList.add(sqlMeta);


                        AnnotationBo sql = AnnotationBo.of(AnnotationKey.SQL.getCode(), StringUtils.trim(originalSql));
                        annotationBoList.add(sql);

                    }
                } else {
                    // TODO need improvement
                    String collisionSqlIdCodeMessage = collisionSqlIdCodeMessage(sqlId, sqlMetaDataList);
                    AnnotationBo api = AnnotationBo.of(AnnotationKey.SQL.getCode(), collisionSqlIdCodeMessage);
                    annotationBoList.add(api);
                }
                // add if bindValue exists
                final String bindValue = sqlValue.getStringValue2();
                if (StringUtils.isNotEmpty(bindValue)) {
                    AnnotationBo bindValueAnnotation = AnnotationBo.of(AnnotationKey.SQL_BINDVALUE.getCode(), bindValue);
                    annotationBoList.add(bindValueAnnotation);
                }

            }

        });
    }

    private void transitionMongoJson(final List<Align> spans) {
        this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
            @Override
            public void replacement(Align align, List<AnnotationBo> annotationBoList) {

                for (int i = 0; i < annotationBoList.size(); i++) {
                    final AnnotationBo annotationBo = annotationBoList.get(i);
                    if (annotationBo.getKey() == MongoConstants.MONGO_COLLECTION_INFO.getCode()) {
                        AnnotationBo collectionOption = findAnnotation(annotationBoList, MongoConstants.MONGO_COLLECTION_OPTION.getCode());
                        String collectionValue = getCollectionInfo(align.getDestinationId(), annotationBo, collectionOption);
                        AnnotationBo replace = AnnotationBo.of(annotationBo.getKey(), collectionValue);
                        annotationBoList.set(i, replace);
                    }
                }

                AnnotationBo jsonAnnotation = findAnnotation(annotationBoList, MongoConstants.MONGO_JSON_DATA.getCode());
                if (jsonAnnotation == null) {
                    return;
                }

                final StringStringValue jsonValue = (StringStringValue) jsonAnnotation.getValue();

                final String json = jsonValue.getStringValue1();
                final String jsonbindValue = jsonValue.getStringValue2();

                if (StringUtils.isEmpty(json)) {
                    logger.debug("No values in Json");
                } else {
                    AnnotationBo jsonMeta = AnnotationBo.of(MongoConstants.MONGO_JSON.getCode(), json);
                    annotationBoList.add(jsonMeta);
                }

                if (StringUtils.isNotEmpty(jsonbindValue)) {
                    AnnotationBo bindValueAnnotation = AnnotationBo.of(MongoConstants.MONGO_JSON_BINDVALUE.getCode(), jsonbindValue);
                    annotationBoList.add(bindValueAnnotation);
                }
            }

            private String getCollectionInfo(String destinationId, AnnotationBo collection, AnnotationBo option) {
                StringBuilder builder = new StringBuilder(32);
                builder.append(destinationId);
                builder.append(".");
                builder.append(collection.getValue());
                if (option != null) {
                    builder.append(" with ");
                    builder.append(Objects.toString(option.getValue(), "").toUpperCase());
                }
                return builder.toString();
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


    private void transitionDynamicApiId(List<Align> spans) {
        this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
            @Override
            public void replacement(Align align, List<AnnotationBo> annotationBoList) {

                final int apiId = align.getApiId();
                if (apiId == 0) {
                    String apiString = AnnotationUtils.findApiAnnotation(annotationBoList);
                    // annotation base api
                    if (apiString != null) {
                        ApiMetaDataBo apiMetaDataBo = new ApiMetaDataBo(align.getAgentId(), align.getStartTime(), apiId,
                                LineNumber.NO_LINE_NUMBER, MethodTypeEnum.DEFAULT, apiString);

                        AnnotationBo apiAnnotation = AnnotationBo.of(AnnotationKey.API_METADATA.getCode(), apiMetaDataBo);
                        annotationBoList.add(apiAnnotation);
                        return;
                    }
                }

                // may be able to get a more accurate data using agentIdentifier.
                List<ApiMetaDataBo> apiMetaDataList = apiMetaDataDao.getApiMetaData(align.getAgentId(), align.getAgentStartTime(), apiId);
                int size = apiMetaDataList.size();
                if (size == 0) {
                    String errorMessage = "API-DynamicID not found. api:" + apiId;
                    AnnotationBo api = AnnotationBo.of(AnnotationKey.ERROR_API_METADATA_NOT_FOUND.getCode(), errorMessage);
                    annotationBoList.add(api);
                } else if (size == 1) {
                    ApiMetaDataBo apiMetaDataBo = apiMetaDataList.get(0);
                    AnnotationBo apiMetaData = AnnotationBo.of(AnnotationKey.API_METADATA.getCode(), apiMetaDataBo);
                    annotationBoList.add(apiMetaData);

                    if (apiMetaDataBo.getMethodTypeEnum() == MethodTypeEnum.DEFAULT) {
                        String apiInfo = getApiInfo(apiMetaDataBo);
                        AnnotationBo apiAnnotation = AnnotationBo.of(AnnotationKey.API.getCode(), apiInfo);
                        annotationBoList.add(apiAnnotation);
                    } else {
                        String apiTagInfo = getApiTagInfo(apiMetaDataBo);
                        AnnotationBo apiAnnotation = AnnotationBo.of(AnnotationKey.API_TAG.getCode(), apiTagInfo);
                        annotationBoList.add(apiAnnotation);
                    }
                } else {
                    String collisionMessage = collisionApiDidMessage(apiId, apiMetaDataList);
                    AnnotationBo apiAnnotation = AnnotationBo.of(AnnotationKey.ERROR_API_METADATA_DID_COLLSION.getCode(), collisionMessage);
                    annotationBoList.add(apiAnnotation);
                }

            }

        });
    }

    private void transitionCachedString(List<Align> spans) {
        this.transitionAnnotation(spans, new AnnotationReplacementCallback() {
            @Override
            public void replacement(Align align, List<AnnotationBo> annotationBoList) {

                List<AnnotationBo> cachedStringAnnotation = findCachedStringAnnotation(annotationBoList);
                if (cachedStringAnnotation.isEmpty()) {
                    return;
                }
                for (AnnotationBo annotationBo : cachedStringAnnotation) {
                    final int cachedArgsKey = annotationBo.getKey();
                    int stringMetaDataId = (Integer) annotationBo.getValue();
                    List<StringMetaDataBo> stringMetaList = stringMetaDataDao.getStringMetaData(align.getAgentId(), align.getAgentStartTime(), stringMetaDataId);
                    int size = stringMetaList.size();
                    if (size == 0) {
                        logger.warn("StringMetaData not Found {}/{}/{}", align.getAgentId(), stringMetaDataId, align.getAgentStartTime());
                        String errorMessage = "CACHED-STRING-ID not found. stringId:";
                        AnnotationBo api = AnnotationBo.of(AnnotationKey.ERROR_API_METADATA_NOT_FOUND.getCode(), errorMessage);
                        annotationBoList.add(api);
                    } else if (size >= 1) {
                        // key collision shouldn't really happen (probability too low)
                        StringMetaDataBo stringMetaDataBo = stringMetaList.get(0);

                        AnnotationBo stringMetaData = AnnotationBo.of(AnnotationKeyUtils.cachedArgsToArgs(cachedArgsKey), stringMetaDataBo.getStringValue());
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

    private void transitionException(List<Align> alignList) {
        for (Align align : alignList) {
            if (align.hasException()) {
                StringMetaDataBo stringMetaData = selectStringMetaData(align.getAgentId(), align.getExceptionId(), align.getAgentStartTime());
                align.setExceptionClass(stringMetaData.getStringValue());
            }
        }

    }

    private StringMetaDataBo selectStringMetaData(String agentId, int cacheId, long agentStartTime) {
        final List<StringMetaDataBo> metaDataList = stringMetaDataDao.getStringMetaData(agentId, agentStartTime, cacheId);
        if (CollectionUtils.isEmpty(metaDataList)) {
            logger.warn("StringMetaData not Found agent:{}, cacheId{}, agentStartTime:{}", agentId, cacheId, agentStartTime);
            return new StringMetaDataBo(agentId, agentStartTime, cacheId, "STRING-META-DATA-NOT-FOUND");
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
        if (LineNumber.isLineNumber(apiMetaDataBo.getLineNumber())) {
            return apiMetaDataBo.getApiInfo() + ":" + apiMetaDataBo.getLineNumber();
        } else {
            return apiMetaDataBo.getApiInfo();
        }
    }

    private String getApiTagInfo(ApiMetaDataBo apiMetaDataBo) {
        return apiMetaDataBo.getApiInfo();
    }

    public interface AnnotationReplacementCallback {
        void replacement(Align align, List<AnnotationBo> annotationBoList);
    }

    private SpanResult order(List<SpanBo> spans, Predicate<SpanBo> filter, boolean isReachedLimit) {
        SpanAligner spanAligner = new SpanAligner(spans, filter, serviceTypeRegistryService);
        final CallTree callTree = spanAligner.align();

        TraceState.State matchType = spanAligner.getMatchType();
        if (matchType == TraceState.State.PROGRESS && isReachedLimit) {
            matchType = TraceState.State.OVERFLOW;
        }

        return new SpanResult(matchType, callTree.iterator());
    }

    private Optional<String> getAgentName(String agentId, long agentStartTime) {
        final int deltaTimeInMilli = 1000;
        final AgentInfo agentInfo = this.agentInfoService.getAgentInfoWithoutStatus(agentId, agentStartTime, deltaTimeInMilli);
        return agentInfo == null ? Optional.empty() : Optional.ofNullable(agentInfo.getAgentName());
    }
}

