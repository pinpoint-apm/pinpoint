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
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import com.navercorp.pinpoint.grpc.client.ChannelFactoryOption;
import com.navercorp.pinpoint.profiler.context.thrift.MessageConverter;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import com.google.protobuf.GeneratedMessageV3;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class GrpcDataSender implements DataSender<Object> {
    protected static ScheduledExecutorService reconnectScheduler
            = Executors.newScheduledThreadPool(1, new PinpointThreadFactory("Pinpoint-reconnect-thread"));

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final String name;
    protected final ManagedChannel managedChannel;

    // not thread safe
    protected final MessageConverter<GeneratedMessageV3> messageConverter;

    protected final ThreadPoolExecutor executor;

    protected final ChannelFactory channelFactory;

    protected volatile boolean shutdown;


    public GrpcDataSender(String host, int port, int executorQueueSize, MessageConverter<GeneratedMessageV3> messageConverter, ChannelFactoryOption channelFactoryOption) {
        Assert.requireNonNull(channelFactoryOption, "channelFactoryOption must not be null");

        this.name = Assert.requireNonNull(channelFactoryOption.getName(), "name must not be null");
        this.messageConverter = Assert.requireNonNull(messageConverter, "messageConverter must not be null");

        this.executor = newExecutorService(name + "-Executor", executorQueueSize);

        this.channelFactory = new ChannelFactory(channelFactoryOption);
        this.managedChannel = channelFactory.build(name, host, port);
    }

    private ThreadPoolExecutor newExecutorService(String name, int senderExecutorQueueSize) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        return ExecutorFactory.newFixedThreadPool(1, senderExecutorQueueSize, threadFactory);
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


}