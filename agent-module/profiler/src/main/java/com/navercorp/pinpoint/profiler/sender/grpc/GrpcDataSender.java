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

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.common.profiler.concurrent.ExecutorFactory;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.ExecutorUtils;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class GrpcDataSender<T> extends AbstractGrpcDataSender<T> {

    protected final ExecutorService executor;
    protected final BlockingQueue<T> queue;

    public GrpcDataSender(String host, int port,
                          int executorQueueSize,
                          MessageConverter<T, GeneratedMessageV3> messageConverter,
                          ChannelFactory channelFactory) {
        super(host, port, messageConverter, channelFactory);

        this.executor = newExecutorService(name + "-Executor", executorQueueSize);
        this.queue = new LinkedBlockingQueue<>(executorQueueSize);
    }

    protected ExecutorService newExecutorService(String name, int senderExecutorQueueSize) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        return ExecutorFactory.newFixedThreadPool(1, senderExecutorQueueSize, threadFactory);
    }

    @Override
    public boolean send(final T data) {
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
        super.releaseChannel();
    }
}