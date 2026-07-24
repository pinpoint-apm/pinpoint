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
import com.navercorp.pinpoint.common.server.bo.AnnotationBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorRejectedSpan;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.SpanFlags;
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
import java.util.Objects;
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

    // W3C trace-flags sampled bit, carried in the low 8 bits of Span.flags.
    private static final int W3C_SAMPLED_FLAG = 0x01;

    private final OtlpTraceSpanMapper spanMapper;
    private final OtlpTraceSpanEventMapper spanEventMapper;
    private final OtlpTraceSpanChunkMapper spanChunkMapper;
    private final OtlpAgentInfoMapper agentInfoMapper;
    private final OtlpExceptionMapper exceptionMapper;
    private final OtlpExceptionInfoResolver exceptionInfoResolver;
    private final OtlpAgentStartTimeResolver agentStartTimeResolver;
    private final boolean allowApplicationNameFallback;

    public OtlpTraceMapper(OtlpTraceSpanMapper spanMapper, OtlpTraceSpanEventMapper spanEventMapper, OtlpTraceSpanChunkMapper spanChunkMapper, OtlpAgentInfoMapper agentInfoMapper, OtlpExceptionMapper exceptionMapper,
                           OtlpExceptionInfoResolver exceptionInfoResolver,
                           OtlpAgentStartTimeResolver agentStartTimeResolver,
                           @Value("${pinpoint.collector.otlptrace.application-name-fallback.enabled:false}") boolean allowApplicationNameFallback) {
        this.spanMapper = spanMapper;
        this.spanEventMapper = spanEventMapper;
        this.spanChunkMapper = spanChunkMapper;
        this.agentInfoMapper = agentInfoMapper;
        this.exceptionMapper = exceptionMapper;
        this.exceptionInfoResolver = Objects.requireNonNull(exceptionInfoResolver, "exceptionInfoResolver");
        this.agentStartTimeResolver = Objects.requireNonNull(agentStartTimeResolver, "agentStartTimeResolver");
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
            // Stateless, per-ResourceSpans: UNSET(-1) when process.creation.time is absent/unusable,
            // in which case the span mapper keeps the span-start-time approximation.
            final long agentStartTime = agentStartTimeResolver.resolve(resourceAttributeMap);

            final List<ScopeSpans> scopeSpanList = resourceSpan.getScopeSpansList();
            final Map<ByteString, List<ScopedSpan>> spanMap = getSpanMap(scopeSpanList, mapperData.getRejectedSpan());

            // find root span, server type, no parentSpanId
            for (Map.Entry<ByteString, List<ScopedSpan>> entry : spanMap.entrySet()) {
                List<ScopedSpan> rootSpanList = new ArrayList<>();
                List<ScopedSpan> childSpanList = new ArrayList<>();
                initRootAndChild(entry.getValue(), rootSpanList, childSpanList);

                for (ScopedSpan rootScopedSpan : rootSpanList) {
                    try {
                        final Span rootSpan = rootScopedSpan.span();
                        // Resolve URI and root span id per root: each exception in this root's
                        // subtree is attributed to the correct entry-point URI and linked back to
                        // the stored transaction (root SpanBo) via (transactionId, rootSpanId).
                        final Map<String, AttributeValue> rootAttributes =
                                OtlpTraceMapperUtils.getAttributeValueMap(rootSpan.getAttributesList());
                        final String rootUriTemplate = spanMapper.getServerSpanToRpc(rootSpan, rootAttributes);
                        final long rootSpanId = OtlpTraceMapperUtils.getSpanId(rootSpan.getSpanId());

                        final SpanBo spanBo = spanMapper.map(idAndName, rootSpan, rootScopedSpan.scope(), agentStartTime);

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

    Map<ByteString, List<ScopedSpan>> getSpanMap(List<ScopeSpans> scopeSpanList, OtlpTraceCollectorRejectedSpan rejectedSpan) {
        Map<ByteString, List<ScopedSpan>> spanMap = new HashMap<>();
        int unsampledCount = 0;
        int invalidIdCount = 0;
        for (ScopeSpans scopeSpan : scopeSpanList) {
            // The scope→span association only exists on this container — capture it here
            // (see ScopedSpan) so the mappers can emit the OPENTELEMETRY_SCOPE annotation.
            final InstrumentationScope scope = scopeSpan.getScope();
            List<Span> spansList = scopeSpan.getSpansList();
            for (Span span : spansList) {
                if (isUnsampled(span)) {
                    unsampledCount++;
                    continue;
                }
                // Reject malformed identifiers up front (OTel: trace ID = 16 bytes, span ID = 8 bytes,
                // neither all-zero). A present-but-invalid parentSpanId also rejects the span, so it is
                // never mis-classified as a root. Skipped here so the invalid ID never becomes a
                // storage key (transactionId/spanId) or a truncated-collision.
                if (!OtlpIdValidator.isValidTraceId(span.getTraceId())
                        || !OtlpIdValidator.isValidSpanId(span.getSpanId())
                        || !OtlpIdValidator.isValidParentSpanId(span.getParentSpanId())) {
                    invalidIdCount++;
                    continue;
                }
                spanMap.computeIfAbsent(span.getTraceId(), k -> new ArrayList<>()).add(new ScopedSpan(span, scope));
            }
        }
        if (unsampledCount > 0) {
            // Unsampled spans carry no complete trace and would skew ServerMap/response-time
            // stats. Reflected to the client via the rejected-span count (same policy as the
            // orphan-span drop); debug-level log only — a client-data shape issue, and dumping
            // every span would risk log flooding.
            rejectedSpan.putMessage("unsampled span (" + unsampledCount + ")");
            rejectedSpan.addCount(unsampledCount);
            logger.debug("Dropped unsampled spans count={}", unsampledCount);
        }
        if (invalidIdCount > 0) {
            // Same client-data reject policy as above: count into rejected_spans, debug log only.
            rejectedSpan.putMessage("invalid id (" + invalidIdCount + ")");
            rejectedSpan.addCount(invalidIdCount);
            logger.debug("Dropped spans with invalid trace/span id count={}", invalidIdCount);
        }
        return spanMap;
    }

    // Only the low trace-flags byte (bits 0-7, W3C sampled = bit 0) carries the sampling
    // decision. The is_remote metadata bits (bits 8-9) are set by modern SDKs on *every* span,
    // so the whole flags int being non-zero does NOT mean the trace-flags byte was populated —
    // reading it that way dropped spans that only carried is_remote metadata (e.g. a locust
    // load-generator's root spans, exported with is_remote set but the sampled bit clear).
    //
    // A trace-flags byte of 0 is ambiguous: an exporter predating the flags field
    // (opentelemetry-proto < 1.1), or one that populated only is_remote metadata, is
    // indistinguishable from an explicit all-clear, and there is no "has trace flags" validity
    // bit. Be conservative and keep the span. Only a trace-flags byte that is itself populated
    // (non-zero) yet has the sampled bit clear identifies a definitively unsampled span.
    static boolean isUnsampled(Span span) {
        final int traceFlags = span.getFlags() & SpanFlags.SPAN_FLAGS_TRACE_FLAGS_MASK_VALUE;
        if (traceFlags == 0) {
            return false;
        }
        return (traceFlags & W3C_SAMPLED_FLAG) == 0;
    }

    void initRootAndChild(List<ScopedSpan> spanList, List<ScopedSpan> rootSpanList, List<ScopedSpan> childSpanList) {
        for (ScopedSpan scopedSpan : spanList) {
            final Span span = scopedSpan.span();
            if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_SERVER_VALUE) {
                rootSpanList.add(scopedSpan);
            } else if (span.getKind().getNumber() == Span.SpanKind.SPAN_KIND_CONSUMER_VALUE) {
                rootSpanList.add(scopedSpan);
            } else {
                // client, producer, internal
                if (span.getParentSpanId().isEmpty()) {
                    // even the client type can be root if there is no parentSpanId value.
                    rootSpanList.add(scopedSpan);
                } else {
                    childSpanList.add(scopedSpan);
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
    void alignWrapperRoots(List<ScopedSpan> rootSpanList) {
        Map<ByteString, Span> rootById = new HashMap<>();
        for (ScopedSpan s : rootSpanList) {
            rootById.put(s.span().getSpanId(), s.span());
        }

        Map<ByteString, Long> minChildStartByParent = new HashMap<>();
        for (ScopedSpan scopedChild : rootSpanList) {
            Span child = scopedChild.span();
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
            ScopedSpan scopedSpan = rootSpanList.get(i);
            Span s = scopedSpan.span();
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
            // rebuild keeps the original scope — realignment must not drop the scope identity
            rootSpanList.set(i, new ScopedSpan(builder.build(), scopedSpan.scope()));
        }
    }

    List<SpanEventBo> findLinkSpan(long startTime, List<ScopedSpan> childSpanList, ByteString parentSpanId, int depth) {
        // Orphan/spanChunk traversal: no exception processing (no root span to link against) and
        // no exception-trace deep-link annotation — orphan spans have no stored exceptiontrace row
        // to link to (see recordException skip in the caller).
        return findLinkSpan(startTime, childSpanList, parentSpanId, depth, span -> {}, false);
    }

    List<SpanEventBo> findLinkSpan(long startTime, List<ScopedSpan> childSpanList, ByteString parentSpanId, int depth, Consumer<Span> spanConsumer) {
        // Root-linked traversal: exceptions are recorded to exception-trace here, so emit the
        // deep-link annotation on the exception-bearing SpanEvent.
        return findLinkSpan(startTime, childSpanList, parentSpanId, depth, spanConsumer, true);
    }

    private List<SpanEventBo> findLinkSpan(long startTime, List<ScopedSpan> childSpanList, ByteString parentSpanId, int depth,
                                           Consumer<Span> spanConsumer, boolean emitExceptionLink) {
        List<SpanEventBo> spanEventList = new ArrayList<>();
        if (depth > 99) {
            // defensive check
            return spanEventList;
        }

        // Collect matching spans and remove them from childSpanList in a single O(n) pass,
        // avoiding the O(n*m) cost of a separate removeAll call.
        List<ScopedSpan> linkSpanList = new ArrayList<>();
        Iterator<ScopedSpan> iterator = childSpanList.iterator();
        while (iterator.hasNext()) {
            ScopedSpan scopedSpan = iterator.next();
            if (parentSpanId.equals(scopedSpan.span().getParentSpanId())) {
                linkSpanList.add(scopedSpan);
                iterator.remove();
            }
        }

        for (ScopedSpan scopedSpan : linkSpanList) {
            final Span span = scopedSpan.span();
            spanConsumer.accept(span);
            final SpanEventBo spanEventBo = spanEventMapper.map(startTime, span, scopedSpan.scope(), depth);
            // Link the SpanEvent's inline exception to its exceptiontrace record. The id matches
            // OtlpExceptionMapper's ExceptionWrapperBo.exceptionId (getSpanId of the same span);
            // only emitted on the root-linked path where an exceptiontrace row actually exists.
            if (emitExceptionLink && exceptionInfoResolver.isExceptionClassCaptured(spanEventBo.getExceptionInfo())) {
                spanEventBo.addAnnotation(AnnotationBo.of(AnnotationKey.EXCEPTION_CHAIN_ID.getCode(),
                        OtlpTraceMapperUtils.getSpanId(span.getSpanId())));
            }
            spanEventList.add(spanEventBo);
            List<SpanEventBo> list = findLinkSpan(startTime, childSpanList, span.getSpanId(), depth + 1, spanConsumer, emitExceptionLink);
            spanEventList.addAll(list);

        }

        return spanEventList;
    }

    List<SpanChunkBo> findLinkSpanChunk(IdAndName idAndName, List<ScopedSpan> childSpanList) {
        // Collect all spanIds for quick lookup
        List<SpanChunkBo> spanChunkList = new ArrayList<>();
        Set<ByteString> spanIdSet = new HashSet<>();
        for (ScopedSpan s : childSpanList) {
            spanIdSet.add(s.span().getSpanId());
        }
        // Identify local roots: parentSpanId is empty or not present in current set
        List<ScopedSpan> localRootSpanList = new ArrayList<>();
        for (ScopedSpan s : childSpanList) {
            if (!spanIdSet.contains(s.span().getParentSpanId())) {
                localRootSpanList.add(s);
            }
        }
        // If no local root (possible cycle), pick one arbitrarily
        if (localRootSpanList.isEmpty()) {
            localRootSpanList.addAll(childSpanList);
        }
        // Remove all local roots at once in O(n) using Set, instead of O(n*k) repeated remove calls.
        Set<ScopedSpan> localRootSet = new HashSet<>(localRootSpanList);
        childSpanList.removeIf(localRootSet::contains);

        for (ScopedSpan localRootScopedSpan : localRootSpanList) {
            final Span localRootSpan = localRootScopedSpan.span();
            // Map root as SpanChunk (attached to its parentSpanId if present)
            SpanChunkBo spanChunkBo = spanChunkMapper.map(idAndName, localRootSpan, localRootScopedSpan.scope());
            long rootStartTime = localRootSpan.getStartTimeUnixNano();
            // Recursively attach children as events
            List<SpanEventBo> childrenEvents = findLinkSpan(rootStartTime, childSpanList, localRootSpan.getSpanId(), 2);
            spanChunkBo.addSpanEventBoList(childrenEvents);
            spanChunkList.add(spanChunkBo);
        }

        return spanChunkList;
    }
}
