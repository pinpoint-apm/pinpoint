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

import com.navercorp.pinpoint.collector.grpc.config.ServerServiceDefinitions;
import com.navercorp.pinpoint.otlp.log.collector.OtlpLogRejectedRecords;
import com.navercorp.pinpoint.otlp.trace.collector.OtlpTraceCollectorModule;
import com.navercorp.pinpoint.otlp.trace.collector.service.OtlpTransport;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.SPAN_ID;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.exceptionRecord;
import static com.navercorp.pinpoint.otlp.log.collector.LogRecordFixtures.resourceLogs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The seam the trace module opened for this receiver: its {@code serviceList} takes every
 * {@link ServerServiceDefinition} bean, so registering the log service is a matter of declaring one.
 * This runs both services on one in-process gRPC server, the way the real 9998 server hosts them, and
 * checks that gRPC routes by service name to each.
 */
class OtlpLogGrpcCoRegistrationTest {

    private Server server;
    private ManagedChannel channel;
    private final OtlpLogExportService logExportService = mock(OtlpLogExportService.class);

    @BeforeEach
    void startServer() throws Exception {
        TraceServiceGrpc.TraceServiceImplBase traceService = new TraceServiceGrpc.TraceServiceImplBase() {
            @Override
            public void export(ExportTraceServiceRequest request, StreamObserver<ExportTraceServiceResponse> responseObserver) {
                responseObserver.onNext(ExportTraceServiceResponse.getDefaultInstance());
                responseObserver.onCompleted();
            }
        };
        OtlpLogIngestMetrics metrics = new OtlpLogIngestMetrics(new SimpleMeterRegistry());
        GrpcOtlpLogService logService = new GrpcOtlpLogService(logExportService, Runnable::run, 1 << 20, metrics);

        ServerServiceDefinitions definitions = new OtlpTraceCollectorModule()
                .serviceList(List.of(traceService.bindService(), logService.bindService()));

        String name = InProcessServerBuilder.generateName();
        InProcessServerBuilder builder = InProcessServerBuilder.forName(name).directExecutor();
        for (ServerServiceDefinition definition : definitions.getDefinitions()) {
            builder.addService(definition);
        }
        server = builder.build().start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
    }

    @AfterEach
    void stopServer() throws Exception {
        channel.shutdownNow();
        server.shutdownNow();
        server.awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void bothOtlpServices_areServedOnTheSameServer() {
        when(logExportService.export(anyList(), eq(OtlpTransport.GRPC)))
                .thenReturn(new OtlpLogExportResult(1, new OtlpLogRejectedRecords()));

        ExportTraceServiceResponse traceResponse = TraceServiceGrpc.newBlockingStub(channel)
                .export(ExportTraceServiceRequest.getDefaultInstance());
        ExportLogsServiceResponse logsResponse = LogsServiceGrpc.newBlockingStub(channel)
                .export(ExportLogsServiceRequest.newBuilder()
                        .addResourceLogs(resourceLogs(exceptionRecord(SPAN_ID, "java.lang.IllegalStateException", "boom", null).build()))
                        .build());

        assertThat(traceResponse).isEqualTo(ExportTraceServiceResponse.getDefaultInstance());
        assertThat(logsResponse).isEqualTo(ExportLogsServiceResponse.getDefaultInstance());
    }

    @Test
    void logsBackpressure_surfacesAsUnavailable_toTheExporter() {
        when(logExportService.export(anyList(), eq(OtlpTransport.GRPC))).thenThrow(new IllegalStateException("poison"));

        assertThatThrownBy(() -> LogsServiceGrpc.newBlockingStub(channel)
                .export(ExportLogsServiceRequest.newBuilder().addResourceLogs(resourceLogs()).build()))
                .isInstanceOf(StatusRuntimeException.class)
                .extracting(e -> ((StatusRuntimeException) e).getStatus().getCode())
                .isEqualTo(Status.Code.INTERNAL);
    }
}
