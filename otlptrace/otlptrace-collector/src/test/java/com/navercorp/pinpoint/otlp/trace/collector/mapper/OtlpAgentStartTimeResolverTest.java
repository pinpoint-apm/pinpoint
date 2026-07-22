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

import com.navercorp.pinpoint.common.trace.attribute.AttributeValue;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.intVal;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;

class OtlpAgentStartTimeResolverTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final OtlpAgentStartTimeResolver resolver = new OtlpAgentStartTimeResolver(meterRegistry);

    private static Map<String, AttributeValue> attributes(io.opentelemetry.proto.common.v1.KeyValue... kvs) {
        return OtlpTraceMapperUtils.getAttributeValueMap(List.of(kvs));
    }

    private double count(String name, String... tags) {
        return meterRegistry.counter(name, tags).count();
    }

    @Test
    void resolve_utc() {
        long resolved = resolver.resolve(attributes(
                kv("process.creation.time", strVal("2026-07-01T00:00:00Z"))));

        assertThat(resolved).isEqualTo(1782864000000L);
        assertThat(count("collector.otlptrace.agent-start-time.resolved", "source", "creation-time")).isEqualTo(1);
    }

    @Test
    void resolve_zoneOffset() {
        // 2026-07-01T09:00:00+09:00 == 2026-07-01T00:00:00Z
        long resolved = resolver.resolve(attributes(
                kv("process.creation.time", strVal("2026-07-01T09:00:00+09:00"))));

        assertThat(resolved).isEqualTo(1782864000000L);
    }

    @Test
    void resolve_fractionalSeconds() {
        long resolved = resolver.resolve(attributes(
                kv("process.creation.time", strVal("2026-07-01T00:00:00.588Z"))));

        assertThat(resolved).isEqualTo(1782864000588L);
    }

    @Test
    void resolve_absent_returnsUnset() {
        long resolved = resolver.resolve(attributes(kv("host.name", strVal("host-1"))));

        assertThat(resolved).isEqualTo(OtlpAgentStartTimeResolver.UNSET);
        assertThat(count("collector.otlptrace.agent-start-time.resolved", "source", "span-time")).isEqualTo(1);
        assertThat(count("collector.otlptrace.agent-start-time.parse-error")).isZero();
    }

    @Test
    void resolve_nonStringValue_returnsUnset() {
        // an int-typed attribute is not the semconv shape (string ISO 8601) — treated as absent
        long resolved = resolver.resolve(attributes(
                kv("process.creation.time", intVal(1782864000000L))));

        assertThat(resolved).isEqualTo(OtlpAgentStartTimeResolver.UNSET);
    }

    @Test
    void resolve_unparsable_returnsUnset_andCountsParseError() {
        long resolved = resolver.resolve(attributes(
                kv("process.creation.time", strVal("not-a-timestamp"))));

        assertThat(resolved).isEqualTo(OtlpAgentStartTimeResolver.UNSET);
        assertThat(count("collector.otlptrace.agent-start-time.parse-error")).isEqualTo(1);
        // discarded values fall back to the span-time source
        assertThat(count("collector.otlptrace.agent-start-time.resolved", "source", "span-time")).isEqualTo(1);
    }

    @Test
    void resolve_beforeYear2000_discarded() {
        long resolved = resolver.resolve(attributes(
                kv("process.creation.time", strVal("1999-12-31T23:59:59Z"))));

        assertThat(resolved).isEqualTo(OtlpAgentStartTimeResolver.UNSET);
        assertThat(count("collector.otlptrace.agent-start-time.parse-error")).isEqualTo(1);
    }

    @Test
    void resolve_farFuture_discarded() {
        String tenMinutesAhead = OffsetDateTime.ofInstant(
                Instant.now().plusSeconds(600), ZoneOffset.UTC).toString();

        long resolved = resolver.resolve(attributes(
                kv("process.creation.time", strVal(tenMinutesAhead))));

        assertThat(resolved).isEqualTo(OtlpAgentStartTimeResolver.UNSET);
        assertThat(count("collector.otlptrace.agent-start-time.parse-error")).isEqualTo(1);
    }

    @Test
    void resolve_recentPast_accepted() {
        String oneMinuteAgo = OffsetDateTime.ofInstant(
                Instant.now().minusSeconds(60), ZoneOffset.UTC).toString();

        long resolved = resolver.resolve(attributes(
                kv("process.creation.time", strVal(oneMinuteAgo))));

        assertThat(resolved).isNotEqualTo(OtlpAgentStartTimeResolver.UNSET);
    }
}
