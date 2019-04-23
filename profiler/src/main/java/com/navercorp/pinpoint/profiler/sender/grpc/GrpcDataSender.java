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

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.ExecutorFactory;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.trace.PSpan;
import com.navercorp.pinpoint.grpc.trace.PSpanChunk;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.ExecutorUtils;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.HeaderFactory;
import com.navercorp.pinpoint.grpc.trace.SpanGrpc;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import io.grpc.ManagedChannel;
import io.grpc.NameResolverProvider;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcDataSender implements DataSender<Object> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String name;
    private final ManagedChannel managedChannel;
    private final SpanGrpc.SpanStub spanStub;

    private volatile StreamObserver<PSpan> spanStream;
    private volatile StreamObserver<PSpanChunk> spanChunkStream;

    // not thread safe
    private final MessageConverter<GeneratedMessageV3> messageConverter;

    private final ThreadPoolExecutor executor;

    private final ChannelFactory channelFactory;

    private volatile boolean shutdown;

    private static ScheduledExecutorService reconnectScheduler
            = Executors.newScheduledThreadPool(1, new PinpointThreadFactory("pinpoint-reconnect-thread"));

    private ThreadPoolExecutor newExecutorService(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(name, true);
        return ExecutorFactory.newFixedThreadPool(1, 1000, threadFactory);
    }

    public GrpcDataSender(String name, String host, int port, MessageConverter<GeneratedMessageV3> messageConverter, HeaderFactory<AgentHeaderFactory.Header> headerFactory,
                          NameResolverProvider nameResolverProvider) {
        this.name = Assert.requireNonNull(name, "name must not be null");
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter must not be null");

        this.executor = newExecutorService(name);

        this.channelFactory = newChannelFactory(name, headerFactory, nameResolverProvider);
        this.managedChannel = channelFactory.build(name, host, port);

        this.spanStub = SpanGrpc.newStub(managedChannel);
        this.spanStream = newSpanStream();
        this.spanChunkStream = newSpanChunkStream();
    }

    private StreamObserver<PSpanChunk> newSpanChunkStream() {
        final ResponseStreamObserver responseObserver = new ResponseStreamObserver();
        final StreamObserver<PSpanChunk> pSpanChunkStreamObserver = spanStub.sendSpanChunk(responseObserver);

        responseObserver.setReconnectAction(new ExponentialBackoffReconnectJob() {
            @Override
            public void run() {
                spanChunkStream = spanStub.sendSpanChunk(responseObserver);
            }
        });
        return pSpanChunkStreamObserver;
    }

    private StreamObserver<PSpan> newSpanStream() {
        final ResponseStreamObserver responseObserver = new ResponseStreamObserver();
        StreamObserver<PSpan> pSpanStreamObserver = spanStub.sendSpan(responseObserver);

        responseObserver.setReconnectAction(new ExponentialBackoffReconnectJob() {
            @Override
            public void run() {
                spanStream = spanStub.sendSpan(responseObserver);
            }
        });

        return pSpanStreamObserver;
    }

    private ChannelFactory newChannelFactory(String name, HeaderFactory<AgentHeaderFactory.Header> headerFactory, NameResolverProvider nameResolverProvider) {
        return new ChannelFactory(name, headerFactory, nameResolverProvider);
    }

    @Override
    public boolean send(final Object data) {
        final Runnable command = new Runnable() {
            @Override
            public void run() {
                try {
                    send0(data);
                } catch (Exception ex) {
                    logger.debug("send fail:{}", data, ex);
                }
            }
        };
        try {
            executor.execute(command);
        } catch (RejectedExecutionException reject) {
            logger.debug("reject:{}", command);
            return false;
        }
        return true;
    }

    private boolean send0(Object data) {
        final GeneratedMessageV3 spanMessage = messageConverter.toMessage(data);
        if (logger.isDebugEnabled()) {
            logger.debug("message:{}", spanMessage);
        }
        if (spanMessage instanceof PSpanChunk) {
            final PSpanChunk pSpan = (PSpanChunk) spanMessage;
            spanChunkStream.onNext(pSpan);
            return true;
        }
        if (spanMessage instanceof PSpan) {
            final  PSpan pSpan = (PSpan) spanMessage;
            spanStream.onNext(pSpan);
            return true;
        }
        throw new IllegalStateException("unsupported message " + data);
    }


    @Override
    public void stop() {
        shutdown = true;
        spanStream.onCompleted();
        spanChunkStream.onCompleted();
        if (this.managedChannel != null) {
            this.managedChannel.shutdown();
        }
        ExecutorUtils.shutdownExecutorService(name, executor);
        this.channelFactory.close();
    }

    private void reconnect(ReconnectJob reconnectAction) {
        if (this.shutdown) {
            return;
        }
        logger.info("recreateStream");
        reconnectScheduler.schedule(reconnectAction, reconnectAction.nextBackoffNanos(), TimeUnit.NANOSECONDS);
    }



    private class ResponseStreamObserver implements StreamObserver<Empty> {
        private ReconnectJob runnable;

        public ResponseStreamObserver() {
        }

        void setReconnectAction(ReconnectJob runnable) {
            this.runnable = runnable;
        }

        @Override
        public void onNext(Empty value) {
            logger.debug("[{}] onNext:{}", name, value);
        }

        @Override
        public void onError(Throwable t) {
            logger.info("{} onError:{}", name, t);
            final ReconnectJob copy = this.runnable;
            if (copy != null) {
                reconnect(copy);
            }
        }

        @Override
        public void onCompleted() {
            logger.debug("{} onCompleted", name);
        }
    };
}