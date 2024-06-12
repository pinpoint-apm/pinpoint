/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.realtime.collector.receiver.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.DefaultTransportMetadata;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCount;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadCountRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDump;
import com.navercorp.pinpoint.grpc.trace.PCmdActiveThreadLightDumpRes;
import com.navercorp.pinpoint.grpc.trace.PCmdEcho;
import com.navercorp.pinpoint.grpc.trace.PCmdEchoResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdMessage;
import com.navercorp.pinpoint.grpc.trace.PCmdRequest;
import com.navercorp.pinpoint.grpc.trace.PCmdResponse;
import com.navercorp.pinpoint.grpc.trace.PCmdServiceHandshake;
import com.navercorp.pinpoint.grpc.trace.PCmdStreamResponse;
import com.navercorp.pinpoint.grpc.trace.ProfilerCommandServiceGrpc;
import com.navercorp.pinpoint.realtime.collector.sink.ErrorSinkRepository;
import com.navercorp.pinpoint.realtime.collector.sink.SinkRepository;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class GrpcCommandServiceTest {

    private static final long SINK_ID = 2345;

    @Mock GrpcAgentConnectionRepository agentConnectionRepository;
    @Mock ErrorSinkRepository sinkRepository;
    @Mock SinkRepository<FluxSink<PCmdActiveThreadCountRes>> activeThreadCountSinkRepo;
    @Mock SinkRepository<MonoSink<PCmdActiveThreadDumpRes>> activeThreadDumpSinkRepo;
    @Mock SinkRepository<MonoSink<PCmdActiveThreadLightDumpRes>> activeThreadLightDumpSinkRepo;
    @Mock SinkRepository<MonoSink<PCmdEchoResponse>> echoSinkRepo;

    @Test
    public void testActiveThreadCount() throws IOException {
        GrpcCommandService service = new GrpcCommandService(
                agentConnectionRepository,
                sinkRepository,
                activeThreadCountSinkRepo,
                activeThreadDumpSinkRepo,
                activeThreadLightDumpSinkRepo,
                echoSinkRepo
        );

        AtomicReference<GrpcAgentConnection> connRef = new AtomicReference<>();
        doAnswer(inv -> {
            GrpcAgentConnection conn = inv.getArgument(0, GrpcAgentConnection.class);
            connRef.set(conn);
            return null;
        }).when(agentConnectionRepository).add(any());

        AtomicReference<FluxSink<PCmdActiveThreadCountRes>> sinkRef = new AtomicReference<>();
        doAnswer(inv -> sinkRef.get()).when(activeThreadCountSinkRepo).get(eq(SINK_ID));

        String serverName = InProcessServerBuilder.generateName();
        Server server = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(service)
                .intercept(getMockingInterceptor())
                .build()
                .start();
        ManagedChannel channel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build();
        ProfilerCommandServiceGrpc.ProfilerCommandServiceStub stub = ProfilerCommandServiceGrpc.newStub(channel);

        StreamObserver<PCmdMessage> commandObserver = connect(stub);

        commandObserver.onNext(PCmdMessage.newBuilder()
                .setHandshakeMessage(PCmdServiceHandshake.newBuilder()
                        .addAllSupportCommandServiceKey(List.of()))
                .build());

        GrpcAgentConnection conn = connRef.get();

        List<PCmdActiveThreadCountRes> res = Flux.<PCmdActiveThreadCountRes>create(sink -> {
            sinkRef.set(sink);
            conn.request(PCmdRequest.newBuilder()
                    .setCommandActiveThreadCount(PCmdActiveThreadCount.getDefaultInstance())
                    .setRequestId((int) SINK_ID)
                    .build());
        }).take(2).collectList().block(Duration.ofSeconds(1));

        assertThat(res).hasSize(2);

        server.shutdownNow();
    }

    @Test
    public void testActiveThreadDump() throws IOException {
        GrpcCommandService service = new GrpcCommandService(
                agentConnectionRepository,
                sinkRepository,
                activeThreadCountSinkRepo,
                activeThreadDumpSinkRepo,
                activeThreadLightDumpSinkRepo,
                echoSinkRepo
        );

        AtomicReference<GrpcAgentConnection> connRef = new AtomicReference<>();
        doAnswer(inv -> {
            GrpcAgentConnection conn = inv.getArgument(0, GrpcAgentConnection.class);
            connRef.set(conn);
            return null;
        }).when(agentConnectionRepository).add(any());

        AtomicReference<MonoSink<PCmdActiveThreadDumpRes>> sinkRef = new AtomicReference<>();
        doAnswer(inv -> sinkRef.get()).when(activeThreadDumpSinkRepo).get(eq(SINK_ID));

        String serverName = InProcessServerBuilder.generateName();
        Server server = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(service)
                .intercept(getMockingInterceptor())
                .build()
                .start();
        ManagedChannel channel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build();
        ProfilerCommandServiceGrpc.ProfilerCommandServiceStub stub = ProfilerCommandServiceGrpc.newStub(channel);

        StreamObserver<PCmdMessage> commandObserver = connect(stub);

        commandObserver.onNext(PCmdMessage.newBuilder()
                .setHandshakeMessage(PCmdServiceHandshake.newBuilder()
                        .addAllSupportCommandServiceKey(List.of()))
                .build());

        GrpcAgentConnection conn = connRef.get();

        PCmdActiveThreadDumpRes res = Mono.<PCmdActiveThreadDumpRes>create(sink -> {
            sinkRef.set(sink);
            conn.request(PCmdRequest.newBuilder()
                    .setCommandActiveThreadDump(PCmdActiveThreadDump.getDefaultInstance())
                    .setRequestId((int) SINK_ID)
                    .build());
        }).block(Duration.ofSeconds(1));

        assertThat(res).isNotNull();

        server.shutdownNow();
    }

    @Test
    public void testActiveThreadLightDump() throws IOException {
        GrpcCommandService service = new GrpcCommandService(
                agentConnectionRepository,
                sinkRepository,
                activeThreadCountSinkRepo,
                activeThreadDumpSinkRepo,
                activeThreadLightDumpSinkRepo,
                echoSinkRepo
        );

        AtomicReference<GrpcAgentConnection> connRef = new AtomicReference<>();
        doAnswer(inv -> {
            GrpcAgentConnection conn = inv.getArgument(0, GrpcAgentConnection.class);
            connRef.set(conn);
            return null;
        }).when(agentConnectionRepository).add(any());

        AtomicReference<MonoSink<PCmdActiveThreadLightDumpRes>> sinkRef = new AtomicReference<>();
        doAnswer(inv -> sinkRef.get()).when(activeThreadLightDumpSinkRepo).get(eq(SINK_ID));

        String serverName = InProcessServerBuilder.generateName();
        Server server = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(service)
                .intercept(getMockingInterceptor())
                .build()
                .start();
        ManagedChannel channel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build();
        ProfilerCommandServiceGrpc.ProfilerCommandServiceStub stub = ProfilerCommandServiceGrpc.newStub(channel);

        StreamObserver<PCmdMessage> commandObserver = connect(stub);

        commandObserver.onNext(PCmdMessage.newBuilder()
                .setHandshakeMessage(PCmdServiceHandshake.newBuilder()
                        .addAllSupportCommandServiceKey(List.of()))
                .build());

        GrpcAgentConnection conn = connRef.get();

        PCmdActiveThreadLightDumpRes res = Mono.<PCmdActiveThreadLightDumpRes>create(sink -> {
            sinkRef.set(sink);
            conn.request(PCmdRequest.newBuilder()
                    .setCommandActiveThreadLightDump(PCmdActiveThreadLightDump.getDefaultInstance())
                    .setRequestId((int) SINK_ID)
                    .build());
        }).block(Duration.ofSeconds(1));

        assertThat(res).isNotNull();

        server.shutdownNow();
    }

    @Test
    public void testEcho() throws IOException {
        GrpcCommandService service = new GrpcCommandService(
                agentConnectionRepository,
                sinkRepository,
                activeThreadCountSinkRepo,
                activeThreadDumpSinkRepo,
                activeThreadLightDumpSinkRepo,
                echoSinkRepo
        );

        AtomicReference<GrpcAgentConnection> connRef = new AtomicReference<>();
        doAnswer(inv -> {
            GrpcAgentConnection conn = inv.getArgument(0, GrpcAgentConnection.class);
            connRef.set(conn);
            return null;
        }).when(agentConnectionRepository).add(any());

        AtomicReference<MonoSink<PCmdEchoResponse>> sinkRef = new AtomicReference<>();
        doAnswer(inv -> sinkRef.get()).when(echoSinkRepo).get(eq(SINK_ID));

        String serverName = InProcessServerBuilder.generateName();
        Server server = InProcessServerBuilder
                .forName(serverName)
                .directExecutor()
                .addService(service)
                .intercept(getMockingInterceptor())
                .build()
                .start();
        ManagedChannel channel = InProcessChannelBuilder
                .forName(serverName)
                .directExecutor()
                .build();
        ProfilerCommandServiceGrpc.ProfilerCommandServiceStub stub = ProfilerCommandServiceGrpc.newStub(channel);

        StreamObserver<PCmdMessage> commandObserver = connect(stub);

        commandObserver.onNext(PCmdMessage.newBuilder()
                .setHandshakeMessage(PCmdServiceHandshake.newBuilder()
                        .addAllSupportCommandServiceKey(List.of()))
                .build());

        GrpcAgentConnection conn = connRef.get();

        PCmdEchoResponse res = Mono.<PCmdEchoResponse>create(sink -> {
            sinkRef.set(sink);
            conn.request(PCmdRequest.newBuilder()
                    .setCommandEcho(PCmdEcho.getDefaultInstance())
                    .setRequestId((int) SINK_ID)
                    .build());
        }).block(Duration.ofSeconds(1));

        assertThat(res).isNotNull();

        server.shutdownNow();
    }

    private static ServerInterceptor getMockingInterceptor() {
        return new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                Context nextCtx = Context.current()
                        .withValue(ServerContext.getTransportMetadataKey(), new DefaultTransportMetadata(
                                "debugString",
                                InetSocketAddress.createUnresolved("127.0.0.2", 53253),
                                InetSocketAddress.createUnresolved("127.0.0.1", 9991),
                                0, 1234
                        ))
                        .withValue(ServerContext.getAgentInfoKey(), new Header(
                                "name",
                                AgentId.of("agent-id"),
                                "agent-name",
                                "application-name",
                                "service-name",
                                ServiceType.TEST.getCode(),
                                1234,
                                0,
                                List.of(710, 720, 730, 740, 750)
                        ));
                return Contexts.interceptCall(nextCtx, call, headers, next);
            }
        };
    }

    private static StreamObserver<PCmdMessage> connect(ProfilerCommandServiceGrpc.ProfilerCommandServiceStub stub) {
        return stub.handleCommandV2(new StreamObserver<>() {
            @Override
            public void onNext(PCmdRequest request) {
                if (request.hasCommandActiveThreadCount()) {
                    int sinkId = request.getRequestId();
                    StreamObserver<PCmdActiveThreadCountRes> emit =
                            stub.commandStreamActiveThreadCount(getEmptyObserver());
                    emit.onNext(PCmdActiveThreadCountRes.newBuilder()
                            .setCommonStreamResponse(PCmdStreamResponse.newBuilder()
                                    .setResponseId(sinkId)
                                    .setSequenceId(1)
                                    .setMessage(StringValue.of("OK")))
                            .build());
                    emit.onNext(PCmdActiveThreadCountRes.newBuilder()
                            .addAllActiveThreadCount(List.of(0, 1, 2, 3))
                            .setCommonStreamResponse(PCmdStreamResponse.newBuilder()
                                    .setResponseId(sinkId)
                                    .setSequenceId(2)
                                    .setMessage(StringValue.of("OK")))
                            .build());
                    emit.onNext(PCmdActiveThreadCountRes.newBuilder()
                            .addAllActiveThreadCount(List.of(0, 1, 2, 3))
                            .setCommonStreamResponse(PCmdStreamResponse.newBuilder()
                                    .setResponseId(sinkId)
                                    .setSequenceId(3)
                                    .setMessage(StringValue.of("OK")))
                            .build());
                } else if (request.hasCommandActiveThreadDump()) {
                    stub.commandActiveThreadDump(
                            PCmdActiveThreadDumpRes.newBuilder()
                                    .setCommonResponse(PCmdResponse.newBuilder()
                                            .setResponseId(request.getRequestId())
                                            .setMessage(StringValue.of("OK")))
                                    .build(),
                            getEmptyObserver()
                    );
                } else if (request.hasCommandActiveThreadLightDump()) {
                    stub.commandActiveThreadLightDump(
                            PCmdActiveThreadLightDumpRes.newBuilder()
                                    .setCommonResponse(PCmdResponse.newBuilder()
                                            .setResponseId(request.getRequestId())
                                            .setMessage(StringValue.of("OK")))
                                    .build(),
                            getEmptyObserver()
                    );
                } else if (request.hasCommandEcho()) {
                    stub.commandEcho(
                            PCmdEchoResponse.newBuilder()
                                    .setCommonResponse(PCmdResponse.newBuilder()
                                            .setResponseId(request.getRequestId())
                                            .setMessage(StringValue.of("OK")))
                                    .build(),
                            getEmptyObserver()
                    );
                }
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onCompleted() {
            }
        });
    }

    private static StreamObserver<Empty> getEmptyObserver() {
        return new StreamObserver<>() {
            @Override
            public void onNext(Empty empty) {
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onCompleted() {
            }
        };
    }

}
