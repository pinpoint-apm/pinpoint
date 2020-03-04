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
import com.navercorp.pinpoint.grpc.trace.PAgentStat;
import com.navercorp.pinpoint.grpc.trace.PAgentStatBatch;
import com.navercorp.pinpoint.grpc.trace.PStatMessage;
import com.navercorp.pinpoint.grpc.trace.StatGrpc;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class StatClientMock {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManagedChannel channel;
    private final StatGrpc.StatStub statStub;

    public StatClientMock(final String host, final int port) {
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);
        HeaderFactory headerFactory = new AgentHeaderFactory("mockAgentId", "mockApplicationName", System.currentTimeMillis());
        final Metadata extraHeaders = headerFactory.newHeader();
        final ClientInterceptor headersInterceptor = MetadataUtils.newAttachHeadersInterceptor(extraHeaders);
        builder.intercept(headersInterceptor);
        builder.usePlaintext();

        channel = builder.build();
        this.statStub = StatGrpc.newStub(channel);
    }

    public void stop() throws InterruptedException {
        stop(5);
    }

    public void stop(long await) throws InterruptedException {
        channel.shutdown().awaitTermination(await, TimeUnit.SECONDS);
    }

    public void agentStat() {
        agentStat(1);
    }

    public void agentStat(final int count) {
        StreamObserver<Empty> responseObserver = getResponseObserver();

        StreamObserver<PStatMessage> requestObserver = statStub.sendAgentStat(responseObserver);
        for (int i = 0; i < count; i++) {
            final PAgentStat agentStat = PAgentStat.newBuilder().build();
            final PStatMessage statMessage = PStatMessage.newBuilder().setAgentStat(agentStat).build();
            requestObserver.onNext(statMessage);
        }
        requestObserver.onCompleted();
    }

    public void agentStatBatch() {
        agentStatBatch(1);
    }

    public void agentStatBatch(final int count) {
        StreamObserver<Empty> responseObserver = getResponseObserver();

        StreamObserver<PStatMessage> requestObserver = statStub.sendAgentStat(responseObserver);
        for (int i = 0; i < count; i++) {
            final PAgentStatBatch agentStatBatch = PAgentStatBatch.newBuilder().build();
            final PStatMessage statMessage = PStatMessage.newBuilder().setAgentStatBatch(agentStatBatch).build();
            requestObserver.onNext(statMessage);
        }
        requestObserver.onCompleted();
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
