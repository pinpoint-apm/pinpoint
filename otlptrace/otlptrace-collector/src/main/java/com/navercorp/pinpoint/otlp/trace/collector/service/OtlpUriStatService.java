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

package com.navercorp.pinpoint.otlp.trace.collector.service;

import com.navercorp.pinpoint.common.trace.UriStatHistogramBucket;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpUriStatSpan;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import com.navercorp.pinpoint.uristat.collector.dao.UriStatDao;
import com.navercorp.pinpoint.uristat.collector.model.UriStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds URI stat records from OTLP entry-point spans (Option B).
 * <p>
 * Unlike the native agent, which pre-aggregates over a 30s window before sending, OTLP spans arrive
 * raw. Rather than emitting one record per span, this aggregates the spans of a single export
 * request by (service, application, agent, uri, 30s window) so the number of Kafka/Pinot records
 * stays close to the agent's volume. The web side re-aggregates with SUM/MAX at query time, so
 * batch-level pre-aggregation is mathematically equivalent to per-span records.
 * <p>
 * Gated by two flags (AND), mirroring the native split between the shared uristat storage module
 * and its per-source ingest toggle ({@code collector.stat.uri}):
 * <ul>
 *   <li>{@code pinpoint.modules.collector.uristat.enabled} — the shared storage module that provides
 *       the {@link UriStatDao} and {@link TenantProvider} beans this service requires.</li>
 *   <li>{@code pinpoint.collector.otlptrace.uristat.enabled} — the OTLP-specific switch, off by
 *       default, so enabling native uristat does not silently turn on OTLP feeding.</li>
 * </ul>
 * If only the OTLP flag is set without the storage module, the AND fails and the bean is simply not
 * created (no startup failure from the missing {@link UriStatDao}).
 */
/**
 * Registered as an explicit @Bean in OtlpTraceCollectorModule rather than component-scanned:
 * its enable flags live in the profile property files, which are contributed by an imported
 * @PropertySources class — component-scan conditions are evaluated before those imports are
 * processed, so a scanned @ConditionalOnProperty could never see the flags.
 */
public class OtlpUriStatService {

    // Aligns with the agent's TickClock window and the web's default query granularity (30s), so
    // truncating span timestamps to this window collapses records without changing chart buckets.
    static final long WINDOW_SIZE_MS = 30_000L;

    private static final int BUCKET_SIZE = 8;

    private final UriStatDao uriStatDao;
    private final TenantProvider tenantProvider;
    private final int version = UriStatHistogramBucket.getLayout().getBucketVersion();

    public OtlpUriStatService(UriStatDao uriStatDao, TenantProvider tenantProvider) {
        this.uriStatDao = Objects.requireNonNull(uriStatDao, "uriStatDao");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    public void store(List<OtlpUriStatSpan> uriStatSpanList) {
        if (uriStatSpanList.isEmpty()) {
            return;
        }
        final String tenantId = tenantProvider.getTenantId();

        final Map<UriStatKey, UriStatAccumulator> accumulatorMap = new HashMap<>();
        for (OtlpUriStatSpan span : uriStatSpanList) {
            final long window = (span.getStartTime() / WINDOW_SIZE_MS) * WINDOW_SIZE_MS;
            final UriStatKey key = new UriStatKey(span.getServiceName(), span.getApplicationName(),
                    span.getAgentId(), span.getUri(), window);
            accumulatorMap.computeIfAbsent(key, k -> new UriStatAccumulator())
                    .add(span.getElapsed(), span.isError());
        }

        final List<UriStat> data = new ArrayList<>(accumulatorMap.size());
        for (Map.Entry<UriStatKey, UriStatAccumulator> entry : accumulatorMap.entrySet()) {
            final UriStatKey key = entry.getKey();
            final UriStatAccumulator acc = entry.getValue();
            data.add(new UriStat(key.window, tenantId, key.serviceName, key.applicationName, key.agentId, key.uri,
                    acc.maxLatencyMs, acc.totalTimeMs,
                    toList(acc.totalHistogram), toList(acc.failureHistogram),
                    version));
        }
        uriStatDao.insert(data);
    }

    private static List<Integer> toList(int[] histogram) {
        final List<Integer> list = new ArrayList<>(histogram.length);
        for (int value : histogram) {
            list.add(value);
        }
        return list;
    }

    private static final class UriStatKey {
        private final String serviceName;
        private final String applicationName;
        private final String agentId;
        private final String uri;
        private final long window;

        private UriStatKey(String serviceName, String applicationName, String agentId, String uri, long window) {
            this.serviceName = serviceName;
            this.applicationName = applicationName;
            this.agentId = agentId;
            this.uri = uri;
            this.window = window;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UriStatKey that = (UriStatKey) o;
            return window == that.window
                    && Objects.equals(serviceName, that.serviceName)
                    && Objects.equals(applicationName, that.applicationName)
                    && Objects.equals(agentId, that.agentId)
                    && Objects.equals(uri, that.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceName, applicationName, agentId, uri, window);
        }
    }

    private static final class UriStatAccumulator {
        private final int[] totalHistogram = new int[BUCKET_SIZE];
        private final int[] failureHistogram = new int[BUCKET_SIZE];
        private long totalTimeMs;
        private long maxLatencyMs;

        private void add(int elapsed, boolean error) {
            final int index = UriStatHistogramBucket.getLayout().getBucket(elapsed).getIndex();
            totalHistogram[index]++;
            if (error) {
                failureHistogram[index]++;
            }
            totalTimeMs += elapsed;
            if (elapsed > maxLatencyMs) {
                maxLatencyMs = elapsed;
            }
        }
    }
}
