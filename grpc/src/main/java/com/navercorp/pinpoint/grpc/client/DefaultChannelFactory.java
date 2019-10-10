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

import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.ExecutorUtils;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.NameResolverProvider;
import io.grpc.netty.InternalNettyChannelBuilder;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
public class DefaultChannelFactory implements ChannelFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String factoryName;

    private final int executorQueueSize;
    private final HeaderFactory headerFactory;

    private final ClientOption clientOption;

    private final List<ClientInterceptor> clientInterceptorList;
    private final NameResolverProvider nameResolverProvider;

    // state object
    private final EventLoopGroup eventLoopGroup;
    private final ExecutorService eventLoopExecutor;
    private final ExecutorService executorService;


    DefaultChannelFactory(String factoryName,
                                 int executorQueueSize,
                                 HeaderFactory headerFactory,
                                 NameResolverProvider nameResolverProvider,
                                 ClientOption clientOption,
                                 List<ClientInterceptor> clientInterceptorList) {
        this.factoryName = Assert.requireNonNull(factoryName, "factoryName");
        this.executorQueueSize = executorQueueSize;
        this.headerFactory = Assert.requireNonNull(headerFactory, "headerFactory");
        // @Nullable
        this.nameResolverProvider = nameResolverProvider;
        this.clientOption = Assert.requireNonNull(clientOption, "clientOption");

        Assert.requireNonNull(clientInterceptorList, "clientInterceptorList");
        this.clientInterceptorList = new ArrayList<ClientInterceptor>(clientInterceptorList);


        this.eventLoopExecutor = newCachedExecutorService(factoryName + "-Channel-Worker");
        this.eventLoopGroup = newEventLoopGroup(eventLoopExecutor);
        this.executorService = newExecutorService(factoryName + "-Channel-Executor", this.executorQueueSize);
    }

    @Override
    public String getFactoryName() {
        return factoryName;
    }

    private ExecutorService newCachedExecutorService(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        return Executors.newCachedThreadPool(threadFactory);
    }

    private EventLoopGroup newEventLoopGroup(ExecutorService executorService) {
        return new NioEventLoopGroup(1, executorService);
    }

    private ExecutorService newExecutorService(String name, int executorQueueSize) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(executorQueueSize);
        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                workQueue, threadFactory);
    }

    @Override
    public ManagedChannel build(String host, int port) {
        return build(this.factoryName, host, port);
    }

    @Override
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
        channelBuilder.flowControlWindow(clientOption.getFlowControlWindow());
        channelBuilder.idleTimeout(clientOption.getIdleTimeoutMillis(), TimeUnit.MILLISECONDS);

        // ChannelOption
        channelBuilder.withOption(ChannelOption.TCP_NODELAY, true);
        channelBuilder.withOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, clientOption.getConnectTimeout());

        final WriteBufferWaterMark writeBufferWaterMark = new WriteBufferWaterMark(clientOption.getWriteBufferLowWaterMark(), clientOption.getWriteBufferHighWaterMark());
        channelBuilder.withOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeBufferWaterMark);
        if (logger.isInfoEnabled()) {
            logger.info("Set clientOption {}. name={}", clientOption, factoryName);
        }
    }

    @Override
    public void close() {
        final Future<?> future = eventLoopGroup.shutdownGracefully();
        try {
            logger.debug("shutdown {}-eventLoopGroup", factoryName);
            future.await(1000 * 3);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ExecutorUtils.shutdownExecutorService(factoryName + "-eventLoopExecutor", eventLoopExecutor);
        ExecutorUtils.shutdownExecutorService(factoryName + "-executorService", executorService);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultChannelFactory{");
        sb.append("factoryName='").append(factoryName).append('\'');
        sb.append(", executorQueueSize=").append(executorQueueSize);
        sb.append(", headerFactory=").append(headerFactory);
        sb.append(", clientOption=").append(clientOption);
        sb.append(", clientInterceptorList=").append(clientInterceptorList);
        sb.append(", nameResolverProvider=").append(nameResolverProvider);
        sb.append(", eventLoopGroup=").append(eventLoopGroup);
        sb.append(", eventLoopExecutor=").append(eventLoopExecutor);
        sb.append(", executorService=").append(executorService);
        sb.append('}');
        return sb.toString();
    }
}