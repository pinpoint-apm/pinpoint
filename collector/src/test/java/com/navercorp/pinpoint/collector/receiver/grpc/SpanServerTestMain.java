/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.collector.grpc.config.GrpcStreamConfiguration;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.grpc.service.DefaultServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.SpanService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.StreamExecutorServerInterceptorFactory;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.grpc.server.AgentHeaderReader;
import com.navercorp.pinpoint.grpc.server.HeaderPropagationInterceptor;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.springframework.beans.factory.FactoryBean;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jaehong.kim
 */
public class SpanServerTestMain {
    public static final String IP = "0.0.0.0";
    public static final int PORT = 9993;
    private static final AtomicInteger IncomingCounter = new AtomicInteger(0);

    public void run() throws Exception {
//        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);

        Logger logger = Logger.getLogger("io.grpc");
        logger.setLevel(Level.FINER);
        logger.addHandler(new ConsoleHandler());

        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setBeanName("TraceServer");
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setIp(IP);
        builder.setPort(PORT);
        grpcReceiver.setBindAddress(builder.build());

        Executor executor = newWorkerExecutor(8);
        ServerServiceDefinition bindableService = newSpanBindableService(executor);
        grpcReceiver.setBindableServiceList(Collections.singletonList(bindableService));
        grpcReceiver.setAddressFilter(new MockAddressFilter());
        grpcReceiver.setExecutor(Executors.newFixedThreadPool(8));
        grpcReceiver.setEnable(true);
        grpcReceiver.setServerOption(ServerOption.newBuilder().build());

        AgentHeaderReader agentHeaderReader = new AgentHeaderReader("test");
        HeaderPropagationInterceptor interceptor = new HeaderPropagationInterceptor(agentHeaderReader);
        grpcReceiver.setServerInterceptorList(Arrays.asList(interceptor));

//        for(int i = 0; i < 9999; i++) {
        grpcReceiver.afterPropertiesSet();

        grpcReceiver.blockUntilShutdown();
//            TimeUnit.SECONDS.sleep(30);
//            System.out.println("###### SHUTDOWN");
//            grpcReceiver.destroy();
//            grpcReceiver.blockUntilShutdown();
//            System.out.println("###### START");
//            TimeUnit.SECONDS.sleep(30);

    }

    private ServerServiceDefinition newSpanBindableService(Executor executor) throws Exception {
        GrpcStreamConfiguration streamConfiguration = newStreamConfiguration();

        FactoryBean<ServerInterceptor> interceptorFactory = new StreamExecutorServerInterceptorFactory(executor,
                Executors.newSingleThreadScheduledExecutor(), streamConfiguration);
        ((StreamExecutorServerInterceptorFactory) interceptorFactory).setBeanName("SpanService");

        ServerInterceptor interceptor = interceptorFactory.getObject();
        SpanService spanService = new SpanService(new MockDispatchHandler(), new DefaultServerRequestFactory());
        return ServerInterceptors.intercept(spanService, interceptor);
    }

    private GrpcStreamConfiguration newStreamConfiguration() {
        GrpcStreamConfiguration.Builder builder = GrpcStreamConfiguration.newBuilder();
        builder.setCallInitRequestCount(100);
        builder.setSchedulerPeriodMillis(1000);
        builder.setSchedulerRecoveryMessageCount(100);
        return builder.build();
    }

    private ExecutorService newWorkerExecutor(int thread) {
        return new ThreadPoolExecutor(thread, thread,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(thread * 2));
    }

    public static void main(String[] args) throws Exception {
        SpanServerTestMain main = new SpanServerTestMain();
        main.run();
    }

    private static class MockDispatchHandler implements DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> {
        private static final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void dispatchSendMessage(ServerRequest<GeneratedMessageV3> serverRequest) {
//            System.out.println("## Incoming " + IncomingCounter.addAndGet(1));
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ignore) {
            }

            final GeneratedMessageV3 data = serverRequest.getData();
            if (data instanceof PSpan) {
                PSpan span = (PSpan) data;
                System.out.println("Dispatch send message " + span.getSpanId());
            } else {
                System.out.println("Invalid send message " + serverRequest.getData());
            }
        }

        @Override
        public void dispatchRequestMessage(ServerRequest<GeneratedMessageV3> serverRequest, ServerResponse<GeneratedMessageV3> serverResponse) {
//            System.out.println("Dispatch request message " + serverRequest + ", " + serverResponse);
            serverResponse.write(PResult.newBuilder().setMessage("Success" + counter.getAndIncrement()).build());
        }
    }

    private static class MockAddressFilter implements AddressFilter {
        @Override
        public boolean accept(InetAddress address) {
            return true;
        }
    }
}