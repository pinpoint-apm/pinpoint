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
import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.ExecutorUtils;
import com.navercorp.pinpoint.grpc.ManagedChannelUtils;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.logging.ThrottledLogger;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import com.google.protobuf.GeneratedMessageV3;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class GrpcDataSender implements DataSender<Object> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final String name;
    protected final String host;
    protected final int port;

    protected final ManagedChannel managedChannel;
    protected final long logId;

    // not thread safe
    protected final MessageConverter<GeneratedMessageV3> messageConverter;

    protected final ExecutorService executor;

    protected final ChannelFactory channelFactory;

    protected volatile boolean shutdown;
    
    protected final BlockingQueue<Object> queue;
    protected final ThrottledLogger tLogger;


    public GrpcDataSender(String host, int port,
                          int executorQueueSize,
                          MessageConverter<GeneratedMessageV3> messageConverter,
                          ChannelFactory channelFactory) {
        this.channelFactory = Assert.requireNonNull(channelFactory, "channelFactory");

        this.name = Assert.requireNonNull(channelFactory.getFactoryName(), "channelFactory.name");
        this.host = Assert.requireNonNull(host, "host");
        this.port = port;

        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter");

        this.executor = newExecutorService(name + "-Executor", executorQueueSize);

        this.managedChannel = channelFactory.build(host, port);
        this.logId = ManagedChannelUtils.getLogId(managedChannel);

        final ConnectivityState state = managedChannel.getState(false);
        this.managedChannel.notifyWhenStateChanged(state, new ConnectivityStateMonitor(state));


        this.tLogger = ThrottledLogger.getLogger(logger, 100);
        this.queue = new LinkedBlockingQueue<Object>(executorQueueSize);
    }

    public long getLogId() {
        return logId;
    }

    private class ConnectivityStateMonitor implements Runnable {
        private final ConnectivityState before;

        public ConnectivityStateMonitor(ConnectivityState before) {
            this.before = Assert.requireNonNull(before, "before");
        }

        @Override
        public void run() {
            ConnectivityState change = managedChannel.getState(false);
            logger.info("ConnectivityState changed before:{}, change:{}", before, change);
            managedChannel.notifyWhenStateChanged(change, new ConnectivityStateMonitor(change));
        }
    }

    protected ExecutorService newExecutorService(String name, int senderExecutorQueueSize) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        return ExecutorFactory.newFixedThreadPool(1, senderExecutorQueueSize, threadFactory);
    }

    @Override
    public boolean send(final Object data) {
        if (this.queue.offer(data)) {
            return true;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("reject message queue size:{}", this.queue.size());
        } else {
            if (tLogger.isInfoEnabled()) {
                tLogger.info("reject message queue size : {}", this.queue.size());
            }
        }
        return false;
    }


    protected void release() {
        ExecutorUtils.shutdownExecutorService(name, executor);
        final ManagedChannel managedChannel = this.managedChannel;
        if (managedChannel != null) {
            ManagedChannelUtils.shutdownManagedChannel(name, managedChannel);
        }
        final ChannelFactory channelFactory = this.channelFactory;
        if (channelFactory != null) {
            channelFactory.close();
        }
    }
}