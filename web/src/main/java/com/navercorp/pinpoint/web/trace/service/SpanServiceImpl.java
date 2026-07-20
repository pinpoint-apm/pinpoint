/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.web.trace.service;

import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCount;
import com.navercorp.pinpoint.common.profiler.sql.DefaultSqlNormalizer;
import com.navercorp.pinpoint.common.profiler.sql.OutputParameterParser;
import com.navercorp.pinpoint.common.profiler.sql.SqlNormalizer;
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.ExceptionInfo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.server.trace.ServerTraceId;
import com.navercorp.pinpoint.common.server.util.AnnotationUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.OpenTelemetryServiceTypeCategory;
import com.navercorp.pinpoint.common.trace.ServiceTypeCategory;
import com.navercorp.pinpoint.common.util.AnnotationKeyUtils;
import com.navercorp.pinpoint.common.util.BytesStringStringValue;
import com.navercorp.pinpoint.common.util.IntStringStringValue;
import com.navercorp.pinpoint.common.util.LineNumber;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.plugin.mongo.MongoConstants;
import com.navercorp.pinpoint.web.dao.ApiMetaDataDao;
import com.navercorp.pinpoint.web.dao.SqlMetaDataDao;
import com.navercorp.pinpoint.web.dao.SqlUidMetaDataDao;
import com.navercorp.pinpoint.web.dao.StringMetaDataDao;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.FetchResult;
import com.navercorp.pinpoint.web.trace.dao.TraceDao;
import com.navercorp.pinpoint.web.trace.security.MetaDataFilter;
import com.navercorp.pinpoint.web.trace.security.MetaDataFilter.MetaData;
import com.navercorp.pinpoint.web.trace.span.Align;
import com.navercorp.pinpoint.web.trace.span.CallTree;
import com.navercorp.pinpoint.web.trace.span.CallTreeIterator;
import com.navercorp.pinpoint.web.trace.span.CallTreeNode;
import com.navercorp.pinpoint.web.trace.span.SpanAligner;
import com.navercorp.pinpoint.web.trace.span.SpanCallTree;
import com.navercorp.pinpoint.web.trace.span.TraceState;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private final SqlUidMetaDataDao sqlUidMetaDataDao;

    private final MetaDataFilter metaDataFilter;

    private final ApiMetaDataDao apiMetaDataDao;

    private final StringMetaDataDao stringMetaDataDao;

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final AgentInfoService agentInfoService;

    private final SqlNormalizer sqlNormalizer = new DefaultSqlNormalizer();
    private final OutputParameterParser outputParameterParser = new OutputParameterParser();

    private final AnnotationCallbackExecutor annotationCallback;

    public SpanServiceImpl(TraceDao traceDao,
                           SqlMetaDataDao sqlMetaDataDao,
                           SqlUidMetaDataDao sqlUidMetaDataDao,
                           Optional<MetaDataFilter> metaDataFilter,
                           ApiMetaDataDao apiMetaDataDao,
                           StringMetaDataDao stringMetaDataDao,
                           ServiceTypeRegistryService serviceTypeRegistryService,
                           AgentInfoService agentInfoService) {
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.sqlMetaDataDao = Objects.requireNonNull(sqlMetaDataDao, "sqlMetaDataDao");
        this.sqlUidMetaDataDao = Objects.requireNonNull(sqlUidMetaDataDao, "sqlUidMetaDataDao");
        this.metaDataFilter = Objects.requireNonNull(metaDataFilter, "metaDataFilter").orElse(null);
        this.apiMetaDataDao = Objects.requireNonNull(apiMetaDataDao, "apiMetaDataDao");
        this.stringMetaDataDao = Objects.requireNonNull(stringMetaDataDao, "stringMetaDataDao");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");

        this.annotationCallback = newAnnotationCallback();
    }

    private @NonNull AnnotationCallbackExecutor newAnnotationCallback() {
        return new AnnotationCallbackExecutor(
                transitionDynamicApiId(),
                transitionSqlId(),
                transitionSqlUid(),
                transitionMongoJson(),
                transitionCachedString(),
                transitionException());
    }

    @Override
    public SpanResult selectSpan(ServerTraceId transactionId, Predicate<SpanBo> filter) {
        return selectSpan(transactionId, filter, ColumnGetCount.UNLIMITED_COLUMN_GET_COUNT);
    }

    @Override
    public SpanResult selectSpan(ServerTraceId transactionId, Predicate<SpanBo> filter, ColumnGetCount columnGetCount) {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(columnGetCount, "columnGetCount");

        final FetchResult<List<SpanBo>> fetchResult = traceDao.selectSpan(transactionId, columnGetCount);
        final List<SpanBo> spans = fetchResult.data();
        logger.debug("selectSpan spans:{}", spans.size());

        populateAgentName(spans);
        if (CollectionUtils.isEmpty(spans)) {
            return new SpanResult(TraceState.State.ERROR, new CallTreeIterator(null));
        }

        final boolean isReachedLimit = columnGetCount.isReachedLimit(fetchResult.fetchCount());

        final SpanResult result = order(spans, filter, isReachedLimit);
        final CallTreeIterator callTreeIterator = result.callTree();
        final List<Align> values = callTreeIterator.values();

        annotationCallback.replacement(values, prefetchMetaData(values));

        // TODO need to at least show the row data when root span is not found.
        return result;
    }

    @Override
    public SpanResult selectSpanAndLink(ServerTraceId transactionId, Predicate<SpanBo> filter,
                                        long graftSpanId, ServerTraceId linkServerTraceId,
                                        ColumnGetCount columnGetCount) {
        Objects.requireNonNull(transactionId, "transactionId");
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(linkServerTraceId, "linkServerTraceId");
        Objects.requireNonNull(columnGetCount, "columnGetCount");

        final FetchResult<List<SpanBo>> fetchResult = traceDao.selectSpan(transactionId, columnGetCount);
        final List<SpanBo> spans = fetchResult.data();
        logger.debug("selectSpanAndLink spans:{}", spans.size());

        populateAgentName(spans);
        if (CollectionUtils.isEmpty(spans)) {
            return new SpanResult(TraceState.State.ERROR, new CallTreeIterator(null));
        }

        final boolean isReachedLimit = columnGetCount.isReachedLimit(fetchResult.fetchCount());

        final SpanAligner mainAligner = new SpanAligner(spans, filter, serviceTypeRegistryService);
        final CallTree mainTree = mainAligner.align();

        TraceState.State matchType = mainAligner.getMatchType();
        if (matchType == TraceState.State.PROGRESS && isReachedLimit) {
            matchType = TraceState.State.OVERFLOW;
        }

        // Skip graft when link target equals main transaction; grafting would duplicate the tree under itself.
        if (transactionId.equals(linkServerTraceId)) {
            logger.warn("Skip OTel link graft: link target equals main transaction. transactionId={}", transactionId);
        } else if (mainTree instanceof SpanCallTree mainSpanTree) {
            final boolean grafted = graftLinkedTree(mainSpanTree, filter, graftSpanId, linkServerTraceId, columnGetCount);
            if (!grafted && matchType == TraceState.State.COMPLETE) {
                // Surface a partial result so the caller can signal the link could not be fully resolved.
                matchType = TraceState.State.PROGRESS;
            }
        }

        final CallTreeIterator callTreeIterator = mainTree.iterator();
        final List<Align> values = callTreeIterator.values();
        annotationCallback.replacement(values, prefetchMetaData(values));

        return new SpanResult(matchType, callTreeIterator);
    }

    private boolean graftLinkedTree(SpanCallTree mainSpanTree, Predicate<SpanBo> filter,
                                    long graftSpanId, ServerTraceId linkServerTraceId,
                                    ColumnGetCount columnGetCount) {
        // Main tree is the OTel Link target (upstream); upstream has no Link annotation pointing back,
        // so we locate the graft point by matching the upstream span's spanId directly.
        final CallTreeNode targetNode = findSpanNode(mainSpanTree.getRoot(), graftSpanId);
        if (targetNode == null) {
            logger.warn("Upstream span not found for graft. spanId:{}", graftSpanId);
            return false;
        }

        final FetchResult<List<SpanBo>> linkedFetch = traceDao.selectSpan(linkServerTraceId, columnGetCount);
        final List<SpanBo> linkedSpans = linkedFetch.data();
        if (CollectionUtils.isEmpty(linkedSpans)) {
            logger.warn("Linked spans not found. linkServerTraceId:{}", linkServerTraceId);
            return false;
        }
        populateAgentName(linkedSpans);

        final CallTree linkedTree = new SpanAligner(linkedSpans, filter, serviceTypeRegistryService).align();
        if (linkedTree.isEmpty()) {
            logger.warn("Linked tree empty after alignment. linkServerTraceId:{}", linkServerTraceId);
            return false;
        }

        mainSpanTree.setCursor(targetNode);
        mainSpanTree.add(linkedTree);
        return true;
    }

    private CallTreeNode findSpanNode(CallTreeNode start, long expectedSpanId) {
        FindSpanNodeTraversal traversal = new FindSpanNodeTraversal(expectedSpanId);
        return traversal.travel(start);
    }

    private static class FindSpanNodeTraversal {
        private static final int MAX_OVERFLOW_COUNT = 1024;
        private final long expectedSpanId;

        // Defence cycle ref
        // Weak validate
        private int overflowCounter;

        FindSpanNodeTraversal(long expectedSpanId) {
            this.expectedSpanId = expectedSpanId;
        }

        CallTreeNode travel(CallTreeNode node) {
            if (checkOverFlow()) {
                return null;
            }
            if (matches(node)) {
                return node;
            }
            if (node.hasChild()) {
                final CallTreeNode hit = travel(node.getChild());
                if (hit != null) {
                    return hit;
                }
            }

            // change logic from recursive to loop, because of avoid call-stack-overflow.
            CallTreeNode sibling = node.getSibling();
            while (sibling != null) {
                if (matches(sibling)) {
                    return sibling;
                }
                if (sibling.hasChild()) {
                    final CallTreeNode hit = travel(sibling.getChild());
                    if (hit != null) {
                        return hit;
                    }
                }
                sibling = sibling.getSibling();
            }
            return null;
        }

        private boolean checkOverFlow() {
            if (overflowCounter++ > MAX_OVERFLOW_COUNT) {
                return true;
            }
            return false;
        }

        boolean matches(CallTreeNode node) {
            final Align align = node.getAlign();
            if (align == null || align.isMeta()) {
                return false;
            }
            return matchesSpanId(align, expectedSpanId);
        }
    }

    private static boolean matchesSpanId(Align align, long expectedSpanId) {
        if (align.isSpan()) {
            return align.getSpanId() == expectedSpanId;
        }
        // SpanEvent: only meaningful when this event represents an OTel sub-span,
        // since legacy Pinpoint span events do not carry a distinct span id.
        return align.isOpenTelemetry() && align.getOpenTelemetrySpanId() == expectedSpanId;
    }

    public static class AnnotationCallbackExecutor {
        private final AnnotationReplacementCallback[] callbacks;

        public AnnotationCallbackExecutor(AnnotationReplacementCallback... callbacks) {
            this.callbacks = callbacks;
        }

        public void replacement(List<Align> spans, MetadataAccessor metadataAccessor) {
            for (Align align : spans) {
                List<AnnotationBo> annotationBoList = align.getAnnotationBoList();
                if (annotationBoList == null) {
                    annotationBoList = new ArrayList<>();
                    align.setAnnotationBoList(annotationBoList);
                }
                for (AnnotationReplacementCallback callback : callbacks) {
                    callback.replacement(align, annotationBoList, metadataAccessor);
                }
            }
        }
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
            spanBo.getSpanOwner().setAgentName(agentName.orElse(StringUtils.EMPTY));
        }
    }

    private Map<AgentIdStartTimeKey, Optional<String>> getAgentName(List<AgentIdStartTimeKey> spanBoList) {
        if (CollectionUtils.isEmpty(spanBoList)) {
            return Collections.emptyMap();
        }

        Map<AgentIdStartTimeKey, Optional<String>> nameMap = new HashMap<>(spanBoList.size());
        for (AgentIdStartTimeKey key : spanBoList) {
            if (!nameMap.containsKey(key)) {
                Optional<String> agentName = getAgentName(key.agentId(), key.agentStartTime());
                nameMap.put(key, agentName);
            }
        }
        return nameMap;
    }

    private AnnotationReplacementCallback transitionSqlId() {
        return new AnnotationReplacementCallback() {
            @Override
            public void replacement(Align align, List<AnnotationBo> annotationBoList, MetadataAccessor metadataAccessor) {
                AnnotationBo sqlIdAnnotation = AnnotationUtils.findAnnotation(annotationBoList, AnnotationKey.SQL_ID.getCode());
                if (sqlIdAnnotation == null) {
                    return;
                }
                if (metaDataFilter != null && metaDataFilter.filter(align, MetaData.SQL)) {
                    AnnotationBo annotationBo = metaDataFilter.createAnnotationBo(align, MetaData.SQL);
                    annotationBoList.add(annotationBo);
                    return;
                }

                final IntStringStringValue sqlValue = (IntStringStringValue) sqlIdAnnotation.getValue();

                final int sqlId = sqlValue.getIntValue();
                final String sqlParam = sqlValue.getStringValue1();
                final String bindValue = sqlValue.getStringValue2();

                List<SqlMetaDataBo> sqlMetaDataList = metadataAccessor.getSqlMetaData(align.getAgentId(), align.getAgentStartTime(), sqlId);

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
                        AnnotationBo sql = AnnotationBo.of(AnnotationKey.SQL.getCode(), StringUtils.trim(sqlMetaDataBo.getSql()));
                        annotationBoList.add(sql);
                    } else {
                        final String rippedSql = sqlMetaDataBo.getSql();

                        logger.debug("sqlMetaDataBo:{}", sqlMetaDataBo);
                        List<String> parsedOutputParams = outputParameterParser.parseOutputParameter(sqlParam);
                        logger.debug("outputParams:{}, parsedOutputParams:{}", sqlParam, parsedOutputParams);
                        String originalSql = sqlNormalizer.combineOutputParams(rippedSql, parsedOutputParams);
                        logger.debug("outputParams:{}, originalSql:{}", sqlParam, originalSql);

                        AnnotationBo sqlMeta = AnnotationBo.of(AnnotationKey.SQL_METADATA.getCode(), rippedSql);
                        annotationBoList.add(sqlMeta);
                        AnnotationBo sql = AnnotationBo.of(AnnotationKey.SQL.getCode(), StringUtils.trim(originalSql));
                        annotationBoList.add(sql);
                    }
                } else {
                    // TODO need a separate test case to test for hashCode collision (probability way too low for easy replication)
                    String collisionSqlIdCodeMessage = "Collision Sql sqlId:" + sqlId + "\n" +
                            sqlMetaDataList.stream()
                                    .map(SqlMetaDataBo::getSql)
                                    .collect(Collectors.joining("or\n"));
                    AnnotationBo api = AnnotationBo.of(AnnotationKey.SQL.getCode(), collisionSqlIdCodeMessage);
                    annotationBoList.add(api);
                }

                // add if bindValue exists
                if (StringUtils.isNotEmpty(bindValue)) {
                    AnnotationBo bindValueAnnotation = AnnotationBo.of(AnnotationKey.SQL_BINDVALUE.getCode(), bindValue);
                    annotationBoList.add(bindValueAnnotation);
                }
            }
        };
    }

    private AnnotationReplacementCallback transitionSqlUid() {
        return new AnnotationReplacementCallback() {
            @Override
            public void replacement(Align align, List<AnnotationBo> annotationBoList, MetadataAccessor metadataAccessor) {
                AnnotationBo sqlUidAnnotation = AnnotationUtils.findAnnotation(annotationBoList, AnnotationKey.SQL_UID.getCode());
                if (sqlUidAnnotation == null) {
                    return;
                }
                if (metaDataFilter != null && metaDataFilter.filter(align, MetaData.SQL)) {
                    AnnotationBo annotationBo = metaDataFilter.createAnnotationBo(align, MetaData.SQL);
                    annotationBoList.add(annotationBo);
                    return;
                }

                final BytesStringStringValue sqlValue = (BytesStringStringValue) sqlUidAnnotation.getValue();

                final byte[] sqlUid = sqlValue.getBytesValue();
                final String sqlParam = sqlValue.getStringValue1();
                final String bindValue = sqlValue.getStringValue2();

                List<SqlUidMetaDataBo> sqlUidMetaDataList = metadataAccessor.getSqlUidMetaData(align.getAgentId(), align.getAgentStartTime(), sqlUid);

                final int size = sqlUidMetaDataList.size();
                if (size == 0) {
                    String errorMessage = "SQL-UID not found sqlUid:" + Arrays.toString(sqlUid);
                    AnnotationBo api = AnnotationBo.of(AnnotationKey.SQL.getCode(), errorMessage);
                    annotationBoList.add(api);
                } else if (size == 1) {
                    final SqlUidMetaDataBo sqlUidMetaDataBo = sqlUidMetaDataList.get(0);
                    if (StringUtils.isEmpty(sqlParam)) {
                        AnnotationBo sqlMeta = AnnotationBo.of(AnnotationKey.SQL_METADATA.getCode(), sqlUidMetaDataBo.getSql());
                        annotationBoList.add(sqlMeta);
                        AnnotationBo sql = AnnotationBo.of(AnnotationKey.SQL.getCode(), StringUtils.trim(sqlUidMetaDataBo.getSql()));
                        annotationBoList.add(sql);
                    } else {
                        final String rippedSql = sqlUidMetaDataBo.getSql();

                        logger.debug("sqlUidMetaDataBo:{}", sqlUidMetaDataBo);
                        List<String> parsedOutputParams = outputParameterParser.parseOutputParameter(sqlParam);
                        logger.debug("outputParams:{}, parsedOutputParams:{}", sqlParam, parsedOutputParams);
                        String originalSql = sqlNormalizer.combineOutputParams(rippedSql, parsedOutputParams);
                        logger.debug("outputParams:{}, originalSql:{}", sqlParam, originalSql);

                        AnnotationBo sqlMeta = AnnotationBo.of(AnnotationKey.SQL_METADATA.getCode(), rippedSql);
                        annotationBoList.add(sqlMeta);
                        AnnotationBo sql = AnnotationBo.of(AnnotationKey.SQL.getCode(), StringUtils.trim(originalSql));
                        annotationBoList.add(sql);
                    }
                } else {
                    // TODO need a separate test case to test for hashCode collision (probability way too low for easy replication)
                    String collisionSqlUidCodeMessage = "Collision Sql sqlUid:" + Arrays.toString(sqlUid) + "\n" +
                            sqlUidMetaDataList.stream()
                                    .map(SqlUidMetaDataBo::getSql)
                                    .collect(Collectors.joining("or\n"));
                    AnnotationBo api = AnnotationBo.of(AnnotationKey.SQL.getCode(), collisionSqlUidCodeMessage);
                    annotationBoList.add(api);
                }

                // add if bindValue exists
                if (StringUtils.isNotEmpty(bindValue)) {
                    AnnotationBo bindValueAnnotation = AnnotationBo.of(AnnotationKey.SQL_BINDVALUE.getCode(), bindValue);
                    annotationBoList.add(bindValueAnnotation);
                }
            }
        };
    }

    private AnnotationReplacementCallback transitionMongoJson() {
        return new AnnotationReplacementCallback() {
            @Override
            public void replacement(Align align, List<AnnotationBo> annotationBoList, MetadataAccessor metadataAccessor) {

                for (int i = 0; i < annotationBoList.size(); i++) {
                    final AnnotationBo annotationBo = annotationBoList.get(i);
                    if (annotationBo.getKey() == MongoConstants.MONGO_COLLECTION_INFO.getCode()) {
                        AnnotationBo collectionOption = AnnotationUtils.findAnnotation(annotationBoList, MongoConstants.MONGO_COLLECTION_OPTION.getCode());
                        String collectionValue = getCollectionInfo(align.getDestinationId(), annotationBo, collectionOption);
                        AnnotationBo replace = AnnotationBo.of(annotationBo.getKey(), collectionValue);
                        annotationBoList.set(i, replace);
                    }
                }

                AnnotationBo jsonAnnotation = AnnotationUtils.findAnnotation(annotationBoList, MongoConstants.MONGO_JSON_DATA.getCode());
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
        };
    }

    private AnnotationReplacementCallback transitionDynamicApiId() {
        return new AnnotationReplacementCallback() {
            @Override
            public void replacement(Align align, List<AnnotationBo> annotationBoList, MetadataAccessor metadataAccessor) {

                final int apiId = align.getApiId();
                if (apiId == 0) {
                    String apiString = AnnotationUtils.findApiAnnotation(annotationBoList);
                    // annotation base api
                    if (apiString != null) {
                        // Treat the apiString as a WEB_REQUEST-style entry point label (rendered
                        // as-is on the Call Tree, no method-descriptor parsing) for:
                        //   - OTel SERVER spans (OPENTELEMETRY_SERVER ServiceType), and
                        //   - OTel CONSUMER root Spans on a messaging system (ServiceType in the
                        //     MESSAGE_BROKER category, 8300-8799). The same queue ServiceType
                        //     (KAFKA_CLIENT etc.) is also emitted on producer SpanEvents, so the
                        //     align.isSpan() guard prevents producer events from being
                        //     mis-classified.
                        final int serviceType = align.getServiceType();
                        final boolean isEntryPoint = OpenTelemetryServiceTypeCategory.isServer(serviceType)
                                || (align.isSpan() && ServiceTypeCategory.MESSAGE_BROKER.contains(serviceType));
                        ApiMetaDataBo apiMetaDataBo;
                        if (isEntryPoint) {
                            apiMetaDataBo = new ApiMetaDataBo(align.getAgentId(), align.getStartTimeMillis(), apiId, LineNumber.NO_LINE_NUMBER, MethodTypeEnum.WEB_REQUEST, apiString);
                        } else {
                            apiMetaDataBo = new ApiMetaDataBo(align.getAgentId(), align.getStartTimeMillis(), apiId, LineNumber.NO_LINE_NUMBER, MethodTypeEnum.DEFAULT, apiString);
                        }

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
        };
    }

    private AnnotationReplacementCallback transitionCachedString() {
        return new AnnotationReplacementCallback() {
            @Override
            public void replacement(Align align, List<AnnotationBo> annotationBoList, MetadataAccessor metadataAccessor) {

                List<AnnotationBo> cachedStringAnnotation = AnnotationUtils.findAnnotations(annotationBoList,
                        e -> AnnotationKeyUtils.isCachedArgsKey(e.getKey()));
                if (cachedStringAnnotation.isEmpty()) {
                    return;
                }
                for (AnnotationBo annotationBo : cachedStringAnnotation) {
                    final int cachedArgsKey = annotationBo.getKey();
                    int stringMetaDataId = (Integer) annotationBo.getValue();
                    List<StringMetaDataBo> stringMetaList = metadataAccessor.getStringMetaData(align.getAgentId(), align.getAgentStartTime(), stringMetaDataId);
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


        };
    }

    private AnnotationReplacementCallback transitionException() {
        return new AnnotationReplacementCallback() {
            @Override
            public void replacement(Align align, List<AnnotationBo> annotationBoList, MetadataAccessor metadataAccessor) {
                if (align.hasException()) {
                    ExceptionInfo exceptionInfo = align.getExceptionInfo();
                    if (align.isOpenTelemetry()) {
                        // OTel has no StringMetaData for the exception class name; it is encoded
                        // into exceptionInfo.message as "<className>:<message>" (empty className
                        // when unknown). Resolve the class name from the prefix and skip the
                        // (always-missing) StringMetaData lookup.
                        align.setExceptionClass(ExceptionInfo.otelClassName(exceptionInfo.message()));
                        return;
                    }
                    StringMetaDataBo stringMetaData = selectStringMetaData(metadataAccessor, align.getAgentId(), exceptionInfo.id(), align.getAgentStartTime());
                    align.setExceptionClass(stringMetaData.getStringValue());
                }
            }
        };
    }

    private StringMetaDataBo selectStringMetaData(MetadataAccessor metadataAccessor, String agentId, int cacheId, long agentStartTime) {
        final List<StringMetaDataBo> metaDataList = metadataAccessor.getStringMetaData(agentId, agentStartTime, cacheId);
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
        void replacement(Align align, List<AnnotationBo> annotationBoList, MetadataAccessor metadataAccessor);
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
        final long startTime = agentStartTime - deltaTimeInMilli;
        final long endTime = agentStartTime + deltaTimeInMilli;

        final AgentInfo agentInfo = this.agentInfoService.findAgentInfo(agentId, startTime, endTime);
        return agentInfo == null ? Optional.empty() : Optional.ofNullable(agentInfo.getAgentName());
    }

    private record AgentIdStartTimeKey(String agentId, long agentStartTime) {
        private AgentIdStartTimeKey {
            Objects.requireNonNull(agentId, "agentId");
        }
    }

    /**
     * Collects every SQL/SQL-UID/String metadata lookup needed to resolve the given aligns and
     * loads them in a single batch per metadata type, instead of issuing one HBase Get per align.
     * The {@link AnnotationReplacementCallback}s then read the resolved metadata from the returned
     * {@link MetadataAccessor} rather than calling the DAOs one row at a time (N+1).
     */
    private MetadataAccessor prefetchMetaData(List<Align> aligns) {
        // insertion-ordered so the DAO input list stays index-aligned with the lookup keys
        Map<UidLookupKey, SqlUidMetaDataDao.SqlUidMetaDataKey> sqlUidQueries = new LinkedHashMap<>();
        Map<IntLookupKey, SqlMetaDataDao.SqlMetaDataKey> sqlQueries = new LinkedHashMap<>();
        Map<IntLookupKey, StringMetaDataDao.StringMetaDataKey> stringQueries = new LinkedHashMap<>();

        for (Align align : aligns) {
            final String agentId = align.getAgentId();
            final long agentStartTime = align.getAgentStartTime();

            final List<AnnotationBo> annotationBoList = align.getAnnotationBoList();
            if (annotationBoList != null && !isSqlMetaDataFiltered(align)) {
                AnnotationBo sqlUidAnnotation = AnnotationUtils.findAnnotation(annotationBoList, AnnotationKey.SQL_UID.getCode());
                if (sqlUidAnnotation != null) {
                    final byte[] sqlUid = ((BytesStringStringValue) sqlUidAnnotation.getValue()).getBytesValue();
                    sqlUidQueries.putIfAbsent(new UidLookupKey(agentId, agentStartTime, ByteBuffer.wrap(sqlUid)),
                            new SqlUidMetaDataDao.SqlUidMetaDataKey(agentId, agentStartTime, sqlUid));
                }

                AnnotationBo sqlIdAnnotation = AnnotationUtils.findAnnotation(annotationBoList, AnnotationKey.SQL_ID.getCode());
                if (sqlIdAnnotation != null) {
                    final int sqlId = ((IntStringStringValue) sqlIdAnnotation.getValue()).getIntValue();
                    sqlQueries.putIfAbsent(new IntLookupKey(agentId, agentStartTime, sqlId),
                            new SqlMetaDataDao.SqlMetaDataKey(agentId, agentStartTime, sqlId));
                }
            }

            if (annotationBoList != null) {
                List<AnnotationBo> cachedStringAnnotations = AnnotationUtils.findAnnotations(annotationBoList,
                        e -> AnnotationKeyUtils.isCachedArgsKey(e.getKey()));
                for (AnnotationBo annotationBo : cachedStringAnnotations) {
                    addStringQuery(stringQueries, agentId, agentStartTime, (Integer) annotationBo.getValue());
                }
            }

            // exception class name is resolved via StringMetaData (legacy, non-OTel spans only)
            if (align.hasException() && !align.isOpenTelemetry()) {
                addStringQuery(stringQueries, agentId, agentStartTime, align.getExceptionInfo().id());
            }
        }

        return new MetadataAccessor(
                batchSqlUidMetaData(sqlUidQueries),
                batchSqlMetaData(sqlQueries),
                batchStringMetaData(stringQueries));
    }

    private boolean isSqlMetaDataFiltered(Align align) {
        return metaDataFilter != null && metaDataFilter.filter(align, MetaData.SQL);
    }

    private void addStringQuery(Map<IntLookupKey, StringMetaDataDao.StringMetaDataKey> stringQueries,
                                String agentId, long agentStartTime, int stringId) {
        stringQueries.putIfAbsent(new IntLookupKey(agentId, agentStartTime, stringId),
                new StringMetaDataDao.StringMetaDataKey(agentId, agentStartTime, stringId));
    }

    private Map<UidLookupKey, List<SqlUidMetaDataBo>> batchSqlUidMetaData(Map<UidLookupKey, SqlUidMetaDataDao.SqlUidMetaDataKey> queries) {
        if (queries.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UidLookupKey> lookupKeys = new ArrayList<>(queries.keySet());
        List<List<SqlUidMetaDataBo>> results = sqlUidMetaDataDao.getSqlUidMetaData(new ArrayList<>(queries.values()));
        return zip(lookupKeys, results);
    }

    private Map<IntLookupKey, List<SqlMetaDataBo>> batchSqlMetaData(Map<IntLookupKey, SqlMetaDataDao.SqlMetaDataKey> queries) {
        if (queries.isEmpty()) {
            return Collections.emptyMap();
        }
        List<IntLookupKey> lookupKeys = new ArrayList<>(queries.keySet());
        List<List<SqlMetaDataBo>> results = sqlMetaDataDao.getSqlMetaData(new ArrayList<>(queries.values()));
        return zip(lookupKeys, results);
    }

    private Map<IntLookupKey, List<StringMetaDataBo>> batchStringMetaData(Map<IntLookupKey, StringMetaDataDao.StringMetaDataKey> queries) {
        if (queries.isEmpty()) {
            return Collections.emptyMap();
        }
        List<IntLookupKey> lookupKeys = new ArrayList<>(queries.keySet());
        List<List<StringMetaDataBo>> results = stringMetaDataDao.getStringMetaData(new ArrayList<>(queries.values()));
        return zip(lookupKeys, results);
    }

    private static <K, V> Map<K, List<V>> zip(List<K> keys, List<List<V>> values) {
        if (keys.size() != values.size()) {
            throw new IllegalStateException("batch metadata result size mismatch keys:" + keys.size() + " values:" + values.size());
        }
        Map<K, List<V>> map = new HashMap<>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            map.put(keys.get(i), values.get(i));
        }
        return map;
    }

    private record UidLookupKey(String agentId, long agentStartTime, ByteBuffer sqlUid) {
    }

    private record IntLookupKey(String agentId, long agentStartTime, int id) {
    }

    /**
     * Request-scoped view over metadata prefetched by {@link #prefetchMetaData(List)}.
     * A missing key resolves to an empty list, matching the single-row DAO semantics.
     */
    public static final class MetadataAccessor {
        private final Map<UidLookupKey, List<SqlUidMetaDataBo>> sqlUidMetaData;
        private final Map<IntLookupKey, List<SqlMetaDataBo>> sqlMetaData;
        private final Map<IntLookupKey, List<StringMetaDataBo>> stringMetaData;

        private MetadataAccessor(Map<UidLookupKey, List<SqlUidMetaDataBo>> sqlUidMetaData,
                                 Map<IntLookupKey, List<SqlMetaDataBo>> sqlMetaData,
                                 Map<IntLookupKey, List<StringMetaDataBo>> stringMetaData) {
            this.sqlUidMetaData = sqlUidMetaData;
            this.sqlMetaData = sqlMetaData;
            this.stringMetaData = stringMetaData;
        }

        List<SqlUidMetaDataBo> getSqlUidMetaData(String agentId, long time, byte[] sqlUid) {
            return sqlUidMetaData.getOrDefault(new UidLookupKey(agentId, time, ByteBuffer.wrap(sqlUid)), Collections.emptyList());
        }

        List<SqlMetaDataBo> getSqlMetaData(String agentId, long time, int sqlId) {
            return sqlMetaData.getOrDefault(new IntLookupKey(agentId, time, sqlId), Collections.emptyList());
        }

        List<StringMetaDataBo> getStringMetaData(String agentId, long time, int stringId) {
            return stringMetaData.getOrDefault(new IntLookupKey(agentId, time, stringId), Collections.emptyList());
        }
    }
}
