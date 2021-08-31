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
import com.navercorp.pinpoint.grpc.ChannelTypeEnum;
import com.navercorp.pinpoint.grpc.ExecutorUtils;
import com.navercorp.pinpoint.grpc.client.config.ClientOption;
import com.navercorp.pinpoint.grpc.security.SslClientConfig;
import com.navercorp.pinpoint.grpc.security.SslContextFactory;

import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.NameResolverProvider;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.InternalNettyChannelBuilder;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final SslClientConfig sslClientConfig;

    private final List<ClientInterceptor> clientInterceptorList;
    private final NameResolverProvider nameResolverProvider;

    // state object
    private final EventLoopGroup eventLoopGroup;
    private final ExecutorService eventLoopExecutor;
    private final ExecutorService executorService;
    private final Class<? extends Channel> channelType;

    DefaultChannelFactory(String factoryName,
                          int executorQueueSize,
                          HeaderFactory headerFactory,
                          NameResolverProvider nameResolverProvider,
                          ClientOption clientOption,
                          SslClientConfig sslClientConfig,
                          List<ClientInterceptor> clientInterceptorList) {
        this.factoryName = Objects.requireNonNull(factoryName, "factoryName");
        this.executorQueueSize = executorQueueSize;
        this.headerFactory = Objects.requireNonNull(headerFactory, "headerFactory");
        // @Nullable
        this.nameResolverProvider = nameResolverProvider;
        this.clientOption = Objects.requireNonNull(clientOption, "clientOption");
        this.sslClientConfig = Objects.requireNonNull(sslClientConfig, "sslClientConfig");

        Objects.requireNonNull(clientInterceptorList, "clientInterceptorList");
        this.clientInterceptorList = new ArrayList<>(clientInterceptorList);

        ChannelType channelType = getChannelType();
        this.channelType = channelType.getChannelType();

        this.eventLoopExecutor = newCachedExecutorService(factoryName + "-Channel-Worker");
        this.eventLoopGroup = channelType.newEventLoopGroup(1, eventLoopExecutor);
        this.executorService = newExecutorService(factoryName + "-Channel-Executor", this.executorQueueSize);
    }

    @Override
    public String getFactoryName() {
        return factoryName;
    }

    private ChannelType getChannelType() {
        ChannelTypeFactory factory = new ChannelTypeFactory();
        ChannelTypeEnum channelTypeEnum = clientOption.getChannelTypeEnum();
        return factory.newChannelType(channelTypeEnum);
    }


    private ExecutorService newCachedExecutorService(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        return Executors.newCachedThreadPool(threadFactory);
    }

    private ExecutorService newExecutorService(String name, int executorQueueSize) {
        ThreadFactory threadFactory = new PinpointThreadFactory(PinpointThreadFactory.DEFAULT_THREAD_NAME_PREFIX + name, true);
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(executorQueueSize);
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

        logger.info("ChannelType:{}", channelType.getSimpleName());
        channelBuilder.channelType(channelType);
        channelBuilder.eventLoopGroup(eventLoopGroup);

        setupInternal(channelBuilder);
        channelBuilder.defaultLoadBalancingPolicy(GrpcUtil.DEFAULT_LB_POLICY);

        addHeader(channelBuilder);
        addClientInterceptor(channelBuilder);

        channelBuilder.executor(executorService);
        if (nameResolverProvider != null) {
            logger.info("Set nameResolverProvider {}. channelName={}, host={}, port={}", this.nameResolverProvider, channelName, host, port);
            setNameResolverFactory(channelBuilder, this.nameResolverProvider);
        }
        setupClientOption(channelBuilder);

        if (sslClientConfig.isEnable()) {
            SslContext sslContext = null;
            try {
                sslContext = SslContextFactory.create(sslClientConfig);
            } catch (SSLException e) {
                throw new SecurityException(e);
            }
            channelBuilder.sslContext(sslContext);
            channelBuilder.negotiationType(NegotiationType.TLS);
        }

        channelBuilder.maxTraceEvents(clientOption.getMaxTraceEvent());

        final ManagedChannel channel = channelBuilder.build();

        return channel;
    }

    @SuppressWarnings("deprecation")
    private void setNameResolverFactory(NettyChannelBuilder channelBuilder, NameResolverProvider nameResolverProvider) {
        channelBuilder.nameResolverFactory(nameResolverProvider);
    }

    private void setupInternal(NettyChannelBuilder channelBuilder) {
        InternalNettyChannelBuilder.setTracingEnabled(channelBuilder, false);

        InternalNettyChannelBuilder.setStatsEnabled(channelBuilder, false);
        InternalNettyChannelBuilder.setStatsRecordStartedRpcs(channelBuilder, false);
        InternalNettyChannelBuilder.setStatsRecordFinishedRpcs(channelBuilder, false);
        InternalNettyChannelBuilder.setStatsRecordRealTimeMetrics(channelBuilder, false);
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
        channelBuilder.maxInboundMetadataSize(clientOption.getMaxHeaderListSize());
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