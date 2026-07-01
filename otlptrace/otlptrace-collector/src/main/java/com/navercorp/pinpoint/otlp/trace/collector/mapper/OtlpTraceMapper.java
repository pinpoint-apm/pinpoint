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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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
    private final boolean allowApplicationNameFallback;

    public OtlpTraceMapper(OtlpTraceSpanMapper spanMapper, OtlpTraceSpanEventMapper spanEventMapper, OtlpTraceSpanChunkMapper spanChunkMapper, OtlpAgentInfoMapper agentInfoMapper, OtlpExceptionMapper exceptionMapper,
                           @Value("${pinpoint.collector.otlptrace.application-name-fallback.enabled:false}") boolean allowApplicationNameFallback) {
        this.spanMapper = spanMapper;
        this.spanEventMapper = spanEventMapper;
        this.spanChunkMapper = spanChunkMapper;
        this.agentInfoMapper = agentInfoMapper;
        this.exceptionMapper = exceptionMapper;
        this.allowApplicationNameFallback = allowApplicationNameFallback;
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

                for (Span rootSpan : rootSpanList) {
                    try {
                        // Resolve URI and root span id per root: each exception in this root's
                        // subtree is attributed to the correct entry-point URI and linked back to
                        // the stored transaction (root SpanBo) via (transactionId, rootSpanId).
                        final Map<String, AttributeValue> rootAttributes =
                                OtlpTraceMapperUtils.getAttributeValueMap(rootSpan.getAttributesList());
                        final String rootUriTemplate = spanMapper.getServerSpanToRpc(rootSpan, rootAttributes);
                        final long rootSpanId = OtlpTraceMapperUtils.getSpanId(rootSpan.getSpanId());

                        final SpanBo spanBo = spanMapper.map(idAndName, rootSpan);

                        // root span's own exception
                        recordException(mapperData, idAndName, rootSpan, rootSpanId, rootUriTemplate);

                        // Exceptions on linked child spans are recorded as findLinkSpan traverses the
                        // original OTel spans (where the full stacktrace is still available). Orphan
                        // spans not linked to any root are intentionally skipped: without a root there
                        // is no transaction spanId to link the exception to.
                        final List<SpanEventBo> spanEventList = findLinkSpan(spanBo.getStartTimeNanos(), childSpanList, rootSpan.getSpanId(), 1,
                                childSpan -> recordException(mapperData, idAndName, childSpan, rootSpanId, rootUriTemplate));
                        spanBo.addSpanEventBoList(spanEventList);
                        mapperData.addSpanBo(spanBo);
                        final AgentInfoBo agentInfoBo = agentInfoMapper.map(spanBo, resourceAttributeMap);
                        mapperData.addAgentInfoBo(agentInfoBo);
                    } catch (Exception e) {
                        errorCount++;
                        logMappingError("Failed to map span", e);
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
                        logMappingError("Failed to map spanChunk", e);
                    }
                }

                if (!childSpanList.isEmpty()) {
                    // Orphan/cyclic spans that couldn't be linked into any trace tree are dropped.
                    // Reflected to the client via the rejected-span count rather than an operator log
                    // (client-data shape issue, and dumping every span would risk log flooding).
                    errorCount += childSpanList.size();
                    logger.debug("Dropped unknown spans count={}", childSpanList.size());
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
            return OtlpTraceMapperUtils.getId(resourceAttributeMap, allowApplicationNameFallback);
        } catch (IllegalArgumentException e) {
            // Client-side fault (invalid/missing identifier). The reason is reported back to the
            // client via the INVALID_ARGUMENT response, so an operator-facing log adds only noise.
            reject(mapperData, resourceSpan, e.getMessage());
            return null;
        } catch (Exception e) {
            // Unexpected server-side failure: keep visibility for the operator.
            logger.warn("Unexpected error resolving agent id", e);
            reject(mapperData, resourceSpan, e.getMessage());
            return null;
        }
    }

    /**
     * Records an exception-trace entry for a single OTel span, attributing it to the given root
     * (transaction) span id and URI. Failures are isolated here so a malformed exception payload
     * never aborts span/trace mapping.
     */
    private void recordException(OtlpTraceMapperData mapperData, IdAndName idAndName, Span span, long rootSpanId, String uriTemplate) {
        try {
            exceptionMapper.map(idAndName, span, rootSpanId, uriTemplate)
                    .ifPresent(mapperData::addExceptionMetaDataBo);
        } catch (Exception e) {
            logMappingError("Failed to map exception", e);
        }
    }

    private void logMappingError(String message, Exception e) {
        if (e instanceof IllegalArgumentException) {
            // Client-side data fault; reflected to the client via the rejected-span count.
            return;
        }
        // Unexpected server-side failure: keep visibility for the operator.
        logger.warn(message, e);
    }

    private void reject(OtlpTraceMapperData mapperData, ResourceSpans resourceSpan, String message) {
        OtlpTraceCollectorRejectedSpan rejectedSpan = mapperData.getRejectedSpan();
        int spansCount = resourceSpan.getScopeSpansCount();
        rejectedSpan.putMessage(message + " (" + spansCount + ")");
        rejectedSpan.addCount(spansCount);
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

        alignWrapperRoots(rootSpanList);
    }

    // When a thin wrapper root (e.g. CONSUMER 'receive' or INTERNAL poll loop) has its
    // startTime pinned to an artificial value (worker init, broker connect), the wrapper's
    // child CONSUMER carries the real processing startTime. Re-align the wrapper's startTime
    // to its earliest matching child so scatter/response-time stats reflect actual processing.
    // For the CONSUMER -> CONSUMER case, the upper CONSUMER is additionally re-typed as
    // SPAN_KIND_SERVER so OtlpTraceSpanMapper maps it to ServiceType.OPENTELEMETRY_SERVER
    // (a generic entry-point), letting the lower CONSUMER retain the messaging semantics.
    // The wrapper span itself is preserved so links and attributes stay intact.
    // Applied only when the wrapper itself has no upstream parent — a strong heuristic for
    // local poll/wrapper patterns rather than legitimate distributed-trace consumers.
    void alignWrapperRoots(List<Span> rootSpanList) {
        Map<ByteString, Span> rootById = new HashMap<>();
        for (Span s : rootSpanList) {
            rootById.put(s.getSpanId(), s);
        }

        Map<ByteString, Long> minChildStartByParent = new HashMap<>();
        for (Span child : rootSpanList) {
            ByteString pid = child.getParentSpanId();
            if (pid.isEmpty()) {
                continue;
            }
            Span parent = rootById.get(pid);
            if (parent == null) {
                continue;
            }
            if (!parent.getParentSpanId().isEmpty()) {
                // wrapper itself must be a top-level root (no upstream) to be aligned
                continue;
            }

            int pKind = parent.getKind().getNumber();
            int cKind = child.getKind().getNumber();
            boolean internalToConsumer =
                    pKind == Span.SpanKind.SPAN_KIND_INTERNAL_VALUE
                            && cKind == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE;
            boolean consumerToConsumer =
                    pKind == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE
                            && cKind == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE;
            if (!internalToConsumer && !consumerToConsumer) {
                continue;
            }

            minChildStartByParent.merge(pid, child.getStartTimeUnixNano(), Math::min);
        }

        if (minChildStartByParent.isEmpty()) {
            return;
        }

        for (int i = 0; i < rootSpanList.size(); i++) {
            Span s = rootSpanList.get(i);
            Long newStart = minChildStartByParent.get(s.getSpanId());
            if (newStart == null) {
                continue;
            }

            boolean alignStart = s.getStartTimeUnixNano() < newStart;
            // CONSUMER -> CONSUMER: the upper CONSUMER (this 's') becomes a generic entry-point.
            // Its kind is rewritten to SERVER so OtlpTraceSpanMapper assigns OPENTELEMETRY_SERVER.
            boolean retypeToServer = s.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE;

            if (!alignStart && !retypeToServer) {
                continue;
            }

            Span.Builder builder = Span.newBuilder(s);
            if (alignStart) {
                builder.setStartTimeUnixNano(newStart);
            }
            if (retypeToServer) {
                builder.setKind(Span.SpanKind.SPAN_KIND_SERVER);
            }
            rootSpanList.set(i, builder.build());
        }
    }

    List<SpanEventBo> findLinkSpan(long startTime, List<Span> childSpanList, ByteString parentSpanId, int depth) {
        // Orphan/spanChunk traversal: no exception processing (no root span to link against).
        return findLinkSpan(startTime, childSpanList, parentSpanId, depth, span -> {});
    }

    List<SpanEventBo> findLinkSpan(long startTime, List<Span> childSpanList, ByteString parentSpanId, int depth, Consumer<Span> spanConsumer) {
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
            spanConsumer.accept(span);
            final SpanEventBo spanEventBo = spanEventMapper.map(startTime, span, depth);
            spanEventList.add(spanEventBo);
            List<SpanEventBo> list = findLinkSpan(startTime, childSpanList, span.getSpanId(), depth + 1, spanConsumer);
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
            long rootStartTime = localRootSpan.getStartTimeUnixNano();
            // Recursively attach children as events
            List<SpanEventBo> childrenEvents = findLinkSpan(rootStartTime, childSpanList, localRootSpan.getSpanId(), 2);
            spanChunkBo.addSpanEventBoList(childrenEvents);
            spanChunkList.add(spanChunkBo);
        }

        return spanChunkList;
    }
}
