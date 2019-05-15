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

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.ExecutorFactory;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.ExecutorUtils;
import com.navercorp.pinpoint.grpc.HeaderFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import com.google.protobuf.Empty;
import com.google.protobuf.GeneratedMessageV3;
import io.grpc.ManagedChannel;
import io.grpc.NameResolverProvider;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
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
public abstract class GrpcDataSender implements DataSender<Object> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final String name;
    protected final ManagedChannel managedChannel;

    // not thread safe
    protected final MessageConverter<GeneratedMessageV3> messageConverter;

    protected final ThreadPoolExecutor executor;

    protected final ChannelFactory channelFactory;

    protected volatile boolean shutdown;

    protected static ScheduledExecutorService reconnectScheduler
            = Executors.newScheduledThreadPool(1, new PinpointThreadFactory("pinpoint-reconnect-thread"));

    private ThreadPoolExecutor newExecutorService(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(name, true);
        return ExecutorFactory.newFixedThreadPool(1, 1000, threadFactory);
    }

    public GrpcDataSender(String name, String host, int port, MessageConverter<GeneratedMessageV3> messageConverter, HeaderFactory headerFactory,
                          NameResolverProvider nameResolverProvider) {
        this.name = Assert.requireNonNull(name, "name must not be null");
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter must not be null");

        this.executor = newExecutorService(name);

        this.channelFactory = newChannelFactory(name, headerFactory, nameResolverProvider);
        this.managedChannel = channelFactory.build(name, host, port);
    }

    private ChannelFactory newChannelFactory(String name, HeaderFactory headerFactory, NameResolverProvider nameResolverProvider) {
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

    public abstract boolean send0(Object data);

    @Override
    public void stop() {
        shutdown = true;
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

    public class ResponseStreamObserver<T> implements ClientResponseObserver<T, Empty> {

        private final ReconnectJob reconnectJob;

        public ResponseStreamObserver(ReconnectJob reconnectJob) {
            this.reconnectJob = Assert.requireNonNull(reconnectJob, "reconnectJob");
        }

        @Override
        public void beforeStart(ClientCallStreamObserver<T> requestStream) {
            requestStream.setOnReadyHandler(new Runnable() {
                @Override
                public void run() {
                    logger.info("connect to {} completed.", name);
                    reconnectJob.resetBackoffNanos();
                }
            });
        }

        @Override
        public void onNext(Empty value) {
            logger.debug("[{}] onNext:{}", name, value);
        }

        @Override
        public void onError(Throwable t) {
            logger.info("{} onError:{}", name, t);
            reconnect(reconnectJob);
        }

        @Override
        public void onCompleted() {
            logger.debug("{} onCompleted", name);
        }
    }

}