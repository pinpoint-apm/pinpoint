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
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentServerTest {

    @Ignore
    @Test
    public void service() throws Exception {
        AgentServer server = new AgentServer();
        server.setBeanName("AgentServer");
        server.setBindIp("0.0.0.0");
        server.setBindPort(28081);

        server.setDispatchHandler(new MockDispatchHandler());
        server.setAddressFilter(new MockAddressFilter());

//        SslContextBuilder builder = GrpcSslContexts.forServer(TlsCertUtils.loadCert("server1.pem"), TlsCertUtils.loadCert("server1.key"));
//        builder.clientAuth(ClientAuth.REQUIRE);
//        builder.trustManager(TlsCertUtils.loadCert("ca.pem"));
//        builder.ciphers(TlsCertUtils.preferredTestCiphers(), SupportedCipherSuiteFilter.INSTANCE);
//        server.setSslContext(builder.build());

        server.afterPropertiesSet();

        AgentClient client = new AgentClient("localhost", 28081);
//        for(int i = 0; i < 3; i++) {
        client.info();
//        }
        TimeUnit.SECONDS.sleep(3);
        client.stop();

        server.blockUntilShutdown();
        server.destroy();
    }

    public void abortClient() {

    }


    private static class AgentClient {
        private final ManagedChannel channel;
        private final AgentGrpc.AgentStub agentStub;

        public AgentClient(final String host, final int port) throws Exception {
            NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);
            builder.usePlaintext();


//            SslContextBuilder sslContextBuilder = GrpcSslContexts.forClient();
//            sslContextBuilder.keyManager(TlsCertUtils.loadCert("client.pem"), TlsCertUtils.loadCert("client.key"));
//            sslContextBuilder.trustManager(TlsCertUtils.loadX509Cert("ca.pem"));
//            sslContextBuilder.ciphers(TlsCertUtils.preferredTestCiphers(), SupportedCipherSuiteFilter.INSTANCE);
//            builder.sslContext(sslContextBuilder.build());

            channel = builder.build();
            this.agentStub = AgentGrpc.newStub(channel);
        }

        public void stop() throws InterruptedException {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }

        public void info() throws InterruptedException {
            PAgentInfo request = PAgentInfo.newBuilder().setAgentId("agentId").build();
            StreamObserver<PResult> responseObserver = new StreamObserver<PResult>() {
                @Override
                public void onNext(PResult pResult) {
                    System.out.println("Result " + pResult);
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("Error " + throwable.getMessage());
                }

                @Override
                public void onCompleted() {
                    System.out.println("Completed");
                }
            };

            for (int i = 0; i < 3; i++) {
                agentStub.requestAgentInfo(request, responseObserver);
                TimeUnit.MILLISECONDS.sleep(1);
                System.out.println("COUNT " + i);
            }
        }
    }

    private static class MockDispatchHandler implements DispatchHandler {
        private static AtomicInteger counter = new AtomicInteger(0);

        @Override
        public void dispatchSendMessage(ServerRequest serverRequest) {
            System.out.println("Dispatch send message " + serverRequest);
        }

        @Override
        public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
            System.out.println("Dispatch request message " + serverRequest + ", " + serverResponse);
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