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
import io.grpc.netty.NettyServerBuilder;
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

    public ServerFactory(String name, int port, ExecutorService executor) {
        this.name = Assert.requireNonNull(name, "name must not be null");
        this.port = port;

        this.bossExecutor = newExecutor(name + "-boss");
        this.bossEventLoopGroup = newEventLoopGroup(1, this.bossExecutor);

        this.workerExecutor = newExecutor(name + "-worker");
        this.workerEventLoopGroup = newEventLoopGroup(CpuUtils.workerCount(), bossExecutor);

        this.executor = Assert.requireNonNull(executor, "executor must not be null");
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

    public Server build() {
        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port);
        serverBuilder.bossEventLoopGroup(bossEventLoopGroup);
        serverBuilder.workerEventLoopGroup(workerEventLoopGroup);

        for (BindableService bindableService : this.bindableServices) {
            serverBuilder.addService(bindableService);
        }
        serverBuilder.executor(executor);

        HeaderFactory<AgentHeaderFactory.Header> headerFactory = new AgentHeaderFactory();
        HeaderPropagationInterceptor<AgentHeaderFactory.Header> headerContext = new HeaderPropagationInterceptor<AgentHeaderFactory.Header>(headerFactory, AgentInfoContext.agentInfoKey);
        serverBuilder.intercept(headerContext);
        Server server = serverBuilder.build();
        return server;
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
