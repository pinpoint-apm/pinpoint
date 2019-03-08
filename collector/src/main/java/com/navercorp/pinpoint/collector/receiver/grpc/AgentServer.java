/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.cluster.zookeeper.ZookeeperClusterService;
import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.grpc.service.AgentService;
import com.navercorp.pinpoint.collector.receiver.grpc.service.KeepAliveService;
import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.server.DefaultServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.IdGeneratorServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.InetAddressFilter;
import com.navercorp.pinpoint.grpc.server.PermissionServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.ServerFactory;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author jaehong.kim
 */
public class AgentServer implements InitializingBean, DisposableBean, BeanNameAware {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    // Bean property
    private String beanName;
    private boolean enable = true;

    private int bindPort;
    private ExecutorService executor;
    private AddressFilter addressFilter;
    private DispatchHandler dispatchHandler;
    private ServerOption serverOption;

    private Server server;

    @Autowired
    private AgentEventAsyncTaskService agentEventAsyncTask;

    @Autowired
    private AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTask;

    // TODO
    @Resource(name = "channelStateChangeEventHandlers")
    private List<ServerStateChangeEventHandler> channelStateChangeEventHandlers = Collections.emptyList();
    private ZookeeperClusterService clusterService;

    public void afterPropertiesSet() throws Exception {
        if (Boolean.FALSE == this.enable) {
            return;
        }

        Assert.requireNonNull(this.beanName, "beanName must not be null");
        Assert.requireNonNull(this.dispatchHandler, "dispatchHandler must not be null");
        Assert.requireNonNull(this.addressFilter, "addressFilter must not be null");

        final ServerFactory serverFactory = new ServerFactory(this.beanName, this.bindPort, this.executor, this.serverOption);
        serverFactory.addService(new AgentService(this.dispatchHandler));
        serverFactory.addService(new KeepAliveService(this.agentEventAsyncTask, this.agentLifeCycleAsyncTask));
        serverFactory.addTransportFilter(new DefaultServerTransportFilter());
        serverFactory.addTransportFilter(new IdGeneratorServerTransportFilter());
        serverFactory.addTransportFilter(new PermissionServerTransportFilter(new InetAddressFilter() {
            @Override
            public boolean accept(InetAddress inetAddress) {
                return addressFilter.accept(inetAddress);
            }
        }));

        this.server = serverFactory.build();
        if (logger.isInfoEnabled()) {
            logger.info("Start AgentServer {}", this.server);
        }
        this.server.start();
    }

    @Override
    public void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Destroy AgentServer {}", this.server);
        }

        if (this.server != null) {
            this.server.shutdown();
        }
    }

    // Test only
    void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    @Override
    public void setBeanName(final String beanName) {
        this.beanName = beanName;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public void setAddressFilter(AddressFilter addressFilter) {
        this.addressFilter = addressFilter;
    }

    public void setDispatchHandler(DispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    public void setServerOption(ServerOption serverOption) {
        this.serverOption = serverOption;
    }

    public void setClusterService(ZookeeperClusterService clusterService) {
        this.clusterService = clusterService;
    }
}