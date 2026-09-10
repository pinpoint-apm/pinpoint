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

import com.google.protobuf.ByteString;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.kv;
import static com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpAnyValueFactory.strVal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The type / uriTemplate bounds added on top of the message / frame caps covered by OtlpExceptionMapperTest. */
class OtlpExceptionMapperLimitsTest {

    private static final byte[] TRACE_ID = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    private static final byte[] SPAN_ID = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final int TYPE_MAX = 32;
    private static final int URI_MAX = 16;

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpExceptionMapper mapper = new OtlpExceptionMapper(2048, TYPE_MAX, URI_MAX, 256, 2048, 262144, 4096, registry);

    private static Span span(String type) {
        return Span.newBuilder().setName("op")
                .setTraceId(ByteString.copyFrom(TRACE_ID)).setSpanId(ByteString.copyFrom(SPAN_ID))
                .addEvents(Span.Event.newBuilder().setName("exception").setTimeUnixNano(1_000_000_000L)
                        .addAttributes(kv("exception.type", strVal(type))))
                .build();
    }

    private double truncated(String field) {
        return registry.get("collector.otlptrace.exception.truncated").tag("field", field).counter().count();
    }

    @Test
    void exceptionType_isCappedInBytes_deterministically() {
        String longType = "com.example." + "A".repeat(100);
        IdAndName id = new IdAndName("agent-1", "agent-1", "app-1", "svc");

        ExceptionMetaDataBo first = mapper.map(id, span(longType), 1L, "/x").orElseThrow();
        ExceptionMetaDataBo second = mapper.map(id, span(longType), 1L, "/x").orElseThrow();

        String stored = first.getExceptionWrapperBos().get(0).getExceptionClassName();
        assertThat(stored.getBytes(StandardCharsets.UTF_8)).hasSize(TYPE_MAX);
        assertThat(second.getExceptionWrapperBos().get(0).getExceptionClassName()).isEqualTo(stored);
        assertThat(truncated("type")).isEqualTo(2.0);
    }

    @Test
    void uriTemplate_isSanitizedAndCapped_neverNull() {
        IdAndName id = new IdAndName("agent-1", "agent-1", "app-1", "svc");

        assertThat(mapper.map(id, span("T"), 1L, "/users/{id}").orElseThrow().getUriTemplate()).isEqualTo("/users/{id}");
        assertThat(mapper.map(id, span("T"), 1L, "/login?token=SECRET").orElseThrow().getUriTemplate()).isEqualTo("/login");
        assertThat(mapper.map(id, span("T"), 1L, "/" + "x".repeat(100)).orElseThrow().getUriTemplate()).hasSize(URI_MAX);
        assertThat(mapper.map(id, span("T"), 1L, null).orElseThrow().getUriTemplate()).isEmpty();
        assertThat(mapper.map(id, span("T"), 1L, "").orElseThrow().getUriTemplate()).isEmpty();
        assertThat(truncated("uri_template")).isEqualTo(2.0);
    }

    @Test
    void byteCaps_mustBePositive() {
        assertThatThrownBy(() -> new OtlpExceptionMapper(2048, 0, URI_MAX, 256, 2048, 0, 0, registry)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new OtlpExceptionMapper(2048, TYPE_MAX, 0, 256, 2048, 0, 0, registry)).isInstanceOf(IllegalArgumentException.class);
        // Stack bounds may be <= 0 (unlimited).
        new OtlpExceptionMapper(2048, TYPE_MAX, URI_MAX, 0, 2048, 0, 0, registry);
    }
}
