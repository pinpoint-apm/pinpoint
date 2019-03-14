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
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.ExecutorUtils;
import com.navercorp.pinpoint.grpc.HeaderFactory;
import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.NameResolverProvider;
import io.grpc.internal.PinpointDnsNameResolverProvider;
import io.grpc.netty.InternalNettyChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ChannelFactory {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String name;
    private final HeaderFactory headerFactory;

    private final NioEventLoopGroup eventLoopGroup;

    private final ExecutorService eventLoopExecutor;

    private final ExecutorService executorService;
    private final int executorQueueSize = 1024;

    private final NameResolverProvider nameResolverProvider;

    public ChannelFactory(String name, HeaderFactory headerFactory) {
        this(name, headerFactory, null);
    }

    public ChannelFactory(String name, HeaderFactory headerFactory, NameResolverProvider nameResolverProvider) {
        this.name = Assert.requireNonNull(name, "channelFactoryName must not be null");

        this.headerFactory = Assert.requireNonNull(headerFactory, "headerFactory must not be null");

        this.eventLoopExecutor = newCachedExecutorService(name + "-eventLoop");
        this.eventLoopGroup = newEventLoopGroup(eventLoopExecutor);
        this.executorService = newExecutorService(name + "-executor");

        this.nameResolverProvider = nameResolverProvider;
    }

    private ExecutorService newExecutorService(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(name, true);
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(executorQueueSize);
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                workQueue, threadFactory);
    }

    private ExecutorService newCachedExecutorService(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(name, true);
        return Executors.newCachedThreadPool(threadFactory);
    }


    public ManagedChannel build(String channelName, String host, int port) {
        final NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port);
        channelBuilder.usePlaintext();
        channelBuilder.eventLoopGroup(eventLoopGroup);
        setupInternal(channelBuilder);

        addHeader(channelBuilder);
        channelBuilder.executor(executorService);
        if (this.nameResolverProvider != null) {
            logger.info("setNameResolverProvider:{}", this.nameResolverProvider);
            channelBuilder.nameResolverFactory(this.nameResolverProvider);
        }
        final ManagedChannel channel = channelBuilder.build();
        setChannelStateNotifier(channel, channelName);

        return channel;
    }


    private NioEventLoopGroup newEventLoopGroup(ExecutorService executorService) {
        return new NioEventLoopGroup(1, executorService);
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

    public void close() {
        final Future<?> future = eventLoopGroup.shutdownGracefully();
        try {
            logger.debug("shutdown {}-eventLoopGroup", name);
            future.await(1000*3);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ExecutorUtils.shutdownExecutorService(name + "-eventLoopExecutor", eventLoopExecutor);
        ExecutorUtils.shutdownExecutorService(name + "-executorService", executorService);
    }

}
