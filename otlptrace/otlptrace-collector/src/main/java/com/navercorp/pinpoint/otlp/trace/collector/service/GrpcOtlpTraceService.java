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
import com.navercorp.pinpoint.common.cache.LRUCache;
import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorRejectedSpan;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapper;
import com.navercorp.pinpoint.otlp.trace.collector.mapper.OtlpTraceMapperData;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTracePartialSuccess;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class GrpcOtlpTraceService extends TraceServiceGrpc.TraceServiceImplBase {
    private static final Supplier<ServiceUid> DEFAULT_SERVICE_UID = () -> ServiceUid.DEFAULT;

    private final Logger logger = LogManager.getLogger(this.getClass());

    @NotNull
    private final TraceService traceService;
    private final HbaseOtlpAgentInfoService agentInfoService;
    private final HbaseOtlpApplicationIndexV2Service applicationIndexV2Service;
    @NotNull
    private final OtlpTraceMapper otlpTraceMapper;
    private final LRUCache<String, Boolean> agentIdCache = new LRUCache<>(10000);

    public GrpcOtlpTraceService(TraceService traceService, HbaseOtlpAgentInfoService agentInfoService, HbaseOtlpApplicationIndexV2Service applicationIndexV2Service, OtlpTraceMapper otlpTraceMapper) {
        this.traceService = traceService;
        this.agentInfoService = agentInfoService;
        this.applicationIndexV2Service = applicationIndexV2Service;
        this.otlpTraceMapper = otlpTraceMapper;
    }

    @Override
    public void export(ExportTraceServiceRequest request, StreamObserver<ExportTraceServiceResponse> responseObserver) {
        final List<ResourceSpans> resourceSpanList = request.getResourceSpansList();
        final OtlpTraceCollectorRejectedSpan rejectedSpan = export(resourceSpanList);

        if (rejectedSpan.count() > 0) {
            final ExportTracePartialSuccess.Builder builder = ExportTracePartialSuccess.newBuilder();
            builder.setErrorMessage(rejectedSpan.getMessage());
            builder.setRejectedSpans(rejectedSpan.count());
            responseObserver.onNext(ExportTraceServiceResponse.newBuilder().setPartialSuccess(builder.build()).build());
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(rejectedSpan.getMessage()).asRuntimeException());
        } else {
            // success
            responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
            responseObserver.onCompleted();
        }
    }

    private OtlpTraceCollectorRejectedSpan export(List<ResourceSpans> resourceSpanList) {
        final OtlpTraceMapperData otlpTraceMapperData = otlpTraceMapper.map(resourceSpanList);
        int errorCount = 0;
        for (SpanBo spanBo : otlpTraceMapperData.getSpanBoList()) {
            try {
                traceService.insertSpan(spanBo);
                if (logger.isDebugEnabled()) {
                    logger.debug("SpanBo inserted. {}", spanBo);
                }
            } catch (Exception e) {
                errorCount++;
                logger.warn("Failed to insert spanBo", e);
            }
        }

        for (SpanChunkBo spanChunkBo : otlpTraceMapperData.getSpanChunkBoList()) {
            try {
                traceService.insertSpanChunk(spanChunkBo);
                if (logger.isDebugEnabled()) {
                    logger.debug("SpanChunkBo inserted. {}", spanChunkBo);
                }
            } catch (Exception e) {
                errorCount++;
                logger.warn("Failed to insert spanChunkBo", e);
            }
        }

        if (errorCount > 0) {
            OtlpTraceCollectorRejectedSpan rejectedSpan = otlpTraceMapperData.getRejectedSpan();
            rejectedSpan.putMessage("insert error (" + errorCount + ")");
            rejectedSpan.addCount(errorCount);
        }

        int agentInfoErrorCount = 0;
        for (AgentInfoBo agentInfoBo : otlpTraceMapperData.getAgentInfoBoList()) {
            if (agentIdCache.get(agentInfoBo.getAgentId()) == null) {
                agentIdCache.put(agentInfoBo.getAgentId(), true);
                try {
                    agentInfoService.insert(agentInfoBo);
                    applicationIndexV2Service.insert(DEFAULT_SERVICE_UID, agentInfoBo);
                } catch (Exception e) {
                    agentInfoErrorCount++;
                    logger.warn("Failed to insert agentInfoBo", e);
                }
            }
        }

        if (agentInfoErrorCount > 0) {
            OtlpTraceCollectorRejectedSpan rejectedSpan = otlpTraceMapperData.getRejectedSpan();
            rejectedSpan.putMessage("agentInfo error (" + agentInfoErrorCount + ")");
            rejectedSpan.addCount(agentInfoErrorCount);
        }

        return otlpTraceMapperData.getRejectedSpan();
    }
}