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

package com.navercorp.pinpoint.grpc.server;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CpuUtils;
import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.ExecutorUtils;
import com.navercorp.pinpoint.grpc.HeaderFactory;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import io.grpc.netty.InternalNettyServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ServerFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String name;
    private String hostname;
    private final int port;

    private final ExecutorService bossExecutor;
    private final EventLoopGroup bossEventLoopGroup;

    private final ExecutorService workerExecutor;
    private final EventLoopGroup workerEventLoopGroup;

    private final Executor executor;

    private final List<ServerServiceDefinition> bindableServices = new ArrayList<ServerServiceDefinition>();
    private final List<ServerTransportFilter> serverTransportFilters = new ArrayList<ServerTransportFilter>();
    private final List<ServerInterceptor> serverInterceptors = new ArrayList<ServerInterceptor>();

    private ServerOption serverOption;

    public ServerFactory(String name, String hostname, int port, Executor executor) {
        this(name, hostname, port, executor, null);
    }

    public ServerFactory(String name, String hostname, int port, Executor executor, ServerOption serverOption) {
        this.name = Assert.requireNonNull(name, "name must not be null");
        this.hostname = Assert.requireNonNull(hostname, "hostname must not be null");
        this.port = port;

        this.bossExecutor = newExecutor(name + "-boss");
        this.bossEventLoopGroup = newEventLoopGroup(1, this.bossExecutor);

        this.workerExecutor = newExecutor(name + "-worker");
        this.workerEventLoopGroup = newEventLoopGroup(CpuUtils.workerCount(), bossExecutor);

        this.executor = Assert.requireNonNull(executor, "executor must not be null");
        this.serverOption = serverOption;
    }

    private NioEventLoopGroup newEventLoopGroup(int i, ExecutorService executorService) {
        Assert.requireNonNull(executorService, "executorService must not be null");
        return new NioEventLoopGroup(i, executorService);
    }

    private ExecutorService newExecutor(String name) {
        ThreadFactory threadFactory = new PinpointThreadFactory(name + "-boss", true);
        return Executors.newCachedThreadPool(threadFactory);
    }


    public void addService(BindableService bindableService) {
        Assert.requireNonNull(bindableService, "bindableService must not be null");
        this.bindableServices.add(bindableService.bindService());
    }

    public void addService(ServerServiceDefinition serverServiceDefinition) {
        Assert.requireNonNull(serverServiceDefinition, "serverServiceDefinition must not be null");
        this.bindableServices.add(serverServiceDefinition);
    }

    public void addTransportFilter(ServerTransportFilter serverTransportFilter) {
        Assert.requireNonNull(serverTransportFilter, "serverTransportFilter must not be null");
        this.serverTransportFilters.add(serverTransportFilter);
    }
    public void addInterceptor(ServerInterceptor serverInterceptor) {
        Assert.requireNonNull(serverInterceptor, "serverInterceptor must not be null");
        this.serverInterceptors.add(serverInterceptor);
    }

    public Server build() {
        InetSocketAddress bindAddress = new InetSocketAddress(this.hostname, this.port);
        NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(bindAddress);
        serverBuilder.bossEventLoopGroup(bossEventLoopGroup);
        serverBuilder.workerEventLoopGroup(workerEventLoopGroup);

        setupInternal(serverBuilder);

        for (ServerServiceDefinition bindableService : this.bindableServices) {
            serverBuilder.addService(bindableService);
        }
        for (ServerTransportFilter transportFilter : this.serverTransportFilters) {
            serverBuilder.addTransportFilter(transportFilter);
        }
        for (ServerInterceptor serverInterceptor : this.serverInterceptors) {
            serverBuilder.intercept(serverInterceptor);
        }

        serverBuilder.executor(executor);
        setupServerOption(serverBuilder);

        HeaderFactory<AgentHeaderFactory.Header> headerFactory = new AgentHeaderFactory();
        ServerInterceptor headerContext = new HeaderPropagationInterceptor<AgentHeaderFactory.Header>(headerFactory, ServerContext.getAgentInfoKey());
        serverBuilder.intercept(headerContext);
        Server server = serverBuilder.build();
        return server;
    }

    private void setupInternal(NettyServerBuilder serverBuilder) {
        InternalNettyServerBuilder.setTracingEnabled(serverBuilder, false);
        InternalNettyServerBuilder.setStatsRecordStartedRpcs(serverBuilder, false);
        InternalNettyServerBuilder.setStatsEnabled(serverBuilder, false);
    }

    private void setupServerOption(final NettyServerBuilder builder) {
        // TODO @see PinpointServerAcceptor
        builder.withChildOption(ChannelOption.TCP_NODELAY, true);
        builder.withChildOption(ChannelOption.SO_KEEPALIVE, true);
        builder.withChildOption(ChannelOption.SO_SNDBUF, 1024 * 64);
        builder.withChildOption(ChannelOption.SO_RCVBUF, 1024 * 64);

        if (this.serverOption == null) {
            // Use default
            return;
        }

        builder.handshakeTimeout(this.serverOption.getHandshakeTimeout(), TimeUnit.MILLISECONDS);
        builder.flowControlWindow(this.serverOption.getFlowControlWindow());

        builder.maxInboundMessageSize(this.serverOption.getMaxInboundMessageSize());
        builder.maxHeaderListSize(this.serverOption.getMaxHeaderListSize());

        builder.keepAliveTimeout(this.serverOption.getKeepAliveTimeout(), TimeUnit.MILLISECONDS);
        builder.keepAliveTime(this.serverOption.getKeepAliveTime(), TimeUnit.MILLISECONDS);
        builder.permitKeepAliveTime(this.serverOption.getPermitKeepAliveTimeout(), TimeUnit.MILLISECONDS);
        builder.permitKeepAliveWithoutCalls(this.serverOption.isPermitKeepAliveWithoutCalls());

        builder.maxConnectionIdle(this.serverOption.getMaxConnectionIdle(), TimeUnit.MILLISECONDS);
        builder.maxConnectionAge(this.serverOption.getMaxConnectionAge(), TimeUnit.MILLISECONDS);
        builder.maxConnectionAgeGrace(this.serverOption.getMaxConnectionAgeGrace(), TimeUnit.MILLISECONDS);
        builder.maxConcurrentCallsPerConnection(this.serverOption.getMaxConcurrentCallsPerConnection());
    }

    public void close() {

        final Future<?> workerShutdown = this.workerEventLoopGroup.shutdownGracefully();
        workerShutdown.awaitUninterruptibly();
        ExecutorUtils.shutdownExecutorService(name + "-worker", workerExecutor);

        final Future<?> bossShutdown = this.bossEventLoopGroup.shutdownGracefully();
        bossShutdown.awaitUninterruptibly();
        ExecutorUtils.shutdownExecutorService(name + "-boss", bossExecutor);
    }

    @Override
    public String toString() {
        return "ServerFactory{" +
                "name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                '}';
    }
}
