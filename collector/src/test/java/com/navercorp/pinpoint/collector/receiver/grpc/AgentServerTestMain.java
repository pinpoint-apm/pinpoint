/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.grpc.service.AgentService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.DefaultServerRequestFactory;
import com.navercorp.pinpoint.collector.receiver.grpc.service.MetadataService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.ServerRequestFactory;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import com.navercorp.pinpoint.grpc.server.lifecycle.PingEventHandler;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import io.grpc.BindableService;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;

/**
 * @author jaehong.kim
 */
public class AgentServerTestMain {
    public static final String IP = "0.0.0.0";
    public static final int PORT = 9997;

    private final ServerRequestFactory serverRequestFactory = new DefaultServerRequestFactory();

    public void run() throws Exception {
        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setEnable(true);
        grpcReceiver.setBeanName("AgentServer");
        grpcReceiver.setBindIp(IP);
        grpcReceiver.setBindPort(PORT);

        PingEventHandler pingEventHandler = mock(PingEventHandler.class);
        BindableService agentService = new AgentService(new MockDispatchHandler(), pingEventHandler, Executors.newFixedThreadPool(8), serverRequestFactory);
        grpcReceiver.setBindableServiceList(Arrays.asList(agentService, new MetadataService(new MockDispatchHandler(), Executors.newFixedThreadPool(8), serverRequestFactory)));
        grpcReceiver.setAddressFilter(new MockAddressFilter());
        grpcReceiver.setExecutor(Executors.newFixedThreadPool(8));
        grpcReceiver.setServerOption(new ServerOption.Builder().build());


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

    private static class MockDispatchHandler implements DispatchHandler {
        private static final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void dispatchSendMessage(ServerRequest serverRequest) {
            System.out.println("Dispatch send message " + serverRequest);
        }

        @Override
        public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
            System.out.println("Dispatch request message " + serverRequest + ", " + serverResponse);
            if (serverRequest.getData() instanceof PApiMetaData) {
                PApiMetaData apiMetaData = (PApiMetaData) serverRequest.getData();
                serverResponse.write(PResult.newBuilder().setMessage(String.valueOf(apiMetaData.getApiId())).build());
            } else {
                serverResponse.write(PResult.newBuilder().setMessage("Success " + counter.getAndIncrement()).build());
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
