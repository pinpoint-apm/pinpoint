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

package com.navercorp.pinpoint.grpc.client;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.HeaderFactory;
import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.InternalNettyChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ChannelFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String name;
    private final String host;
    private final int port;

    private final HeaderFactory headerFactory;

    public ChannelFactory(String name, String host, int port, HeaderFactory headerFactory) {
        this.name = Assert.requireNonNull(name, "name must not be null");
        this.host = Assert.requireNonNull(host, "host must not be null");
        this.port = port;
        this.headerFactory = Assert.requireNonNull(headerFactory, "headerFactory must not be null");
    }



    public ManagedChannel build() {
        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port);
        channelBuilder.usePlaintext();

        setupInternal(channelBuilder);

        addHeader(channelBuilder);

        ManagedChannel channel = channelBuilder.build();
        setChannelStateNotifier(channel, name);

        return channel;
    }

    private void setupInternal(NettyChannelBuilder channelBuilder) {
        InternalNettyChannelBuilder.setStatsEnabled(channelBuilder, false);
        InternalNettyChannelBuilder.setTracingEnabled(channelBuilder, false);
        InternalNettyChannelBuilder.setStatsRecordStartedRpcs(channelBuilder, false);
    }

    private void addHeader(NettyChannelBuilder channelBuilder) {
        final Metadata extraHeaders = headerFactory.newHeader();
        if (logger.isDebugEnabled()) {
            logger.debug("addHeader {}", extraHeaders);
        }
        final ClientInterceptor headersInterceptor = MetadataUtils.newAttachHeadersInterceptor(extraHeaders);
        channelBuilder.intercept(headersInterceptor);
    }

    private void setChannelStateNotifier(ManagedChannel channel, final String name) {
        if (logger.isDebugEnabled()) {
            logger.debug("setChannelStateNotifier()");
        }
        channel.notifyWhenStateChanged(ConnectivityState.CONNECTING, new Runnable() {
            @Override
            public void run() {
                logger.info("{} CONNECTING", name);
            }
        });
        channel.notifyWhenStateChanged(ConnectivityState.READY, new Runnable() {
            @Override
            public void run() {
                logger.info("{} READY", name);
            }
        });
        channel.notifyWhenStateChanged(ConnectivityState.IDLE, new Runnable() {
            @Override
            public void run() {
                logger.info("{} IDLE", name);
            }
        });
        channel.notifyWhenStateChanged(ConnectivityState.SHUTDOWN, new Runnable() {
            @Override
            public void run() {
                logger.info("{} SHUTDOWN", name);
            }
        });
        channel.notifyWhenStateChanged(ConnectivityState.TRANSIENT_FAILURE, new Runnable() {
            @Override
            public void run() {
                logger.info("{} TRANSIENT_FAILURE", name);
            }
        });

        final ConnectivityState state = channel.getState(false);
        if (logger.isDebugEnabled()) {
            logger.debug("getState(){}", state);
        }


    }
}
