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

package com.navercorp.pinpoint.otlp.trace.collector.inspector.controller;

import com.google.rpc.Code;
import com.google.rpc.Status;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.OtlpMetricInspectorHttpModule;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricExportResults;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricExportService;
import com.navercorp.pinpoint.otlp.trace.collector.inspector.service.OtlpMetricIngestMetrics;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.resource.v1.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

class OtlpMetricControllerTest {

    private static final String PATH = OtlpMetricInspectorHttpModule.OTLP_HTTP_METRICS_PATH;

    private final OtlpMetricExportService exportService = mock(OtlpMetricExportService.class);
    private SimpleMeterRegistry meterRegistry;
    private OtlpMetricController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        controller = new OtlpMetricController(exportService, new OtlpMetricIngestMetrics(meterRegistry));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private double parseErrors() {
        return meterRegistry.get(OtlpMetricIngestMetrics.REQUEST_REJECTED)
                .tag(OtlpMetricIngestMetrics.TAG_TRANSPORT, "http")
                .tag(OtlpMetricIngestMetrics.TAG_REASON, "parse_error")
                .counter().count();
    }

    @Test
    void protobuf_success() throws Exception {
        ResourceMetrics resourceMetrics = ResourceMetrics.newBuilder().setResource(Resource.newBuilder()).build();
        byte[] request = ExportMetricsServiceRequest.newBuilder().addResourceMetrics(resourceMetrics).build().toByteArray();
        when(exportService.export(anyList(), any())).thenReturn(OtlpMetricExportResults.success(1));

        byte[] body = mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_PROTOBUF)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PROTOBUF))
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(ExportMetricsServiceResponse.parseFrom(body)).isEqualTo(ExportMetricsServiceResponse.getDefaultInstance());
        verify(exportService).export(eq(List.of(resourceMetrics)), eq(OtlpTransport.HTTP));
    }

    @Test
    void emptyBody_isEmptyRequest() throws Exception {
        // The body is read as a raw stream, so a zero-length body is a valid, empty
        // ExportMetricsServiceRequest, the same as an empty message on the gRPC path.
        when(exportService.export(anyList(), any())).thenReturn(OtlpMetricExportResults.success(0));

        byte[] body = mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_PROTOBUF)
                        .content(new byte[0]))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(ExportMetricsServiceResponse.parseFrom(body)).isEqualTo(ExportMetricsServiceResponse.getDefaultInstance());
        verify(exportService).export(eq(List.of()), eq(OtlpTransport.HTTP));
    }

    @Test
    void malformedBody_400WithStatusBody_countedAsParseError() throws Exception {
        byte[] body = mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_PROTOBUF)
                        .content(new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff}))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROTOBUF))
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(Status.parseFrom(body).getCode()).isEqualTo(Code.INVALID_ARGUMENT_VALUE);
        assertThat(parseErrors()).isEqualTo(1.0);
        verify(exportService, never()).export(anyList(), any());
    }

    @Test
    void bodyReadFailure_400WithStatusBody_countedAsParseError() throws Exception {
        // The admission/decompression filters abort an oversized body with an IOException from the
        // request stream. Parsing straight off the stream routes that failure through the controller's
        // parse-error path (Status body + parse_error count) rather than Spring's bare 400.
        InputStream failing = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("OTLP/HTTP request body exceeded max size: limit=1");
            }
        };

        ResponseEntity<byte[]> response = controller.export(failing);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROTOBUF);
        Status status = Status.parseFrom(response.getBody());
        assertThat(status.getCode()).isEqualTo(Code.INVALID_ARGUMENT_VALUE);
        assertThat(status.getMessage()).contains("exceeded max size");
        assertThat(parseErrors()).isEqualTo(1.0);
        verify(exportService, never()).export(anyList(), any());
    }

    @Test
    void json_415() throws Exception {
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnsupportedMediaType());
        verify(exportService, never()).export(anyList(), any());
    }
}
