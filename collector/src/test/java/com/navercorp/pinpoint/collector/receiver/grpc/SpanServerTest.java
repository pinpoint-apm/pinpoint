/*
 * Copyright 2018 NAVER Corp.
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

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.TraceGrpc;
import com.navercorp.pinpoint.io.request.ServerRequest;
import com.navercorp.pinpoint.io.request.ServerResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.Ignore;
import org.junit.Test;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

public class SpanServerTest {

    @Ignore
    @Test
    public void service() throws Exception {
        SpanServer spanServer = new SpanServer();
        spanServer.setEnable(true);
        spanServer.setBeanName("SpanServer");
        spanServer.setBindIp("0.0.0.0");
        spanServer.setBindPort(28080);

        spanServer.setDispatchHandler(new MockDispatchHandler());
        spanServer.setAddressFilter(new MockAddressFilter());

        spanServer.afterPropertiesSet();

        SpanClient spanClient = new SpanClient("localhost", 28080);

        spanClient.record();

        TimeUnit.SECONDS.sleep(3);
        spanClient.stop();

        TimeUnit.SECONDS.sleep(3);
        //spanServer.blockUntilShutdown();
        spanServer.destroy();
    }


    private static class SpanClient {
        private final ManagedChannel channel;
        private final TraceGrpc.TraceStub traceStub;

        public SpanClient(final String host, final int port) {
            this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().keepAliveTime(10, TimeUnit.SECONDS).build();
            this.traceStub = TraceGrpc.newStub(channel);
        }

        public void stop() throws InterruptedException {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }

        public void record() throws InterruptedException {
            StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty empty) {
                    System.out.println("Response");
                }

                @Override
                public void onError(Throwable throwable) {
                    System.out.println("Failed to record");
                    throwable.printStackTrace();
                }

                @Override
                public void onCompleted() {
                }
            };

            StreamObserver<PSpan> requestObserver = traceStub.sendSpan(responseObserver);
            for (int i = 0; i < 1; i++) {
                PSpan pSpan = PSpan.newBuilder().build();
                requestObserver.onNext(pSpan);
                System.out.println("Request " + pSpan);
                TimeUnit.MILLISECONDS.sleep(1);
            }
            requestObserver.onCompleted();
        }
    }

    private static class MockDispatchHandler implements DispatchHandler {
        @Override
        public void dispatchSendMessage(ServerRequest serverRequest) {
            System.out.println("Dispatch send message " + serverRequest);
        }

        @Override
        public void dispatchRequestMessage(ServerRequest serverRequest, ServerResponse serverResponse) {
            System.out.println("Dispatch Request message " + serverRequest + ", " + serverResponse);
        }
    }

    private static class MockAddressFilter implements AddressFilter {
        @Override
        public boolean accept(InetAddress address) {
            return true;
        }
    }
}