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

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.ClientOption;
import com.navercorp.pinpoint.grpc.client.DefaultChannelFactoryBuilder;
import com.navercorp.pinpoint.grpc.client.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.MetadataGrpc;
import com.navercorp.pinpoint.grpc.trace.PApiMetaData;
import com.navercorp.pinpoint.grpc.trace.PResult;
import com.navercorp.pinpoint.grpc.trace.PSqlMetaData;
import com.navercorp.pinpoint.grpc.trace.PStringMetaData;
import com.navercorp.pinpoint.profiler.sender.grpc.RetryResponseStreamObserver;
import com.navercorp.pinpoint.profiler.sender.grpc.RetryScheduler;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
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
    private final Timer retryTimer;
    private final RetryScheduler<GeneratedMessageV3, PResult> retryScheduler;

    private volatile MetadataGrpc.MetadataStub metadataStub;
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final AtomicInteger responseCounter = new AtomicInteger(0);
    private final List<String> responseList = new ArrayList<>();

    public MetadataClientMock(final String host, final int port, final boolean agentHeader) {


        this.retryTimer = newTimer(this.getClass().getName());
        this.channelFactory = newChannelFactory();
        this.channel = channelFactory.build(host, port);

        this.metadataStub = MetadataGrpc.newStub(channel);
        this.retryScheduler = new RetryScheduler<GeneratedMessageV3, PResult>() {
            @Override
            public boolean isSuccess(PResult response) {
                return response.getSuccess();
            }

            @Override
            public void scheduleNextRetry(GeneratedMessageV3 request, int remainingRetryCount) {
                MetadataClientMock.this.scheduleNextRetry(request, remainingRetryCount);
            }
        };
    }

    private ChannelFactory newChannelFactory() {
        HeaderFactory headerFactory = new AgentHeaderFactory("mockAgentId", "mockApplicationName", System.currentTimeMillis());
        ChannelFactoryBuilder channelFactoryBuilder = new DefaultChannelFactoryBuilder("MetadataClientMock");
        channelFactoryBuilder.setHeaderFactory(headerFactory);
        channelFactoryBuilder.setClientOption(new ClientOption.Builder().build());
        return channelFactoryBuilder.build();
    }

    public void stop() throws InterruptedException {
        stop(5);
    }

    public void stop(long await) throws InterruptedException {
        channel.shutdown().awaitTermination(await, TimeUnit.SECONDS);
        channelFactory.close();
        retryTimer.stop();
    }

    private Timer newTimer(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        return new HashedWheelTimer(threadFactory, 100, TimeUnit.MILLISECONDS, 512, false, 100);
    }

    public void apiMetaData() {
        apiMetaData(1);
    }

    public void apiMetaData(final int count) {
        for (int i = 0; i < count; i++) {
            PApiMetaData request = PApiMetaData.newBuilder().setApiId(i).build();
            request(request, MAX_TOTAL_ATTEMPTS);
        }
    }

    public void sqlMetaData() {
        sqlMetaData(1);
    }

    public void sqlMetaData(final int count) {
        for (int i = 0; i < count; i++) {
            PSqlMetaData request = PSqlMetaData.newBuilder().build();
            request(request, MAX_TOTAL_ATTEMPTS);
        }
    }

    public void stringMetaData() {
        stringMetaData(1);
    }

    public void stringMetaData(final int count) {
        for (int i = 0; i < count; i++) {
            PStringMetaData request = PStringMetaData.newBuilder().build();
            request(request, MAX_TOTAL_ATTEMPTS);
        }
    }

    public List<String> getResponseList() {
        return responseList;
    }

    private void request(GeneratedMessageV3 message, int retryCount) {
        if (retryCount <= 0) {
            logger.warn("Drop message {}", debugLog(message));
            return;
        }

        if (message instanceof PSqlMetaData) {
            PSqlMetaData sqlMetaData = (PSqlMetaData) message;
            StreamObserver<PResult> responseObserver = newResponseObserver(message, retryCount);
            this.metadataStub.requestSqlMetaData(sqlMetaData, responseObserver);
        } else if (message instanceof PApiMetaData) {
            final PApiMetaData apiMetaData = (PApiMetaData) message;
            StreamObserver<PResult> responseObserver = newResponseObserver(message, retryCount);
            this.metadataStub.requestApiMetaData(apiMetaData, responseObserver);
        } else if (message instanceof PStringMetaData) {
            final PStringMetaData stringMetaData = (PStringMetaData) message;
            StreamObserver<PResult> responseObserver = newResponseObserver(message, retryCount);
            this.metadataStub.requestStringMetaData(stringMetaData, responseObserver);
        } else {
            logger.warn("Unsupported message {}", debugLog(message));
        }
        int requestCount = requestCounter.getAndIncrement();
        logger.info("Request {} {}", requestCount, message);
        Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

    private void scheduleNextRetry(GeneratedMessageV3 request, int remainingRetryCount) {
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                if (timeout.cancel()) {
                    return;
                }
                logger.info("Retry {} {}", remainingRetryCount, request);
                request(request, remainingRetryCount - 1);
            }
        };

        try {
            retryTimer.newTimeout(timerTask, 1000, TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            logger.debug("retry fail {}", e.getCause(), e);
        }
    }

    private StreamObserver<PResult> newResponseObserver(GeneratedMessageV3 message, int retryCount) {
        return new RetryResponseStreamObserver<GeneratedMessageV3, PResult>(logger, this.retryScheduler, message, retryCount);
    }

}