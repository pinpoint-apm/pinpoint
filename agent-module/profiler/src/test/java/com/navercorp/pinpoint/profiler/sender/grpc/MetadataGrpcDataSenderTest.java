package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.retry.HedgingServiceConfigBuilder;
import com.navercorp.pinpoint.grpc.trace.MetadataGrpc;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.profiler.context.grpc.GrpcMetadataMessageConverter;
import com.navercorp.pinpoint.profiler.context.grpc.mapper.MetaDataMapper;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaData;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

class MetadataGrpcDataSenderTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final long DEFAULT_TEST_HEDGING_DELAY_MILLIS = 500L;
    private static final String DELAY_METADATA = "delay few seconds";
    private static final String RUNTIME_EXCEPTION_METADATA = "runtime exception test";
    private static final String UNAVAILABLE_METADATA = "status code UNAVAILABLE";
    private static final String UNKNOWN_METADATA = "status code UNKNOWN";
    private static final String FAIL_METADATA = "success=false";

    private static final Context.Key<Metadata> GRPC_METADATA_CONTEXT_KEY = Context.key("test-grpc-metadata");

    private static final Metadata.Key<String> TEST_ID_KEY = Metadata.Key.of("test-id", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<String> GRPC_PREVIOUS_RPC_ATTEMPTS_KEY = Metadata.Key.of("grpc-previous-rpc-attempts", Metadata.ASCII_STRING_MARSHALLER);

    private static Server server;
    private static String serverName;
    private static int testId;
    private static int requestCounter;

    @BeforeAll
    public static void setUp() {
        serverName = InProcessServerBuilder.generateName();

        server = InProcessServerBuilder
                .forName(serverName)
                //.directExecutor()
                .addService(ServerInterceptors.intercept(new MetadataGrpcService(), new TestServerInterceptor()))
                .build();

        CompletableFuture.supplyAsync(() -> {
            try {
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });

        testId = 0;
    }

    @AfterAll
    public static void tearDown() {
        server.shutdown();
    }

    @BeforeEach
    public void resetCounter() {
        testId++;
        requestCounter = 0;
    }

    public static class MetadataGrpcService extends MetadataGrpc.MetadataImplBase {
        @Override
        public void requestApiMetaData(PApiMetaData request, StreamObserver<PResult> responseObserver) {
            countAndPrint(request);

            switch (request.getApiInfo()) {
                case DELAY_METADATA:
                    try {
                        Thread.sleep(1000);
                        System.out.println("server delayed response time: " + new Timestamp(System.currentTimeMillis()));
                        responseObserver.onNext(PResult.newBuilder().setSuccess(true).setMessage("test 1s delay, status code: OK").build());
                    } catch (InterruptedException ignore) {
                    }
                    responseObserver.onCompleted();
                    break;
                case UNAVAILABLE_METADATA:
                    responseObserver.onError(Status.UNAVAILABLE.withDescription("test status code: UNAVAILABLE").asException());
                    break;
                case UNKNOWN_METADATA:
                    responseObserver.onError(Status.UNKNOWN.withDescription("test status code: UNKNOWN").asException());
                    break;
                case RUNTIME_EXCEPTION_METADATA:
                    responseObserver.onError(new RuntimeException("test with runtime exception, status code: UNKNOWN "));
                    break;
                case FAIL_METADATA:
                    responseObserver.onNext(PResult.newBuilder().setSuccess(false).setMessage("test success=false, status code: OK").build());
                    responseObserver.onCompleted();
                    break;
                default:
                    responseObserver.onNext(PResult.newBuilder().setSuccess(true).setMessage("test success=true, status code: OK").build());
                    responseObserver.onCompleted();
                    break;
            }
        }

        private void countAndPrint(PApiMetaData request) {
            int totalAttempts = -1;
            Metadata metadata = GRPC_METADATA_CONTEXT_KEY.get(Context.current());
            String requestTestId = metadata.get(TEST_ID_KEY);
            String previousAttempts = metadata.get(GRPC_PREVIOUS_RPC_ATTEMPTS_KEY);

            if (requestTestId != null && requestTestId.equals(Integer.toString(testId))) {
                requestCounter++;
                if (previousAttempts == null) {
                    totalAttempts = 1;
                } else {
                    totalAttempts = Integer.parseInt(previousAttempts) + 1;
                }
            }
            System.out.println("---- server time: " + new Timestamp(System.currentTimeMillis()));
            System.out.println("testId: " + requestTestId);
            System.out.println("total attempts: " + totalAttempts);
            System.out.println(request);
        }
    }

    public static class TestServerInterceptor implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
            Context newContext = Context.current().withValue(GRPC_METADATA_CONTEXT_KEY, metadata);
            return Contexts.interceptCall(newContext, serverCall, metadata, serverCallHandler);
        }
    }

    @Test
    void sendTest() throws InterruptedException {
        HedgingServiceConfigBuilder serviceConfigBuilder = getTestServiceConfigBuilder();
        InProcessChannelBuilder channelBuilder = getInProcessChannelBuilder()
                .defaultServiceConfig(serviceConfigBuilder.buildMetadataConfig());

        MetadataGrpcHedgingDataSender<MetaDataType> metadataGrpcDataSender = getMetadataGrpcHedgingDataSender(channelBuilder);

        ApiMetaData apiMetaData = new ApiMetaData(1, "call", 10, 2);
        boolean send = metadataGrpcDataSender.request(apiMetaData);

        Assertions.assertThat(send).isTrue();
        Thread.sleep(DEFAULT_TEST_HEDGING_DELAY_MILLIS * 2);
        Assertions.assertThat(requestCounter).isGreaterThan(0);
    }

    @Test
    void sendFatalStatusCodeTest() throws InterruptedException {
        HedgingServiceConfigBuilder serviceConfigBuilder = getTestServiceConfigBuilder();
        serviceConfigBuilder.setNonFatalStatusCodes(Collections.emptyList());
        InProcessChannelBuilder channelBuilder = getInProcessChannelBuilder()
                .defaultServiceConfig(serviceConfigBuilder.buildMetadataConfig());
        MetadataGrpcHedgingDataSender<MetaDataType> metadataGrpcDataSender = getMetadataGrpcHedgingDataSender(channelBuilder);

        ApiMetaData apiMetaData = new ApiMetaData(1, UNAVAILABLE_METADATA, 10, 2);
        boolean send = metadataGrpcDataSender.request(apiMetaData);

        Assertions.assertThat(send).isTrue();
        Thread.sleep(DEFAULT_TEST_HEDGING_DELAY_MILLIS * 4);
        Assertions.assertThat(requestCounter).isEqualTo(1);
    }

    @Test
    void sendFailRetryTest() throws InterruptedException {
        HedgingServiceConfigBuilder serviceConfigBuilder = getTestServiceConfigBuilder();
        InProcessChannelBuilder channelBuilder = getInProcessChannelBuilder()
                .defaultServiceConfig(serviceConfigBuilder.buildMetadataConfig());
        MetadataGrpcHedgingDataSender<MetaDataType> metadataGrpcDataSender = getMetadataGrpcHedgingDataSender(channelBuilder);

        ApiMetaData apiMetaData = new ApiMetaData(2, UNAVAILABLE_METADATA, 10, 2);
        boolean send = metadataGrpcDataSender.request(apiMetaData);

        Assertions.assertThat(send).isTrue();
        Thread.sleep(DEFAULT_TEST_HEDGING_DELAY_MILLIS * 4);
        Assertions.assertThat(requestCounter).isEqualTo(3);
    }

    @Test
    void sendDelayRetryTest() throws InterruptedException {
        HedgingServiceConfigBuilder serviceConfigBuilder = getTestServiceConfigBuilder();
        serviceConfigBuilder.setHedgingDelayMillis(100);
        InProcessChannelBuilder channelBuilder = getInProcessChannelBuilder()
                .defaultServiceConfig(serviceConfigBuilder.buildMetadataConfig());
        MetadataGrpcHedgingDataSender<MetaDataType> metadataGrpcDataSender = getMetadataGrpcHedgingDataSender(channelBuilder);

        ApiMetaData apiMetaData = new ApiMetaData(3, DELAY_METADATA, 10, 2);
        boolean send = metadataGrpcDataSender.request(apiMetaData);

        Assertions.assertThat(send).isTrue();
        Thread.sleep(DEFAULT_TEST_HEDGING_DELAY_MILLIS * 4);
        Assertions.assertThat(requestCounter).isGreaterThan(1);
    }

    @Test
    void sendFailRetryRuntimeExceptionTest() throws InterruptedException {
        HedgingServiceConfigBuilder serviceConfigBuilder = getTestServiceConfigBuilder();
        InProcessChannelBuilder channelBuilder = getInProcessChannelBuilder()
                .defaultServiceConfig(serviceConfigBuilder.buildMetadataConfig());
        MetadataGrpcHedgingDataSender<MetaDataType> metadataGrpcDataSender = getMetadataGrpcHedgingDataSender(channelBuilder);

        ApiMetaData apiMetaData = new ApiMetaData(3, RUNTIME_EXCEPTION_METADATA, 10, 2);
        boolean send = metadataGrpcDataSender.request(apiMetaData);

        Assertions.assertThat(send).isTrue();
        Thread.sleep(DEFAULT_TEST_HEDGING_DELAY_MILLIS * 4);
        Assertions.assertThat(requestCounter).isGreaterThan(1);
    }

    @Test
    void sendMaxAttempts() throws InterruptedException {
        HedgingServiceConfigBuilder serviceConfigBuilder = getTestServiceConfigBuilder();
        serviceConfigBuilder.setMaxAttempts(5);
        InProcessChannelBuilder channelBuilder = getInProcessChannelBuilder()
                .defaultServiceConfig(serviceConfigBuilder.buildMetadataConfig());
        MetadataGrpcHedgingDataSender<MetaDataType> metadataGrpcDataSender = getMetadataGrpcHedgingDataSender(channelBuilder);

        ApiMetaData apiMetaData = new ApiMetaData(3, UNAVAILABLE_METADATA, 10, 2);
        boolean send = metadataGrpcDataSender.request(apiMetaData);

        Assertions.assertThat(send).isTrue();
        Thread.sleep(DEFAULT_TEST_HEDGING_DELAY_MILLIS * 7);
        Assertions.assertThat(requestCounter).isEqualTo(5);
    }

    @Test
    void sendMaxAttemptsLimit() throws InterruptedException {
        HedgingServiceConfigBuilder serviceConfigBuilder = getTestServiceConfigBuilder();
        serviceConfigBuilder.setMaxAttempts(5);
        InProcessChannelBuilder channelBuilder = getInProcessChannelBuilder()
                .maxHedgedAttempts(2)
                .defaultServiceConfig(serviceConfigBuilder.buildMetadataConfig());
        MetadataGrpcHedgingDataSender<MetaDataType> metadataGrpcDataSender = getMetadataGrpcHedgingDataSender(channelBuilder);

        ApiMetaData apiMetaData = new ApiMetaData(3, UNAVAILABLE_METADATA, 10, 2);
        boolean send = metadataGrpcDataSender.request(apiMetaData);

        Assertions.assertThat(send).isTrue();
        Thread.sleep(DEFAULT_TEST_HEDGING_DELAY_MILLIS * 6);
        Assertions.assertThat(requestCounter).isEqualTo(2);
    }


    private InProcessChannelBuilder getInProcessChannelBuilder() {
        return InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .intercept(new TestClientInterceptor())
                .enableRetry()
                //.retryBufferSize()
                //.perRpcBufferLimit()
                ;
    }

    public class TestClientInterceptor implements ClientInterceptor {
        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
            final ClientCall<ReqT, RespT> clientCall = channel.newCall(methodDescriptor, callOptions);
            return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(clientCall) {
                @Override
                public void start(Listener<RespT> responseListener, Metadata headers) {
                    logger.info("request, testId: {}, client time: {}", testId, new Timestamp(System.currentTimeMillis()).toString());

                    headers.put(TEST_ID_KEY, Integer.toString(testId));
                    super.start(responseListener, headers);
                }
            };
        }
    }

    private HedgingServiceConfigBuilder getTestServiceConfigBuilder() {
        HedgingServiceConfigBuilder serviceConfigBuilder = new HedgingServiceConfigBuilder();
        serviceConfigBuilder.setHedgingDelayMillis(DEFAULT_TEST_HEDGING_DELAY_MILLIS);
        return serviceConfigBuilder;
    }


    private MetadataGrpcHedgingDataSender<MetaDataType> getMetadataGrpcHedgingDataSender(InProcessChannelBuilder channelBuilder) {
        MetaDataMapper mapper = Mappers.getMapper(MetaDataMapper.class);
        GrpcMetadataMessageConverter converter = new GrpcMetadataMessageConverter(mapper);

        ChannelFactory factory = new ChannelFactory() {
            @Override
            public String getFactoryName() {
                return "inprocess-builder";
            }

            @Override
            public ManagedChannel build(String channelName, String host, int port) {
                return channelBuilder.build();
            }

            @Override
            public ManagedChannel build(String host, int port) {
                return channelBuilder.build();
            }

            @Override
            public void close() {
            }
        };

        return new MetadataGrpcHedgingDataSender<>("localhost", 1234, 1, converter, factory);
    }
}