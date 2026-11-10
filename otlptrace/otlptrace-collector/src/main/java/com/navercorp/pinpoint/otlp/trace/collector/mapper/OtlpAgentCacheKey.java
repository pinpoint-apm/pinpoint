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

import com.navercorp.pinpoint.common.server.uid.ServiceUidSupplier;

import java.util.Objects;

/**
 * Keys of the AgentInfo dedup cache ({@code otlpAgentIdCache}). The trace and the metric paths share
 * the cache but each has its own key space (prefix), so neither suppresses the other's registration.
 * <ul>
 *   <li>trace: {@code t:serviceUid/agentId}, one registration per agent for the collector's lifetime.
 *   The trace path's start time is the span start time unless the Resource carries
 *   {@code process.creation.time}; that approximation changes from span to span, so it cannot tell a
 *   restart from the next batch and is left out.</li>
 *   <li>metric: {@code m:serviceUid/agentId@startTime}, one registration per process session. Every
 *   start time the metric path resolves is fixed for the life of the process, so a new start time is a
 *   restart under the same agentId (a fixed {@code pinpoint.agentId}, a container restarted in the same
 *   pod) and registers the new session. An unknown start time is {@code @0}: one registration per
 *   agent.</li>
 * </ul>
 */
public final class OtlpAgentCacheKey {

    private static final String TRACE_PREFIX = "t:";
    private static final String METRIC_PREFIX = "m:";
    private static final char SERVICE_SEPARATOR = '/';
    private static final char START_TIME_SEPARATOR = '@';

    private OtlpAgentCacheKey() {
    }

    /** Trace path key: one registration per (serviceUid, agentId). */
    public static String trace(ServiceUidSupplier serviceUid, String agentId) {
        return TRACE_PREFIX + agent(serviceUid, agentId);
    }

    /**
     * Metric path key: one registration per process session.
     *
     * @param processStartTime epoch millis of the process start; 0 or less means unknown and gives
     *                         {@code @0}, one registration per agent
     */
    public static String metric(ServiceUidSupplier serviceUid, String agentId, long processStartTime) {
        final long startTime = Math.max(processStartTime, 0L);
        return METRIC_PREFIX + agent(serviceUid, agentId) + START_TIME_SEPARATOR + startTime;
    }

    private static String agent(ServiceUidSupplier serviceUid, String agentId) {
        Objects.requireNonNull(serviceUid, "serviceUid");
        // The supplier is already resolved (FixedServiceUid) by OtlpResourceIdResolver; get() is a field read.
        return serviceUid.get().getUid() + String.valueOf(SERVICE_SEPARATOR) + agentId;
    }
}
