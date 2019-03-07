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
import io.grpc.ServerTransportFilter;
import io.grpc.netty.NettyServerBuilder;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
    private final int port;

    private final ExecutorService bossExecutor;
    private final EventLoopGroup bossEventLoopGroup;

    private final ExecutorService workerExecutor;
    private final EventLoopGroup workerEventLoopGroup;

    private final ExecutorService executor;

    private final List<BindableService> bindableServices = new ArrayList<BindableService>();
    private final List<ServerTransportFilter> serverTransportFilters = new ArrayList<ServerTransportFilter>();

    private ServerOption serverOption;

    public ServerFactory(String name, int port, ExecutorService executor) {
        this(name, port, executor, null);
    }

    public ServerFactory(String name, int port, ExecutorService executor, ServerOption serverOption) {
        this.name = Assert.requireNonNull(name, "name must not be null");
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
        this.bindableServices.add(bindableService);
    }

    public void addTransportFilter(ServerTransportFilter serverTransportFilter) {
        this.serverTransportFilters.add(serverTransportFilter);
    }

    public Server build() {
        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port);
        serverBuilder.bossEventLoopGroup(bossEventLoopGroup);
        serverBuilder.workerEventLoopGroup(workerEventLoopGroup);

        for (BindableService bindableService : this.bindableServices) {
            serverBuilder.addService(bindableService);
        }

        for(ServerTransportFilter serverTransportFilter : this.serverTransportFilters) {
            serverBuilder.addTransportFilter(serverTransportFilter);
        }

        serverBuilder.executor(executor);
        setupServerOption(serverBuilder);

        HeaderFactory<AgentHeaderFactory.Header> headerFactory = new AgentHeaderFactory();
        HeaderPropagationInterceptor<AgentHeaderFactory.Header> headerContext = new HeaderPropagationInterceptor<AgentHeaderFactory.Header>(headerFactory, AgentInfoContext.agentInfoKey);
        serverBuilder.intercept(headerContext);
        Server server = serverBuilder.build();
        return server;
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
        ExecutorUtils.shutdownExecutorService(name + "-executor", this.executor);

        final Future<?> workerShutdown = this.workerEventLoopGroup.shutdownGracefully();
        workerShutdown.awaitUninterruptibly();
        ExecutorUtils.shutdownExecutorService(name + "-worker", workerExecutor);

        final Future<?> bossShutdown = this.bossEventLoopGroup.shutdownGracefully();
        bossShutdown.awaitUninterruptibly();
        ExecutorUtils.shutdownExecutorService(name + "-boss", bossExecutor);
    }
}
