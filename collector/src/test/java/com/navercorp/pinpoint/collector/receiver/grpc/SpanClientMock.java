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

import com.google.protobuf.Empty;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.trace.PSpanMessage;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SpanClientMock {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManagedChannel channel;
    private final SpanGrpc.SpanStub spanStub;

    public SpanClientMock(final String host, final int port) throws Exception {
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);
        HeaderFactory headerFactory = new AgentHeaderFactory("mockAgentId", "mockApplicationName", System.currentTimeMillis());
        final Metadata extraHeaders = headerFactory.newHeader();
        final ClientInterceptor headersInterceptor = MetadataUtils.newAttachHeadersInterceptor(extraHeaders);
        builder.intercept(headersInterceptor);
        builder.usePlaintext();

        channel = builder.build();
        this.spanStub = SpanGrpc.newStub(channel);
        logger.info("CallOptions={}, channel={}", spanStub.getCallOptions(), spanStub.getChannel());
    }

    public void stop() throws InterruptedException {
        stop(5);
    }

    public void stop(long await) throws InterruptedException {
        channel.shutdown().awaitTermination(await, TimeUnit.SECONDS);
    }

    public void span() {
        span(1);
    }

    ExecutorService service = Executors.newFixedThreadPool(1);

    public void span(int count) {
        service.execute(new Runnable() {
            @Override
            public void run() {
                StreamObserver<Empty> responseObserver = getResponseObserver();

                StreamObserver<PSpanMessage> requestObserver = spanStub.sendSpan(responseObserver);
                for (int i = 0; i < count; i++) {
                    final PSpan span = PSpan.newBuilder().build();
                    final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpan(span).build();
                    requestObserver.onNext(spanMessage);
                    try {
                        TimeUnit.SECONDS.sleep(1);

                    } catch (InterruptedException e) {
                    }
                }
                requestObserver.onCompleted();
            }
        });
    }

    public void spanChunk() {
        spanChunk(1);
    }

    public void spanChunk(final int count) {
        service.execute(new Runnable() {
            @Override
            public void run() {

                StreamObserver<Empty> responseObserver = getResponseObserver();

                StreamObserver<PSpanMessage> requestObserver = spanStub.sendSpan(responseObserver);
                for (int i = 0; i < count; i++) {
                    final PSpanChunk spanChunk = PSpanChunk.newBuilder().build();
                    final PSpanMessage spanMessage = PSpanMessage.newBuilder().setSpanChunk(spanChunk).build();
                    requestObserver.onNext(spanMessage);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
                requestObserver.onCompleted();
            }
        });

    }

    private StreamObserver<Empty> getResponseObserver() {
        StreamObserver<Empty> responseObserver = new StreamObserver<Empty>() {
            @Override
            public void onNext(Empty pResult) {
                logger.info("Response {}", pResult);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.info("Error ", throwable);
            }

            @Override
            public void onCompleted() {
                logger.info("Completed");
            }
        };
        return responseObserver;
    }
}
