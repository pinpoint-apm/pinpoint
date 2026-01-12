/*
 * Copyright 2025 NAVER Corp.
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

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperData;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrpcOtlpTraceService extends TraceServiceGrpc.TraceServiceImplBase {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @NotNull
    private final TraceService traceService;
    @NotNull
    private final OtlpTraceMapper otlpTraceMapper;

    public GrpcOtlpTraceService(TraceService traceService, OtlpTraceMapper otlpTraceMapper) {
        this.traceService = traceService;
        this.otlpTraceMapper = otlpTraceMapper;
    }

    @Override
    public void export(ExportTraceServiceRequest request, StreamObserver<ExportTraceServiceResponse> responseObserver) {
        final List<ResourceSpans> resourceSpanList = request.getResourceSpansList();
        export(resourceSpanList);

        responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private void export(List<ResourceSpans> resourceSpanList) {
        final OtlpTraceMapperData otlpTraceMapperData = otlpTraceMapper.map(resourceSpanList);
        for (SpanBo spanBo : otlpTraceMapperData.getSpanBoList()) {
            try {
                traceService.insertSpan(spanBo);
            } catch (Exception e) {
                logger.warn("Failed to insert spanBo", e);
            }
        }

        for (SpanChunkBo spanChunkBo : otlpTraceMapperData.getSpanChunkBoList()) {
            try {
                traceService.insertSpanChunk(spanChunkBo);
            } catch (Exception e) {
                logger.warn("Failed to insert spanChunkBo", e);
            }
        }
    }
}
