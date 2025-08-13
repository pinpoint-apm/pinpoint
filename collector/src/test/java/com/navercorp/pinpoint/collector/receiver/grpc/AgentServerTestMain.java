/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.grpc.lifecycle.PingEventHandler;
import com.navercorp.pinpoint.collector.handler.RequestResponseHandler;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.collector.receiver.grpc.service.AgentService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.DefaultServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.DefaultServerResponseFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.MetadataService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerResponseFactory;
import com.navercorp.pinpoint.collector.uid.service.EmptyApplicationUidService;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PExceptionMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PSqlUidMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import com.navercorp.pinpoint.io.request.UidFetchers;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;

/**
 * @author jaehong.kim
 */
public class AgentServerTestMain {
    public static final String IP = "0.0.0.0";
    public static final int PORT = 9997;

    private final ServerRequestFactory serverRequestFactory = new DefaultServerRequestFactory(UidFetchers.empty());
    private final ServerResponseFactory serverResponseFactory = new DefaultServerResponseFactory();

    public void run() throws Exception {
        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setEnable(true);
        grpcReceiver.setBeanName("AgentServer");

        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(PORT);
        builder.setIp(IP);
        grpcReceiver.setBindAddress(builder.build());

        PingEventHandler pingEventHandler = mock(PingEventHandler.class);
        RequestResponseHandler<PAgentInfo, PResult> mockDispatchHandler = new MockDispatchHandler<>();
        BindableService agentService = new AgentService(mockDispatchHandler, pingEventHandler, Executors.newFixedThreadPool(8), serverRequestFactory, serverResponseFactory);

        RequestResponseHandler<PApiMetaData, PResult> apiMetaDataHandler = new MockDispatchHandler<>();
        RequestResponseHandler<PSqlMetaData, PResult> sqlMetaDataHandler = new MockDispatchHandler<>();
        RequestResponseHandler<PSqlUidMetaData, PResult> sqlUidMetaDataHandler = new MockDispatchHandler<>();
        RequestResponseHandler<PStringMetaData, PResult> stringMetaDataHandler = new MockDispatchHandler<>();
        RequestResponseHandler<PExceptionMetaData, PResult> exceptionMetaDataHandler = new MockDispatchHandler<>();
        MetadataService metadataService = new MetadataService(apiMetaDataHandler,
                sqlMetaDataHandler, sqlUidMetaDataHandler,
                stringMetaDataHandler, exceptionMetaDataHandler,
                Executors.newFixedThreadPool(8), serverRequestFactory, serverResponseFactory);
        List<ServerServiceDefinition> serviceList = List.of(agentService.bindService(), metadataService.bindService());

        grpcReceiver.setBindAddress(builder.build());
        grpcReceiver.setAddressFilter(new MockAddressFilter());

        grpcReceiver.setBindableServiceList(serviceList);
        grpcReceiver.setAddressFilter(new MockAddressFilter());
        grpcReceiver.setExecutor(Executors.newFixedThreadPool(8));
        grpcReceiver.setServerOption(ServerOption.newBuilder().build());


        grpcReceiver.afterPropertiesSet();
        grpcReceiver.blockUntilShutdown();
        grpcReceiver.destroy();
    }

    public static void main(String[] args) throws Exception {
        AgentServerTestMain main = new AgentServerTestMain();
        try {
            main.run();
        } catch (Exception e) {
            System.out.println("Failed to run");
            e.printStackTrace();
        }
    }

    private static class MockDispatchHandler<Req> implements RequestResponseHandler<Req, PResult> {
        private static final AtomicInteger counter = new AtomicInteger(0);


        public MockDispatchHandler() {
        }

        @Override
        public void handleRequest(ServerRequest<Req> serverRequest, ServerResponse<PResult> serverResponse) {
            System.out.println("Dispatch request message " + serverRequest + ", " + serverResponse);
            if (serverRequest.getData() instanceof PApiMetaData apiMetaData) {
                PResult result = PResult.newBuilder().setMessage(String.valueOf(apiMetaData.getApiId())).build();
                serverResponse.write(result);
            } else {
                PResult result = PResult.newBuilder().setMessage("Success " + counter.getAndIncrement()).build();
                serverResponse.write(result);
            }
        }
    }

    private static class MockAddressFilter implements AddressFilter {
        @Override
        public boolean accept(InetAddress address) {
            return true;
        }
    }
}
