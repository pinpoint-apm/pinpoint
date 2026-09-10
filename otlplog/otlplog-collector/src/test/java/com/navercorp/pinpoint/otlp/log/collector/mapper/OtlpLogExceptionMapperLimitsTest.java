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

package com.navercorp.pinpoint.otlp.log.collector.mapper;

import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.IdAndName;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpExceptionMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.logs.v1.LogRecord;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.SPAN_ID;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.exceptionRecord;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.kv;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/** The log path applies the same field bounds as the span-event path, counted under its own namespace. */
class OtlpLogExceptionMapperLimitsTest {

    private static final IdAndName ID = new IdAndName("agent-1", "agent-1", "app-1", "svc");
    private static final int TYPE_MAX = 32;
    private static final int URI_MAX = 16;

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpExceptionMapper traceMapper = new OtlpExceptionMapper(2048, 256, 2048, registry);
    private final OtlpLogExceptionMapper mapper = new OtlpLogExceptionMapper(traceMapper, 2048, TYPE_MAX, URI_MAX, registry, () -> 0L);

    private ExceptionMetaDataBo map(LogRecord record) {
        return mapper.map(new ExceptionLogCandidate(ID, "nodejs", record, false)).orElseThrow();
    }

    private double truncated(String field) {
        return registry.get("collector.otlplog.exception.truncated").tag("field", field).counter().count();
    }

    @Test
    void exceptionType_isCapped_andCountedUnderTheLogNamespace() {
        String longType = "Error" + "X".repeat(100);

        ExceptionMetaDataBo bo = map(exceptionRecord(SPAN_ID, longType, "m", null).build());

        assertThat(bo.getExceptionWrapperBos().get(0).getExceptionClassName().getBytes(StandardCharsets.UTF_8)).hasSize(TYPE_MAX);
        assertThat(truncated("type")).isEqualTo(1.0);
        // The trace mapper pre-registers its own series at zero; the log path must not have counted there.
        assertThat(registry.get("collector.otlptrace.exception.truncated").tag("field", "type").counter().count()).isZero();
    }

    @Test
    void uriTemplate_dropsQueryString_andCaps() {
        assertThat(map(exceptionRecord(SPAN_ID, "T", "m", null).addAttributes(kv("http.route", "/login?token=SECRET")).build()).getUriTemplate())
                .isEqualTo("/login");
        assertThat(map(exceptionRecord(SPAN_ID, "T", "m", null).addAttributes(kv("http.route", "/" + "x".repeat(100))).build()).getUriTemplate())
                .hasSize(URI_MAX);
        assertThat(truncated("uri_template")).isEqualTo(2.0);
    }

    @Test
    void hostileSingleLineStackTrace_isBoundedByTheSharedParserLimits() {
        String hostile = "at " + "x (".repeat(500_000);
        LogRecord record = exceptionRecord(SPAN_ID, "Error", "m", hostile).build();

        ExceptionMetaDataBo bo = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> map(record));

        assertThat(bo.getExceptionWrapperBos().get(0).getStackTraceElements()).hasSizeLessThanOrEqualTo(1);
        assertThat(registry.get("collector.otlptrace.exception.truncated").tag("field", "stacktrace_line").counter().count()).isEqualTo(1.0);
    }
}
