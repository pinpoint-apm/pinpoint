/*
 * Copyright 2026 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.bo.stat.AgentUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.EachUriStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.UriStatHistogram;
import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates OTel root (entry-point) spans into per-URI response-time statistics
 * ({@link AgentUriStatBo} / {@link EachUriStatBo}), mirroring what the native agent's
 * uri-stat collection produces and the collector persists via {@code AgentUriStatService}.
 *
 * <p>Unlike the exception path (one Bo per span), uri-stat is inherently an aggregate: many
 * requests to the same URI in the same collection window roll up into a single {@link EachUriStatBo}
 * with a response-time histogram. Aggregation is therefore driven through a per-export
 * {@link Aggregator} (obtained via {@link #newAggregator()}) rather than a stateless map() call —
 * the mapper itself stays a singleton and only carries configuration.
 *
 * <p>Root spans are grouped by (serviceName, applicationName, agentId) into one {@link AgentUriStatBo}
 * each, and within that by (uri, collection-window timestamp) into one {@link EachUriStatBo}. The
 * window is {@code startTime} floored to {@code collectIntervalMs}, matching the native agent which
 * emits one row per URI per collection interval.
 */
@Component
public class OtlpUriStatMapper {

    // The native gRPC uri-stat path has no service name and hardcodes "DEFAULT"; used here only as a
    // fallback since AgentUriStatBo requires a non-empty serviceName (OTel usually carries a real one).
    private static final String DEFAULT_SERVICE_NAME = "DEFAULT";

    // Collection window: root spans are bucketed by (startTime floored to this interval) so requests
    // in the same window aggregate into a single per-URI histogram row, mirroring the agent's
    // interval-based uri-stat collection. Downstream (Pinot) groups by this timestamp.
    private final long collectIntervalMs;

    public OtlpUriStatMapper(
            @Value("${pinpoint.collector.otlptrace.uristat.collect-interval-ms:30000}") long collectIntervalMs) {
        if (collectIntervalMs < 1) {
            throw new IllegalArgumentException("collectIntervalMs must be >= 1: " + collectIntervalMs);
        }
        this.collectIntervalMs = collectIntervalMs;
    }

    /**
     * Creates a fresh, single-threaded accumulator for one export batch. Call
     * {@link Aggregator#add} per root span and {@link Aggregator#build()} once at the end.
     */
    public Aggregator newAggregator() {
        return new Aggregator(collectIntervalMs);
    }

    public static final class Aggregator {

        private final long collectIntervalMs;
        private final UriStatHistogramBucket.Layout layout = UriStatHistogramBucket.getLayout();
        // LinkedHashMap keeps output order stable (agents/URIs in first-seen order) for predictable
        // downstream inserts and tests. This accumulator is used within a single map() call and is
        // not shared across threads.
        private final Map<AgentKey, Map<UriTimeKey, Accum>> byAgent = new LinkedHashMap<>();

        private Aggregator(long collectIntervalMs) {
            this.collectIntervalMs = collectIntervalMs;
        }

        /**
         * Records one entry-point (root span) request against its URI's histogram. No-op when the
         * URI is empty (nothing meaningful to key the stat on).
         */
        public void add(IdAndName idAndName, String uri, int elapsedMs, boolean error, long startTimeMillis) {
            if (!StringUtils.hasLength(uri)) {
                return;
            }
            final int elapsed = Math.max(0, elapsedMs);
            final long bucketTimestamp = startTimeMillis - Math.floorMod(startTimeMillis, collectIntervalMs);

            final AgentKey agentKey = new AgentKey(serviceName(idAndName), idAndName.applicationName(), idAndName.agentId());
            final UriTimeKey uriKey = new UriTimeKey(uri, bucketTimestamp);
            final Accum accum = byAgent.computeIfAbsent(agentKey, k -> new LinkedHashMap<>())
                    .computeIfAbsent(uriKey, k -> new Accum());

            final int slot = layout.getBucket(elapsed).getIndex();
            accum.totalCounts[slot]++;
            accum.totalTimeMs += elapsed;
            accum.maxMs = Math.max(accum.maxMs, elapsed);
            if (error) {
                accum.failedCounts[slot]++;
                accum.failedTimeMs += elapsed;
                accum.failedMaxMs = Math.max(accum.failedMaxMs, elapsed);
                accum.failedCount++;
            }
        }

        /**
         * Materializes the accumulated stats into one {@link AgentUriStatBo} per agent. The failed
         * histogram is left {@code null} when a URI had no failures (matching the native path, which
         * the collector treats as an empty 8-slot histogram downstream).
         */
        public List<AgentUriStatBo> build() {
            final byte bucketVersion = layout.getBucketVersion();
            final List<AgentUriStatBo> result = new ArrayList<>(byAgent.size());
            for (Map.Entry<AgentKey, Map<UriTimeKey, Accum>> agentEntry : byAgent.entrySet()) {
                final AgentKey agentKey = agentEntry.getKey();
                final List<EachUriStatBo> eachList = new ArrayList<>(agentEntry.getValue().size());
                for (Map.Entry<UriTimeKey, Accum> uriEntry : agentEntry.getValue().entrySet()) {
                    final UriTimeKey uriKey = uriEntry.getKey();
                    final Accum accum = uriEntry.getValue();
                    final UriStatHistogram total = new UriStatHistogram(accum.totalTimeMs, accum.maxMs, toList(accum.totalCounts));
                    final UriStatHistogram failed = accum.failedCount > 0
                            ? new UriStatHistogram(accum.failedTimeMs, accum.failedMaxMs, toList(accum.failedCounts))
                            : null;
                    eachList.add(new EachUriStatBo(uriKey.timestamp(), uriKey.uri(), total, failed));
                }
                result.add(new AgentUriStatBo(bucketVersion, agentKey.serviceName(), agentKey.applicationName(),
                        agentKey.agentId(), eachList));
            }
            return result;
        }

        private static String serviceName(IdAndName idAndName) {
            return StringUtils.hasLength(idAndName.serviceName()) ? idAndName.serviceName() : DEFAULT_SERVICE_NAME;
        }

        private static List<Integer> toList(int[] counts) {
            final List<Integer> list = new ArrayList<>(counts.length);
            for (int count : counts) {
                list.add(count);
            }
            return list;
        }
    }

    private record AgentKey(String serviceName, String applicationName, String agentId) {
    }

    private record UriTimeKey(String uri, long timestamp) {
    }

    private static final class Accum {
        private final int[] totalCounts = new int[UriStatHistogram.URI_HISTOGRAM_SLOT];
        private final int[] failedCounts = new int[UriStatHistogram.URI_HISTOGRAM_SLOT];
        private long totalTimeMs;
        private long maxMs;
        private long failedTimeMs;
        private long failedMaxMs;
        private int failedCount;
    }
}
