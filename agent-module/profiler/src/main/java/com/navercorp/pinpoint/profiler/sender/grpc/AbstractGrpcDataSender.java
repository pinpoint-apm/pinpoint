/*
 * Copyright 2024 NAVER Corp.
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
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.common.profiler.message.MessageConverter;
import com.navercorp.pinpoint.grpc.ManagedChannelUtils;
import com.navercorp.pinpoint.grpc.client.ChannelFactory;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public abstract class AbstractGrpcDataSender<T> implements DataSender<T> {
    protected final Logger logger = LogManager.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();
    protected final ThrottledLogger tLogger;

    protected final String name;
    protected final String host;
    protected final int port;

    protected final ManagedChannel managedChannel;
    protected final long logId;

    // not thread safe
    protected final MessageConverter<T, GeneratedMessageV3> messageConverter;

    protected final ChannelFactory channelFactory;

    protected volatile boolean shutdown;


    public AbstractGrpcDataSender(String host, int port,
                                  MessageConverter<T, GeneratedMessageV3> messageConverter,
                                  ChannelFactory channelFactory) {
        this.tLogger = ThrottledLogger.getLogger(logger, 100);

        this.channelFactory = Objects.requireNonNull(channelFactory, "channelFactory");

        this.name = Objects.requireNonNull(channelFactory.getFactoryName(), "channelFactory.name");
        this.host = Objects.requireNonNull(host, "host");
        this.port = port;

        this.messageConverter = Objects.requireNonNull(messageConverter, "messageConverter");

        this.managedChannel = channelFactory.build(host, port);
        this.logId = ManagedChannelUtils.getLogId(managedChannel);

        final ConnectivityState state = managedChannel.getState(false);
        this.managedChannel.notifyWhenStateChanged(state, new ConnectivityStateMonitor(state));

    }

    public long getLogId() {
        return logId;
    }

    private class ConnectivityStateMonitor implements Runnable {
        private final ConnectivityState before;

        public ConnectivityStateMonitor(ConnectivityState before) {
            this.before = Objects.requireNonNull(before, "before");
        }

        @Override
        public void run() {
            final ConnectivityState change = managedChannel.getState(false);
            logger.info("ConnectivityState changed before:{}, change:{}", before, change);
            if (change == ConnectivityState.TRANSIENT_FAILURE) {
                logger.info("Failed to connect to collector server {} {}/{}", name, host, port);
            }
            managedChannel.notifyWhenStateChanged(change, new ConnectivityStateMonitor(change));
        }
    }



    protected void releaseChannel() {
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