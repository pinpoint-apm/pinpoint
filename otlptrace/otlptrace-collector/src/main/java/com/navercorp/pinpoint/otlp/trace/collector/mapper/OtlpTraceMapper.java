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

package com.navercorp.pinpoint.otlp.trace.collector.mapper;

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorRejectedSpan;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class OtlpTraceMapper {
    private final Logger logger = LogManager.getLogger(this.getClass());

    public static final String SERVER_METHOD_NAME = "Server";
    public static final String CLIENT_METHOD_NAME = "Client";
    public static final String PRODUCER_METHOD_NAME = "Producer";
    public static final String CONSUMER_METHOD_NAME = "Consumer";
    public static final String INTERNAL_METHOD_NAME = "Internal";
    public static final String LINK_METHOD_NAME = "Link";

    private final OtlpTraceSpanMapper spanMapper;
    private final OtlpTraceSpanEventMapper spanEventMapper;
    private final OtlpTraceSpanChunkMapper spanChunkMapper;
    private final OtlpAgentInfoMapper agentInfoMapper;
    private final OtlpExceptionMapper exceptionMapper;

    public OtlpTraceMapper(OtlpTraceSpanMapper spanMapper, OtlpTraceSpanEventMapper spanEventMapper, OtlpTraceSpanChunkMapper spanChunkMapper, OtlpAgentInfoMapper agentInfoMapper, OtlpExceptionMapper exceptionMapper) {
        this.spanMapper = spanMapper;
        this.spanEventMapper = spanEventMapper;
        this.spanChunkMapper = spanChunkMapper;
        this.agentInfoMapper = agentInfoMapper;
        this.exceptionMapper = exceptionMapper;
    }

    // sort by traceId
    // find root span, server type, no parentSpanId
    // link span, find parentSpanId
    public OtlpTraceMapperData map(List<ResourceSpans> resourceSpanList) {
        final OtlpTraceMapperData mapperData = new OtlpTraceMapperData();
        int errorCount = 0;
        for (ResourceSpans resourceSpan : resourceSpanList) {
            final Map<String, AttributeValue> resourceAttributeMap = OtlpTraceMapperUtils.getAttributeValueMap(resourceSpan.getResource().getAttributesList());
            final IdAndName idAndName = getId(mapperData, resourceSpan, resourceAttributeMap);
            if (idAndName == null) {
                // skip
                continue;
            }

            final List<ScopeSpans> scopeSpanList = resourceSpan.getScopeSpansList();
            final Map<ByteString, List<Span>> spanMap = getSpanMap(scopeSpanList);

            // find root span, server type, no parentSpanId
            for (Map.Entry<ByteString, List<Span>> entry : spanMap.entrySet()) {
                List<Span> rootSpanList = new ArrayList<>();
                List<Span> childSpanList = new ArrayList<>();
                initRootAndChild(entry.getValue(), rootSpanList, childSpanList);

                for (Span span : entry.getValue()) {
                    exceptionMapper.map(idAndName, span).ifPresent(mapperData::addExceptionMetaDataBo);
                }

                for (Span rootSpan : rootSpanList) {
                    try {
                        final SpanBo spanBo = spanMapper.map(idAndName, rootSpan);
                        final List<SpanEventBo> spanEventList = findLinkSpan(spanBo.getStartTime(), childSpanList, rootSpan.getSpanId(), 1);
                        spanBo.addSpanEventBoList(spanEventList);
                        mapperData.addSpanBo(spanBo);
                        final AgentInfoBo agentInfoBo = agentInfoMapper.map(spanBo, resourceAttributeMap);
                        mapperData.addAgentInfoBo(agentInfoBo);
                    } catch (Exception e) {
                        errorCount++;
                        logger.warn("Failed to map span", e);
                    }
                }

                // Build trees from remaining child spans (orphan sub-traces)
                if (!childSpanList.isEmpty()) {
                    try {
                        List<SpanChunkBo> spanChunkBoList = findLinkSpanChunk(idAndName, childSpanList);
                        for (SpanChunkBo spanChunkBo : spanChunkBoList) {
                            mapperData.addSpanChunkBo(spanChunkBo);
                        }
                    } catch (Exception e) {
                        errorCount++;
                        logger.warn("Failed to map spanChunk", e);
                    }
                }

                if (!childSpanList.isEmpty()) {
                    logger.warn("Unknown spans={}", childSpanList);
                }
            }
        }

        if (errorCount > 0) {
            OtlpTraceCollectorRejectedSpan rejectedSpan = mapperData.getRejectedSpan();
            rejectedSpan.putMessage("mapping error (" + errorCount + ")");
            rejectedSpan.addCount(errorCount);
        }
        return mapperData;
    }

    IdAndName getId(OtlpTraceMapperData mapperData, ResourceSpans resourceSpan, Map<String, AttributeValue> resourceAttributeMap) {
        try {
            return OtlpTraceMapperUtils.getId(resourceAttributeMap);
        } catch (Exception e) {
            logger.warn("Failed to auth", e);
            OtlpTraceCollectorRejectedSpan rejectedSpan = mapperData.getRejectedSpan();
            int spansCount = resourceSpan.getScopeSpansCount();
            rejectedSpan.putMessage(e.getMessage() + " (" + spansCount + ")");
            rejectedSpan.addCount(spansCount);
            return null;
        }
    }

    Map<ByteString, List<Span>> getSpanMap(List<ScopeSpans> scopeSpanList) {
        Map<ByteString, List<Span>> spanMap = new HashMap<>();
        for (ScopeSpans scopeSpan : scopeSpanList) {
            List<Span> spansList = scopeSpan.getSpansList();
            for (Span span : spansList) {
                spanMap.computeIfAbsent(span.getTraceId(), k -> new ArrayList<>()).add(span);
            }
        }
        return spanMap;
    }

    void initRootAndChild(List<Span> spanList, List<Span> rootSpanList, List<Span> childSpanList) {
        for (Span span : spanList) {
            if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE) {
                rootSpanList.add(span);
            } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
                rootSpanList.add(span);
            } else {
                // client, producer, internal
                if (span.getParentSpanId().isEmpty()) {
                    // even the client type can be root if there is no parentSpanId value.
                    rootSpanList.add(span);
                } else {
                    childSpanList.add(span);
                }
            }
        }
    }

    List<SpanEventBo> findLinkSpan(long startTime, List<Span> childSpanList, ByteString parentSpanId, int depth) {
        List<SpanEventBo> spanEventList = new ArrayList<>();
        if (depth > 99) {
            // defensive check
            return spanEventList;
        }

        // Collect matching spans and remove them from childSpanList in a single O(n) pass,
        // avoiding the O(n*m) cost of a separate removeAll call.
        List<Span> linkSpanList = new ArrayList<>();
        Iterator<Span> iterator = childSpanList.iterator();
        while (iterator.hasNext()) {
            Span span = iterator.next();
            if (parentSpanId.equals(span.getParentSpanId())) {
                linkSpanList.add(span);
                iterator.remove();
            }
        }

        for (Span span : linkSpanList) {
            final SpanEventBo spanEventBo = spanEventMapper.map(startTime, span, depth);
            spanEventList.add(spanEventBo);
            List<SpanEventBo> list = findLinkSpan(startTime, childSpanList, span.getSpanId(), depth + 1);
            spanEventList.addAll(list);
        }

        return spanEventList;
    }

    List<SpanChunkBo> findLinkSpanChunk(IdAndName idAndName, List<Span> childSpanList) {
        // Collect all spanIds for quick lookup
        List<SpanChunkBo> spanChunkList = new ArrayList<>();
        Set<ByteString> spanIdSet = new HashSet<>();
        for (Span s : childSpanList) {
            spanIdSet.add(s.getSpanId());
        }
        // Identify local roots: parentSpanId is empty or not present in current set
        List<Span> localRootSpanList = new ArrayList<>();
        for (Span s : childSpanList) {
            if (!spanIdSet.contains(s.getParentSpanId())) {
                localRootSpanList.add(s);
            }
        }
        // If no local root (possible cycle), pick one arbitrarily
        if (localRootSpanList.isEmpty()) {
            localRootSpanList.addAll(childSpanList);
        }
        // Remove all local roots at once in O(n) using Set, instead of O(n*k) repeated remove calls.
        Set<Span> localRootSet = new HashSet<>(localRootSpanList);
        childSpanList.removeIf(localRootSet::contains);

        for (Span localRootSpan : localRootSpanList) {
            // Map root as SpanChunk (attached to its parentSpanId if present)
            SpanChunkBo spanChunkBo = spanChunkMapper.map(idAndName, localRootSpan);
            long rootStartTime = TimeUnit.NANOSECONDS.toMillis(localRootSpan.getStartTimeUnixNano());
            // Recursively attach children as events
            List<SpanEventBo> childrenEvents = findLinkSpan(rootStartTime, childSpanList, localRootSpan.getSpanId(), 2);
            spanChunkBo.addSpanEventBoList(childrenEvents);
            spanChunkList.add(spanChunkBo);
        }

        return spanChunkList;
    }
}
