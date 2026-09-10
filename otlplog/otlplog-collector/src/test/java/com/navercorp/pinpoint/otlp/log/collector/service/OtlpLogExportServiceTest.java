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

package com.navercorp.pinpoint.otlp.log.collector.service;

import com.navercorp.pinpoint.collector.service.ExceptionMetaDataService;
import com.navercorp.pinpoint.common.server.bo.exception.ExceptionMetaDataBo;
import com.navercorp.pinpoint.otlp.log.collector.OtlpLogRejectReason;
import com.navercorp.pinpoint.otlp.log.collector.mapper.ExceptionLogCandidate;
import com.navercorp.pinpoint.otlp.log.collector.mapper.ExceptionLogSelector;
import com.navercorp.pinpoint.otlp.log.collector.mapper.OtlpLogExceptionMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpExceptionMapper;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.OTHER_SPAN_ID;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.SPAN_ID;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.exceptionRecord;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.kv;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.plainRecord;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.record;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.resource;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.resourceLogs;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.validResource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtlpLogExportServiceTest {

    private static final String TYPE = "java.lang.IllegalStateException";
    private static final String STACK = "java.lang.IllegalStateException: boom\n\tat com.example.App.run(App.java:10)\n";

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OtlpLogIngestMetrics metrics = new OtlpLogIngestMetrics(registry);
    private final ExceptionMetaDataService store = mock(ExceptionMetaDataService.class);
    private final OtlpLogExceptionMapper realMapper = new OtlpLogExceptionMapper(
            new OtlpExceptionMapper(2048, 256, 2048, registry), 2048,
            OtlpExceptionMapper.DEFAULT_TYPE_MAX_BYTES, OtlpExceptionMapper.DEFAULT_URI_TEMPLATE_MAX_BYTES, registry);

    private OtlpLogExportService service(boolean storeUnsampled) {
        return new OtlpLogExportService(new ExceptionLogSelector(List.of(), List.of()), realMapper,
                Optional.of(store), metrics, false, storeUnsampled);
    }

    private double dropped(OtlpLogRejectReason reason) {
        return registry.get(OtlpLogIngestMetrics.RECORD_DROPPED)
                .tag(OtlpLogIngestMetrics.TAG_TRANSPORT, "grpc")
                .tag(OtlpLogIngestMetrics.TAG_REASON, reason.tagValue())
                .counter().count();
    }

    private double counter(String name) {
        return registry.get(name).tag(OtlpLogIngestMetrics.TAG_TRANSPORT, "grpc").counter().count();
    }

    @Test
    void exceptionRecord_isStored_plainLogLines_areDroppedSilently() {
        ResourceLogs logs = resourceLogs(
                plainRecord(SPAN_ID, "started").build(),
                exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build(),
                plainRecord(SPAN_ID, "done").build());

        OtlpLogExportResult result = service(false).export(List.of(logs), OtlpTransport.GRPC);

        assertThat(result.stored()).isEqualTo(1);
        // Non-exception logs are the receiver's own choice: counted, not reported to the exporter.
        assertThat(result.rejected().count(OtlpLogRejectReason.NO_EXCEPTION)).isEqualTo(2);
        assertThat(result.rejected().clientVisibleCount()).isZero();
        assertThat(counter(OtlpLogIngestMetrics.RECORD_RECEIVED)).isEqualTo(3.0);
        assertThat(counter(OtlpLogIngestMetrics.RECORD_STORED)).isEqualTo(1.0);
        assertThat(dropped(OtlpLogRejectReason.NO_EXCEPTION)).isEqualTo(2.0);

        ArgumentCaptor<ExceptionMetaDataBo> captor = ArgumentCaptor.forClass(ExceptionMetaDataBo.class);
        verify(store).save(captor.capture());
        assertThat(captor.getValue().getApplicationName()).isEqualTo("app-1");
        assertThat(captor.getValue().getExceptionWrapperBos().get(0).getExceptionClassName()).isEqualTo(TYPE);
    }

    @Test
    void missingTraceContext_isDropped_andReportedAsRejected() {
        LogRecord noContext = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).clearTraceId().clearSpanId().build();
        LogRecord badSpanId = exceptionRecord(SPAN_ID, TYPE, "boom", STACK)
                .setSpanId(com.google.protobuf.ByteString.copyFrom(new byte[]{1, 2, 3})).build();

        OtlpLogExportResult result = service(false).export(List.of(resourceLogs(noContext, badSpanId)), OtlpTransport.GRPC);

        assertThat(result.stored()).isZero();
        assertThat(result.rejected().count(OtlpLogRejectReason.NO_TRACE_CONTEXT)).isEqualTo(2);
        assertThat(result.rejected().clientVisibleCount()).isEqualTo(2);
        assertThat(result.rejected().clientVisibleMessage()).isEqualTo("no trace context (2)");
        verify(store, never()).save(any());
    }

    @Test
    void unsampledTrace_isDroppedByDefault() {
        LogRecord unsampled = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).setFlags(0).build();

        OtlpLogExportResult result = service(false).export(List.of(resourceLogs(unsampled)), OtlpTransport.GRPC);

        assertThat(result.stored()).isZero();
        assertThat(result.rejected().count(OtlpLogRejectReason.UNSAMPLED_CONTEXT)).isEqualTo(1);
        // Dropped by configuration, not a client fault: no partial success.
        assertThat(result.rejected().clientVisibleCount()).isZero();
        assertThat(dropped(OtlpLogRejectReason.UNSAMPLED_CONTEXT)).isEqualTo(1.0);
        assertThat(counter(OtlpLogIngestMetrics.RECORD_UNSAMPLED_CONTEXT)).isZero();
        verify(store, never()).save(any());
    }

    @Test
    void unsampledTrace_isStoredAndCounted_whenEnabled() {
        LogRecord unsampled = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).setFlags(0).build();
        LogRecord sampled = exceptionRecord(OTHER_SPAN_ID, TYPE, "boom", STACK).build();

        OtlpLogExportResult result = service(true).export(List.of(resourceLogs(unsampled, sampled)), OtlpTransport.GRPC);

        assertThat(result.stored()).isEqualTo(2);
        assertThat(result.rejected().count(OtlpLogRejectReason.UNSAMPLED_CONTEXT)).isZero();
        assertThat(counter(OtlpLogIngestMetrics.RECORD_UNSAMPLED_CONTEXT)).isEqualTo(1.0);
        assertThat(counter(OtlpLogIngestMetrics.RECORD_STORED)).isEqualTo(2.0);
    }

    @Test
    void sampledBitOnly_decidesUnsampled_otherFlagBitsIgnored() {
        assertThat(OtlpLogExportService.isUnsampled(record(SPAN_ID).setFlags(0).build())).isTrue();
        assertThat(OtlpLogExportService.isUnsampled(record(SPAN_ID).setFlags(0x01).build())).isFalse();
        assertThat(OtlpLogExportService.isUnsampled(record(SPAN_ID).setFlags(0x02).build())).isTrue();
        assertThat(OtlpLogExportService.isUnsampled(record(SPAN_ID).setFlags(0x101).build())).isFalse();
    }

    @Test
    void duplicateWithinRequest_storedOnce_withTheStackTraceBearingRecord() {
        LogRecord appender = exceptionRecord(SPAN_ID, TYPE, "boom", null).build();
        LogRecord instrumentation = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build();

        OtlpLogExportResult result = service(false).export(List.of(resourceLogs(appender, instrumentation)), OtlpTransport.GRPC);

        assertThat(result.stored()).isEqualTo(1);
        assertThat(result.rejected().count(OtlpLogRejectReason.DUPLICATE)).isEqualTo(1);
        assertThat(result.rejected().clientVisibleCount()).isZero();
        ArgumentCaptor<ExceptionMetaDataBo> captor = ArgumentCaptor.forClass(ExceptionMetaDataBo.class);
        verify(store).save(captor.capture());
        assertThat(captor.getValue().getExceptionWrapperBos().get(0).getStackTraceElements()).isNotEmpty();
    }

    @Test
    void duplicatesAcrossResources_inOneRequest_areStillDeduplicated() {
        LogRecord a = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build();
        LogRecord b = exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build();

        OtlpLogExportResult result = service(false).export(List.of(resourceLogs(a), resourceLogs(b)), OtlpTransport.GRPC);

        assertThat(result.stored()).isEqualTo(1);
        assertThat(result.rejected().count(OtlpLogRejectReason.DUPLICATE)).isEqualTo(1);
    }

    @Test
    void invalidResource_dropsEveryRecordOfThatResource_asClientVisible() {
        // No application name at all -> getId throws.
        ResourceLogs bad = resourceLogs(resource(kv("host.name", "h1")), "scope",
                exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build(), plainRecord(SPAN_ID, "x").build());
        ResourceLogs good = resourceLogs(exceptionRecord(OTHER_SPAN_ID, TYPE, "boom", STACK).build());

        OtlpLogExportResult result = service(false).export(List.of(bad, good), OtlpTransport.GRPC);

        assertThat(result.stored()).isEqualTo(1);
        assertThat(result.rejected().count(OtlpLogRejectReason.INVALID_RESOURCE)).isEqualTo(2);
        assertThat(result.rejected().clientVisibleCount()).isEqualTo(2);
        assertThat(counter(OtlpLogIngestMetrics.RECORD_RECEIVED)).isEqualTo(3.0);
    }

    @Test
    void resourceWithoutInstanceId_isAcceptedOnlyWithTheApplicationNameFallback() {
        ResourceLogs logs = resourceLogs(resource(kv("service.name", "app-2")), "scope",
                exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build());

        OtlpLogExportService strict = new OtlpLogExportService(new ExceptionLogSelector(List.of(), List.of()), realMapper,
                Optional.of(store), metrics, false, false);
        OtlpLogExportService lenient = new OtlpLogExportService(new ExceptionLogSelector(List.of(), List.of()), realMapper,
                Optional.of(store), metrics, true, false);

        assertThat(strict.export(List.of(logs), OtlpTransport.GRPC).rejected().count(OtlpLogRejectReason.INVALID_RESOURCE)).isEqualTo(1);
        assertThat(lenient.export(List.of(logs), OtlpTransport.GRPC).stored()).isEqualTo(1);
    }

    @Test
    void blacklistedScope_isDroppedSilently() {
        OtlpLogExportService service = new OtlpLogExportService(
                new ExceptionLogSelector(List.of("io.opentelemetry.exporter"), List.of()), realMapper,
                Optional.of(store), metrics, false, false);
        ResourceLogs logs = resourceLogs(validResource(), "io.opentelemetry.exporter.internal.grpc",
                exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build());

        OtlpLogExportResult result = service.export(List.of(logs), OtlpTransport.GRPC);

        assertThat(result.stored()).isZero();
        assertThat(result.rejected().count(OtlpLogRejectReason.BLACKLISTED)).isEqualTo(1);
        assertThat(result.rejected().clientVisibleCount()).isZero();
    }

    @Test
    void exceptionAttributesWithoutAType_areRejectedAsClientVisible() {
        LogRecord noType = record(SPAN_ID, kv("exception.message", "boom")).build();

        OtlpLogExportResult result = service(false).export(List.of(resourceLogs(noType)), OtlpTransport.GRPC);

        assertThat(result.rejected().count(OtlpLogRejectReason.NO_EXCEPTION_TYPE)).isEqualTo(1);
        assertThat(result.rejected().clientVisibleMessage()).isEqualTo("no exception type (1)");
    }

    @Test
    void severalTypelessRecordsInOneSpan_areEachRejected_notFoldedIntoOneDuplicate() {
        // Same span, no resolvable type on any of them: the exporter must see three rejections, and
        // none of them may be counted as a duplicate of the others.
        ResourceLogs logs = resourceLogs(
                record(SPAN_ID, kv("exception.message", "a")).build(),
                record(SPAN_ID, kv("exception.message", "b")).build(),
                record(SPAN_ID, kv("exception.type", ""), kv("error.type", ""), kv("exception.message", "c")).build());

        OtlpLogExportResult result = service(false).export(List.of(logs), OtlpTransport.GRPC);

        assertThat(result.stored()).isZero();
        assertThat(result.rejected().count(OtlpLogRejectReason.NO_EXCEPTION_TYPE)).isEqualTo(3);
        assertThat(result.rejected().count(OtlpLogRejectReason.DUPLICATE)).isZero();
        assertThat(result.rejected().clientVisibleCount()).isEqualTo(3);
        verify(store, never()).save(any());
    }

    @Test
    void emptyExceptionTypeWithErrorType_isStoredOnce_withTheExceptionTypeTwin() {
        // The mapper stores both under "E" (empty exception.type falls back to error.type), so the
        // deduplicator must key them the same way.
        ResourceLogs logs = resourceLogs(
                record(SPAN_ID, kv("exception.type", ""), kv("error.type", "E"), kv("exception.message", "m")).build(),
                record(SPAN_ID, kv("exception.type", "E"), kv("exception.message", "m"), kv("exception.stacktrace", STACK)).build());

        OtlpLogExportResult result = service(false).export(List.of(logs), OtlpTransport.GRPC);

        assertThat(result.stored()).isEqualTo(1);
        assertThat(result.rejected().count(OtlpLogRejectReason.DUPLICATE)).isEqualTo(1);
        assertThat(result.rejected().count(OtlpLogRejectReason.NO_EXCEPTION_TYPE)).isZero();
        ArgumentCaptor<ExceptionMetaDataBo> captor = ArgumentCaptor.forClass(ExceptionMetaDataBo.class);
        verify(store).save(captor.capture());
        assertThat(captor.getValue().getExceptionWrapperBos().get(0).getExceptionClassName()).isEqualTo("E");
    }

    @Test
    void mapperFailure_isIsolatedPerRecord_andReported() {
        OtlpLogExceptionMapper failing = mock(OtlpLogExceptionMapper.class);
        when(failing.map(any(ExceptionLogCandidate.class))).thenThrow(new IllegalStateException("bad record"));
        OtlpLogExportService service = new OtlpLogExportService(new ExceptionLogSelector(List.of(), List.of()), failing,
                Optional.of(store), metrics, false, false);

        OtlpLogExportResult result = service.export(List.of(resourceLogs(exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build())), OtlpTransport.GRPC);

        assertThat(result.rejected().count(OtlpLogRejectReason.MAPPING_ERROR)).isEqualTo(1);
        assertThat(result.rejected().clientVisibleCount()).isEqualTo(1);
        assertThat(dropped(OtlpLogRejectReason.MAPPING_ERROR)).isEqualTo(1.0);
    }

    @Test
    void storeFailure_isCounted_notReportedToTheExporter() {
        doThrow(new RuntimeException("kafka down")).when(store).save(any());

        OtlpLogExportResult result = service(false).export(List.of(resourceLogs(exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build())), OtlpTransport.GRPC);

        assertThat(result.stored()).isZero();
        assertThat(result.rejected().clientVisibleCount()).isZero();
        assertThat(registry.get(OtlpLogIngestMetrics.STORE_ERROR).counter().count()).isEqualTo(1.0);
    }

    @Test
    void noStorageModule_dropsAsStorageUnavailable_silently() {
        OtlpLogExportService service = new OtlpLogExportService(new ExceptionLogSelector(List.of(), List.of()), realMapper,
                Optional.empty(), metrics, false, false);

        OtlpLogExportResult result = service.export(List.of(resourceLogs(exceptionRecord(SPAN_ID, TYPE, "boom", STACK).build())), OtlpTransport.GRPC);

        assertThat(result.stored()).isZero();
        assertThat(result.rejected().count(OtlpLogRejectReason.STORAGE_UNAVAILABLE)).isEqualTo(1);
        assertThat(result.rejected().clientVisibleCount()).isZero();
        assertThat(dropped(OtlpLogRejectReason.STORAGE_UNAVAILABLE)).isEqualTo(1.0);
    }

    @Test
    void emptyRequest_isACleanSuccess() {
        OtlpLogExportResult result = service(false).export(List.of(), OtlpTransport.HTTP);

        assertThat(result.stored()).isZero();
        assertThat(result.rejected().countByReason()).isEmpty();
    }
}
