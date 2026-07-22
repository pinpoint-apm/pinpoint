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

import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import com.navercorp.pinpoint.otlp.trace.collector.util.AttributeUtils;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Resolves the agentStartTime session field from the {@code process.creation.time} resource
 * attribute — the OTel counterpart of the native agent's {@code RuntimeMXBean.getStartTime()}.
 *
 * <p>Stateless by design: the value is derived from the resource attributes alone (no cache,
 * storage, or collector-local clock), so any collector instance resolves the same value at any
 * time. When the attribute is absent or unusable, {@link #UNSET} is returned and the caller keeps
 * the pre-existing per-span approximation (span start time), so deployments that do not set the
 * attribute see no behavior change.</p>
 */
@Component
public class OtlpAgentStartTimeResolver {

    // "not provided" sentinel (same convention as the parentSpanId -1). Cannot collide with a
    // resolved value: the range validation below only admits large positive epoch millis.
    public static final long UNSET = -1;

    private static final String RESOLVED_METRIC = "collector.otlptrace.agent-start-time.resolved";

    // process.creation.time is semconv Development stage — validate before trusting. Values
    // outside [2000-01-01, now + 5min] (epoch 0, exporter clock far in the future) would corrupt
    // the AGENTINFO session rowkey, so they are discarded rather than clamped.
    private static final long MIN_VALID_EPOCH_MILLIS = 946684800000L; // 2000-01-01T00:00:00Z
    private static final long MAX_FUTURE_SKEW_MILLIS = TimeUnit.MINUTES.toMillis(5);

    private final Logger logger = LogManager.getLogger(this.getClass());
    // Throttled so a fleet-wide misconfigured exporter (every batch failing to parse) does not
    // flood the log; the true failure rate stays observable via the parse-error counter.
    private final ThrottledLogger throttledLogger = ThrottledLogger.getLogger(logger, 100);

    private final Counter creationTimeCounter;
    private final Counter spanTimeCounter;
    private final Counter parseErrorCounter;

    public OtlpAgentStartTimeResolver(MeterRegistry meterRegistry) {
        Objects.requireNonNull(meterRegistry, "meterRegistry");
        // A high span-time ratio signals fleets that should be guided to set
        // process.creation.time (session semantics are approximated without it).
        this.creationTimeCounter = resolvedCounter(meterRegistry, "creation-time");
        this.spanTimeCounter = resolvedCounter(meterRegistry, "span-time");
        this.parseErrorCounter = Counter.builder("collector.otlptrace.agent-start-time.parse-error")
                .description("OTLP trace process.creation.time values discarded (unparsable or out of range)")
                .register(meterRegistry);
    }

    private static Counter resolvedCounter(MeterRegistry meterRegistry, String source) {
        return Counter.builder(RESOLVED_METRIC)
                .description("OTLP trace agentStartTime resolutions by source")
                .tag("source", source)
                .register(meterRegistry);
    }

    /**
     * @return the process start time in epoch millis, or {@link #UNSET} when the attribute is
     * absent or unusable (caller falls back to the span start time)
     */
    public long resolve(Map<String, AttributeValue> resourceAttributeMap) {
        final String creationTime = AttributeUtils.getAttributeStringValue(
                resourceAttributeMap, OtlpTraceConstants.ATTRIBUTE_KEY_PROCESS_CREATION_TIME, null);
        if (creationTime == null) {
            spanTimeCounter.increment();
            return UNSET;
        }
        final long epochMillis;
        try {
            epochMillis = OffsetDateTime.parse(creationTime).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            discard(creationTime);
            return UNSET;
        }
        if (epochMillis < MIN_VALID_EPOCH_MILLIS
                || epochMillis > System.currentTimeMillis() + MAX_FUTURE_SKEW_MILLIS) {
            discard(creationTime);
            return UNSET;
        }
        creationTimeCounter.increment();
        return epochMillis;
    }

    private void discard(String creationTime) {
        parseErrorCounter.increment();
        spanTimeCounter.increment();
        throttledLogger.warn("Discarded invalid process.creation.time={}", creationTime);
    }
}
