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

import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportResult;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTraceExportService;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
public class OtlpTraceController {

    private final OtlpTraceExportService exportService;

    public OtlpTraceController(OtlpTraceExportService exportService) {
        this.exportService = Objects.requireNonNull(exportService, "exportService");
    }

    @PostMapping(value = "/v1/traces", consumes = "application/x-protobuf")
    public ResponseEntity<Void> saveOtlpMetric(@RequestBody ExportTraceServiceRequest request) {
        final List<ResourceSpans> resourceSpanList = request.getResourceSpansList();
        final OtlpTraceExportResult result = exportService.export(resourceSpanList);

        // NOTE (M-1, out of scope for this change): the HTTP path currently returns 200 regardless of
        // outcome and sends no ExportTraceServiceResponse body. result.serverErrorCount() /
        // result.clientRejected() are now available here to implement proper OTLP/HTTP response
        // semantics (5xx on server error, ExportTracePartialSuccess body on client rejects) later.
        return ResponseEntity.ok().build();
    }
}
