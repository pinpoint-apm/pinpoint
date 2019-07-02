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

import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.NameResolverProvider;
import io.grpc.netty.InternalNettyChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
    private final NameResolverProvider nameResolverProvider;
    private final ClientOption clientOption;

    private final List<ClientInterceptor> clientInterceptorList;

    public ChannelFactory(ChannelFactoryOption option) {
        this.name = option.getName();

        this.headerFactory = option.getHeaderFactory();

        this.eventLoopExecutor = newCachedExecutorService(name + "-Channel-Worker");
        this.eventLoopGroup = newEventLoopGroup(eventLoopExecutor);
        this.executorService = newExecutorService(name + "-Channel-Executor", option.getExecutorQueueSize());

        this.nameResolverProvider = option.getNameResolverProvider();

        this.clientInterceptorList = Assert.requireNonNull(option.getClientInterceptorList(), "clientInterceptorList must not be null");
        this.clientOption = option.getClientOption();
    }

    private ExecutorService newCachedExecutorService(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        return Executors.newCachedThreadPool(threadFactory);
    }

    private NioEventLoopGroup newEventLoopGroup(ExecutorService executorService) {
        return new NioEventLoopGroup(1, executorService);
    }

    private ExecutorService newExecutorService(String name, int executorQueueSize) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(executorQueueSize);
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                workQueue, threadFactory);
    }

    public ManagedChannel build(String channelName, String host, int port) {
        final NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port);
        channelBuilder.usePlaintext();
        channelBuilder.eventLoopGroup(eventLoopGroup);
        setupInternal(channelBuilder);

        addHeader(channelBuilder);
        addClientInterceptor(channelBuilder);

        channelBuilder.executor(executorService);
        if (this.nameResolverProvider != null) {
            logger.info("Set nameResolverProvider {}. channelName={}, host={}, port={}", this.nameResolverProvider, channelName, host, port);
            channelBuilder.nameResolverFactory(this.nameResolverProvider);
        }
        setupClientOption(channelBuilder);

        final ManagedChannel channel = channelBuilder.build();

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

    private void addClientInterceptor(NettyChannelBuilder channelBuilder) {
        channelBuilder.intercept(clientInterceptorList);
    }

    private void setupClientOption(final NettyChannelBuilder channelBuilder) {
        channelBuilder.keepAliveTime(clientOption.getKeepAliveTime(), TimeUnit.MILLISECONDS);
        channelBuilder.keepAliveTimeout(clientOption.getKeepAliveTimeout(), TimeUnit.MILLISECONDS);
        channelBuilder.keepAliveWithoutCalls(clientOption.isKeepAliveWithoutCalls());
        channelBuilder.maxHeaderListSize(clientOption.getMaxHeaderListSize());
        channelBuilder.maxInboundMessageSize(clientOption.getMaxInboundMessageSize());

        // ChannelOption
        channelBuilder.withOption(ChannelOption.TCP_NODELAY, true);
        channelBuilder.withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientOption.getConnectTimeout());
        final WriteBufferWaterMark writeBufferWaterMark = new WriteBufferWaterMark(clientOption.getWriteBufferLowWaterMark(), clientOption.getWriteBufferHighWaterMark());
        channelBuilder.withOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark);
        if (logger.isInfoEnabled()) {
            logger.info("Set clientOption {}. name={}", clientOption, name);
        }
    }


    public void close() {
        final Future<?> future = eventLoopGroup.shutdownGracefully();
        try {
            logger.debug("shutdown {}-eventLoopGroup", name);
            future.await(1000 * 3);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ExecutorUtils.shutdownExecutorService(name + "-eventLoopExecutor", eventLoopExecutor);
        ExecutorUtils.shutdownExecutorService(name + "-executorService", executorService);
    }
}