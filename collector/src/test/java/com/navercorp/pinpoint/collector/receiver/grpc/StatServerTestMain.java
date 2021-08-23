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
import com.navercorp.pinpoint.collector.receiver.grpc.service.StatService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.StreamExecutorServerInterceptorFactory;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.springframework.beans.factory.FactoryBean;

import java.net.InetAddress;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class StatServerTestMain {
    public static final String IP = "0.0.0.0";
    public static final int PORT = 9999;

    public void run() throws Exception {
        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setBeanName("StatServer");

        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(PORT);
        grpcReceiver.setBindAddress(builder.build());

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        ServerServiceDefinition bindableService = newStatBindableService(executorService);
        grpcReceiver.setBindableServiceList(Collections.singletonList(bindableService));
        grpcReceiver.setAddressFilter(new MockAddressFilter());
        grpcReceiver.setExecutor(Executors.newFixedThreadPool(8));
        grpcReceiver.setEnable(true);
        grpcReceiver.setServerOption(ServerOption.newBuilder().build());

        grpcReceiver.afterPropertiesSet();

        grpcReceiver.blockUntilShutdown();
        grpcReceiver.destroy();
    }

    private ServerServiceDefinition newStatBindableService(Executor executor) throws Exception {
        GrpcStreamConfiguration streamConfiguration = newStreamConfiguration();


        FactoryBean<ServerInterceptor> interceptorFactory = new StreamExecutorServerInterceptorFactory(executor,
                Executors.newSingleThreadScheduledExecutor(),
                streamConfiguration);
        ServerInterceptor interceptor = interceptorFactory.getObject();
        StatService statService = new StatService(new MockDispatchHandler(), new DefaultServerRequestFactory());
        return ServerInterceptors.intercept(statService, interceptor);
    }

    private GrpcStreamConfiguration newStreamConfiguration() {
        GrpcStreamConfiguration.Builder builder = GrpcStreamConfiguration.newBuilder();
        builder.setCallInitRequestCount(100);
        builder.setSchedulerPeriodMillis(1000);
        builder.setSchedulerRecoveryMessageCount(100);
        return builder.build();
    }

    public static void main(String[] args) throws Exception {
        StatServerTestMain main = new StatServerTestMain();
        main.run();
    }

    private static class MockDispatchHandler implements DispatchHandler<GeneratedMessageV3, GeneratedMessageV3> {
        private static final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void dispatchSendMessage(ServerRequest<GeneratedMessageV3> serverRequest) {
            System.out.println("Dispatch send message " + serverRequest);
        }

        @Override
        public void dispatchRequestMessage(ServerRequest<GeneratedMessageV3> serverRequest, ServerResponse<GeneratedMessageV3> serverResponse) {
            System.out.println("Dispatch request message " + serverRequest + ", " + serverResponse);
            PResult pResult = PResult.newBuilder().setMessage("Success" + counter.getAndIncrement()).build();
            serverResponse.write(pResult);
        }
    }

    private static class MockAddressFilter implements AddressFilter {
        @Override
        public boolean accept(InetAddress address) {
            return true;
        }
    }

}
