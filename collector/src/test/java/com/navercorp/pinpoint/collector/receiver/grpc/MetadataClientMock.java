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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryOption;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.MetadataGrpc;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.navercorp.pinpoint.grpc.MessageFormatUtils.debugLog;

public class MetadataClientMock {
    private static final int MAX_TOTAL_ATTEMPTS = 3;
    private static final ScheduledExecutorService RECONNECT_SCHEDULER
            = Executors.newScheduledThreadPool(1, new PinpointThreadFactory("Pinpoint-reconnect-thread"));

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ChannelFactory channelFactory;
    private final ManagedChannel channel;

    private volatile MetadataGrpc.MetadataStub metadataStub;
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final AtomicInteger responseCounter = new AtomicInteger(0);
    private final List<String> responseList = new ArrayList<>();

    public MetadataClientMock(final String host, final int port, final boolean agentHeader) throws Exception {
        HeaderFactory headerFactory = new AgentHeaderFactory("mockAgentId", "mockApplicationName", System.currentTimeMillis());
        ChannelFactoryOption.Builder builder = ChannelFactoryOption.newBuilder();
        builder.setHeaderFactory(headerFactory);

        this.channelFactory = new ChannelFactory(builder.build());
        this.channel = channelFactory.build("MetadataClientMock", host, port);

        this.metadataStub = MetadataGrpc.newStub(channel);
    }

    public void stop() throws InterruptedException {
        stop(5);
    }

    public void stop(long await) throws InterruptedException {
        channel.shutdown().awaitTermination(await, TimeUnit.SECONDS);
    }

    public void apiMetaData() throws InterruptedException {
        apiMetaData(1);
    }

    public void apiMetaData(final int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            PApiMetaData request = PApiMetaData.newBuilder().setApiId(i).build();
            request(request, MAX_TOTAL_ATTEMPTS);
        }
    }

    public void sqlMetaData() throws InterruptedException {
        sqlMetaData(1);
    }

    public void sqlMetaData(final int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            PSqlMetaData request = PSqlMetaData.newBuilder().build();
            request(request, MAX_TOTAL_ATTEMPTS);
        }
    }

    public void stringMetaData() throws InterruptedException {
        stringMetaData(1);
    }

    public void stringMetaData(final int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            PStringMetaData request = PStringMetaData.newBuilder().build();
            request(request, MAX_TOTAL_ATTEMPTS);
        }
    }

    public List<String> getResponseList() {
        return responseList;
    }

    private void request(GeneratedMessageV3 message, int retryCount) throws InterruptedException {
        if (retryCount <= 0) {
            logger.warn("Drop message {}", debugLog(message));
            return;
        }

        if (message instanceof PSqlMetaData) {
            PSqlMetaData sqlMetaData = (PSqlMetaData) message;
            this.metadataStub.requestSqlMetaData(sqlMetaData, new RetryResponseStreamObserver(message, retryCount));
        } else if (message instanceof PApiMetaData) {
            final PApiMetaData apiMetaData = (PApiMetaData) message;
            this.metadataStub.requestApiMetaData(apiMetaData, new RetryResponseStreamObserver(message, retryCount));
        } else if (message instanceof PStringMetaData) {
            final PStringMetaData stringMetaData = (PStringMetaData) message;
            this.metadataStub.requestStringMetaData(stringMetaData, new RetryResponseStreamObserver(message, retryCount));
        } else {
            logger.warn("Unsupported message {}", debugLog(message));
        }
        int requestCount = requestCounter.getAndIncrement();
        logger.info("Request {} {}", requestCount, message);
        TimeUnit.SECONDS.sleep(1);
    }

    class RetryResponseStreamObserver implements StreamObserver<PResult> {
        private final Logger logger = LoggerFactory.getLogger(this.getClass());
        private GeneratedMessageV3 message;
        private int retryCount;

        public RetryResponseStreamObserver(GeneratedMessageV3 message, int retryCount) {
            this.message = message;
            this.retryCount = retryCount;
        }

        @Override
        public void onNext(PResult result) {
            int count = responseCounter.getAndIncrement();
            logger.info("Response {} {}", count, result);
            responseList.add(result.getMessage());
        }

        @Override
        public void onError(Throwable throwable) {
            logger.info("Error ", throwable);
            channelFactory.getEventLoopGroup().schedule(new Runnable() {
                @Override
                public void run() {
                    logger.info("Retry {} {}", retryCount, message);
                    try {
                        request(message, retryCount - 1);
                    } catch (InterruptedException e) {
                    }
                }
            }, 1000, TimeUnit.MILLISECONDS);
        }

        @Override
        public void onCompleted() {
            logger.info("Completed");
        }
    }
}