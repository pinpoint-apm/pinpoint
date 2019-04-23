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

import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.AgentGrpc;
import com.navercorp.pinpoint.grpc.trace.KeepAliveGrpc;
import com.navercorp.pinpoint.grpc.trace.PAgentInfo;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PPing;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AgentClientMock {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ManagedChannel channel;
    private final AgentGrpc.AgentBlockingStub agentStub;
    private final KeepAliveGrpc.KeepAliveStub keepAliveStub;


    public AgentClientMock(final String host, final int port) throws Exception {
        NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port);

        AgentHeaderFactory.Header header = new AgentHeaderFactory.Header("mockAgentId", "mockApplicationName", System.currentTimeMillis());
        HeaderFactory headerFactory = new AgentHeaderFactory(header);
        final Metadata extraHeaders = headerFactory.newHeader();
        final ClientInterceptor headersInterceptor = MetadataUtils.newAttachHeadersInterceptor(extraHeaders);
        builder.intercept(headersInterceptor);
        builder.usePlaintext();

        channel = builder.build();
        this.agentStub = AgentGrpc.newBlockingStub(channel);
        this.keepAliveStub = KeepAliveGrpc.newStub(channel);
    }

    public void stop() throws InterruptedException {
        stop(5);
    }

    public void stop(long await) throws InterruptedException {
        channel.shutdown().awaitTermination(await, TimeUnit.SECONDS);
    }

    public void info() throws InterruptedException {
        info(1);
    }

    public void info(final int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            PAgentInfo request = PAgentInfo.newBuilder().build();
            StreamObserver<PResult> responseObserver = getResponseObserver();
            PResult pResult = agentStub.requestAgentInfo(request);
            logger.info("Result {}", pResult);
        }
    }

    public void apiMetaData() throws InterruptedException {
        apiMetaData(1);
    }

    public void apiMetaData(final int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            PApiMetaData request = PApiMetaData.newBuilder().build();
            StreamObserver<PResult> responseObserver = getResponseObserver();
            PResult result = agentStub.requestApiMetaData(request);
        }
    }

    public void sqlMetaData() throws InterruptedException {
        sqlMetaData(1);
    }

    public void sqlMetaData(final int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            PSqlMetaData request = PSqlMetaData.newBuilder().build();
            StreamObserver<PResult> responseObserver = getResponseObserver();
            PResult result = agentStub.requestSqlMetaData(request);
        }
    }

    public void stringMetaData() throws InterruptedException {
        stringMetaData(1);
    }

    public void stringMetaData(final int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            PStringMetaData request = PStringMetaData.newBuilder().build();
            StreamObserver<PResult> responseObserver = getResponseObserver();
            PResult result = agentStub.requestStringMetaData(request);
        }
    }

    StreamObserver<PPing> requestObserver;

    public void pingPoing() {
        StreamObserver<PPing> responseObserver = new StreamObserver<PPing>() {
            @Override
            public void onNext(PPing ping) {
                logger.info("Response {}", ping);
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                }
                pingPong("ping");
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
        requestObserver = keepAliveStub.clientKeepAlive(responseObserver);
        requestObserver.onNext(PPing.newBuilder().build());
    }

    private void pingPong(final String message) {
        requestObserver.onNext(PPing.newBuilder().build());
    }


    private StreamObserver<PResult> getResponseObserver() {
        StreamObserver<PResult> responseObserver = new StreamObserver<PResult>() {
            @Override
            public void onNext(PResult pResult) {
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