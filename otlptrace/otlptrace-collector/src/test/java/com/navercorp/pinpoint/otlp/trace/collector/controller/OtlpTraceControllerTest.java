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

package com.navercorp.pinpoint.otlp.trace.collector.controller;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.rpc.Code;
import com.google.rpc.Status;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorRejectedSpan;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceRejectReason;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportResult;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportService;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceIngestMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OtlpTraceControllerTest {

    private static final String TRACE_ID_HEX = "0102030405060708090a0b0c0d0e0f10";
    private static final String SPAN_ID_HEX = "1112131415161718";

    private static final String JSON_REQUEST = "{\"resourceSpans\":[{\"scopeSpans\":[{\"spans\":[" +
            "{\"traceId\":\"" + TRACE_ID_HEX + "\",\"spanId\":\"" + SPAN_ID_HEX + "\",\"name\":\"op\"}]}]}]}";

    private OtlpTraceExportService exportService;
    private SimpleMeterRegistry meterRegistry;
    private OtlpTraceIngestMetrics ingestMetrics;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        exportService = mock(OtlpTraceExportService.class);
        meterRegistry = new SimpleMeterRegistry();
        ingestMetrics = new OtlpTraceIngestMetrics(meterRegistry);
        OtlpTraceController controller = new OtlpTraceController(exportService, ingestMetrics);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private static OtlpTraceExportResult successResult() {
        return new OtlpTraceExportResult(new OtlpTraceCollectorRejectedSpan(), 0, "");
    }

    private static OtlpTraceExportResult partialSuccessResult() {
        OtlpTraceCollectorRejectedSpan rejected = new OtlpTraceCollectorRejectedSpan();
        rejected.addCount(OtlpTraceRejectReason.INVALID_ID, 5);
        rejected.putMessage("invalid span");
        return new OtlpTraceExportResult(rejected, 0, "");
    }

    private static OtlpTraceExportResult serverErrorResult() {
        return new OtlpTraceExportResult(new OtlpTraceCollectorRejectedSpan(), 2, "insert failed");
    }

    private static <T extends Message.Builder> T parseJson(String json, T builder) throws Exception {
        JsonFormat.parser().merge(json, builder);
        return builder;
    }

    private static byte[] protobufRequest() {
        return ExportTraceServiceRequest.newBuilder()
                .addResourceSpans(ResourceSpans.newBuilder()
                        .addScopeSpans(ScopeSpans.newBuilder()
                                .addSpans(Span.newBuilder()
                                        .setTraceId(ByteString.copyFrom(HexFormat.of().parseHex(TRACE_ID_HEX)))
                                        .setSpanId(ByteString.copyFrom(HexFormat.of().parseHex(SPAN_ID_HEX)))
                                        .setName("op"))))
                .build()
                .toByteArray();
    }

    @Test
    void protobuf_success() throws Exception {
        when(exportService.export(anyList(), any())).thenReturn(successResult());

        MockHttpServletResponse response = mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_PROTOBUF)
                        .content(protobufRequest()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PROTOBUF))
                .andReturn().getResponse();

        // Empty ExportTraceServiceResponse serializes to zero bytes.
        assertThat(response.getContentAsByteArray()).isEmpty();
    }

    @Test
    void json_success() throws Exception {
        when(exportService.export(anyList(), any())).thenReturn(successResult());

        mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        // The hex IDs must arrive at the export service as the same raw bytes the protobuf path yields.
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ResourceSpans>> captor = ArgumentCaptor.forClass((Class) List.class);
        verify(exportService).export(captor.capture(), eq(OtlpTraceIngestMetrics.Transport.HTTP));
        Span span = captor.getValue().get(0).getScopeSpans(0).getSpans(0);
        assertThat(span.getTraceId()).isEqualTo(ByteString.copyFrom(HexFormat.of().parseHex(TRACE_ID_HEX)));
        assertThat(span.getSpanId()).isEqualTo(ByteString.copyFrom(HexFormat.of().parseHex(SPAN_ID_HEX)));
    }

    @Test
    void json_partialSuccess_int64AsString() throws Exception {
        when(exportService.export(anyList(), any())).thenReturn(partialSuccessResult());

        String body = mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        ExportTraceServiceResponse response = parseJson(body, ExportTraceServiceResponse.newBuilder()).build();
        assertThat(response.getPartialSuccess().getRejectedSpans()).isEqualTo(5);
        assertThat(response.getPartialSuccess().getErrorMessage()).isEqualTo("invalid span");
        // proto3 JSON prints int64 as a string.
        assertThat(body).contains("\"rejectedSpans\":\"5\"");
    }

    @Test
    void json_serverError_503WithStatusBody() throws Exception {
        when(exportService.export(anyList(), any())).thenReturn(serverErrorResult());

        String body = mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_REQUEST))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Status status = parseJson(body, Status.newBuilder()).build();
        assertThat(status.getCode()).isEqualTo(Code.UNAVAILABLE_VALUE);
        assertThat(status.getMessage()).isEqualTo("insert failed");
    }

    @Test
    void json_parseFailure_400WithStatusBody() throws Exception {
        String body = mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceSpans\":[{\"scopeSpans\":[{\"spans\":[{\"traceId\":\"zz\"}]}]}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();

        Status status = parseJson(body, Status.newBuilder()).build();
        assertThat(status.getCode()).isEqualTo(Code.INVALID_ARGUMENT_VALUE);
        assertThat(status.getMessage()).isEqualTo("not a hexadecimal digit: \"z\" = 122");
    }

    @Test
    void parseFailure_countsRequestRejectedParseError_andNothingElse() throws Exception {
        mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resourceSpans\":[{\"scopeSpans\":[{\"spans\":[{\"traceId\":\"zz\"}]}]}]}"))
                .andExpect(status().isBadRequest());

        assertThat(meterRegistry.get(OtlpTraceIngestMetrics.REQUEST_REJECTED)
                .tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "http")
                .tag(OtlpTraceIngestMetrics.TAG_REASON, "parse_error")
                .counter().count()).isEqualTo(1.0);
        // A refused request never reaches the span-level counters.
        assertThat(meterRegistry.get(OtlpTraceIngestMetrics.SPAN_RECEIVED)
                .tag(OtlpTraceIngestMetrics.TAG_TRANSPORT, "http")
                .counter().count()).isZero();
        verify(exportService, never()).export(anyList(), any());
    }

    @Test
    void json_parseFailure_messageCappedAt256() throws Exception {
        // JsonFormat echoes the offending value into its message; a 3 KB root string would otherwise
        // come back as a 3 KB Status.message.
        String hostile = "\"" + "A".repeat(3000) + "\"";

        String body = mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(hostile))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        Status status = parseJson(body, Status.newBuilder()).build();
        assertThat(status.getMessage())
                .hasSize(OtlpTraceController.MAX_ERROR_MESSAGE_LENGTH + "...".length())
                .startsWith("Expect message object but got: \"AAAA")
                .endsWith("...");
    }

    @Test
    void protobuf_parseFailure_400WithStatusBody() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_PROTOBUF)
                        .content(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROTOBUF))
                .andReturn().getResponse();

        Status status = Status.parseFrom(response.getContentAsByteArray());
        assertThat(status.getCode()).isEqualTo(Code.INVALID_ARGUMENT_VALUE);
    }

    @Test
    void protobuf_partialSuccess() throws Exception {
        when(exportService.export(anyList(), any())).thenReturn(partialSuccessResult());

        byte[] body = mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_PROTOBUF)
                        .content(protobufRequest()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PROTOBUF))
                .andReturn().getResponse().getContentAsByteArray();

        ExportTraceServiceResponse response = ExportTraceServiceResponse.parseFrom(body);
        assertThat(response.getPartialSuccess().getRejectedSpans()).isEqualTo(5);
        assertThat(response.getPartialSuccess().getErrorMessage()).isEqualTo("invalid span");
    }

    @Test
    void protobuf_serverError_503WithStatusBody() throws Exception {
        when(exportService.export(anyList(), any())).thenReturn(serverErrorResult());

        byte[] body = mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_PROTOBUF)
                        .content(protobufRequest()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentType(MediaType.APPLICATION_PROTOBUF))
                .andReturn().getResponse().getContentAsByteArray();

        Status status = Status.parseFrom(body);
        assertThat(status.getCode()).isEqualTo(Code.UNAVAILABLE_VALUE);
        assertThat(status.getMessage()).isEqualTo("insert failed");
    }

    @Test
    void json_acceptHeaderIgnored_responseMirrorsRequestContentType() throws Exception {
        // OTLP exporters do not negotiate: the response encoding follows the request body, not Accept.
        when(exportService.export(anyList(), any())).thenReturn(successResult());

        mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROTOBUF)
                        .content(JSON_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));
    }

    @Test
    void json_emptyObject_exportsNothing() throws Exception {
        when(exportService.export(anyList(), any())).thenReturn(successResult());

        mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));

        verify(exportService).export(eq(List.of()), eq(OtlpTraceIngestMetrics.Transport.HTTP));
    }

    @Test
    void emptyBody_400_pinnedSpringBehavior() throws Exception {
        // A zero-length body never reaches the controller: Spring rejects a missing @RequestBody
        // with 400 before parsing (no google.rpc.Status body). No OTLP exporter sends one, so this
        // only pins the behavior for both encodings.
        mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_PROTOBUF)
                        .content(new byte[0]))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new byte[0]))
                .andExpect(status().isBadRequest());

        verify(exportService, never()).export(anyList(), any());
    }

    @Test
    void missingContentType_415() throws Exception {
        mockMvc.perform(post("/v1/traces")
                        .content(protobufRequest()))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void json_contentTypeCharsetParameter_treatedAsJson() throws Exception {
        when(exportService.export(anyList(), any())).thenReturn(successResult());

        mockMvc.perform(post("/v1/traces")
                        .header("Content-Type", "application/json; charset=utf-8")
                        .content(JSON_REQUEST))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));
    }

    @Test
    void unsupportedContentType_415() throws Exception {
        mockMvc.perform(post("/v1/traces")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void json_gzip_decompressedByExistingFilter() throws Exception {
        when(exportService.export(anyList(), any())).thenReturn(successResult());

        MockMvc mvcWithFilter = MockMvcBuilders.standaloneSetup(new OtlpTraceController(exportService, ingestMetrics))
                .addFilters(new OtlpTraceDecompressionFilter(16 * 1024 * 1024, ingestMetrics))
                .build();

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(compressed)) {
            gzip.write(JSON_REQUEST.getBytes(StandardCharsets.UTF_8));
        }

        mvcWithFilter.perform(post("/v1/traces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Content-Encoding", "gzip")
                        .content(compressed.toByteArray()))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));
    }
}
