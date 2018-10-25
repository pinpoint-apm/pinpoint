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
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.AgentEventHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.AgentLifeCycleEventHandler;
import com.navercorp.pinpoint.collector.receiver.thrift.tcp.TCPPacketHandler;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentServer implements InitializingBean, DisposableBean, BeanNameAware {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String beanName;
    private boolean enable = true;

    private String bindIp;
    private int bindPort;

    private AddressFilter addressFilter;

    private Server server;
    private String certChainFilePath;
    private String privateKeyFilePath;
    private String trustCertCollectionFilePath;

    // TODO Add constructor arguments ?
    private TCPPacketHandler tcpPacketHandler;
    private ZookeeperClusterService clusterService;

    @Autowired
    private AgentEventHandler agentEventHandler;

    private DispatchHandler dispatchHandler;

    @Autowired
    private AgentLifeCycleEventHandler agentLifeCycleEventHandler;
    private SslContext sslContext;

    @Resource(name = "channelStateChangeEventHandlers")
    private List<ServerStateChangeEventHandler> channelStateChangeEventHandlers = Collections.emptyList();

    private AtomicInteger idGenerator = new AtomicInteger(0);

    public void afterPropertiesSet() throws Exception {
        if (Boolean.FALSE == this.enable) {
            return;
        }

        Assert.requireNonNull(this.beanName, "beanName must not be null");
        Assert.requireNonNull(this.bindIp, "bindIp must not be null");
        Assert.requireNonNull(this.dispatchHandler, "dispatchHandler must not be null");
        Assert.requireNonNull(this.addressFilter, "addressFilter must not be null");

        final SocketAddress socketAddress = new InetSocketAddress(this.bindIp, this.bindPort);
        final NettyServerBuilder builder = NettyServerBuilder.forAddress(socketAddress);
        final ServerTransportFilter serverTransportFilter = new DefaultServerTransportFilter();
        builder.addTransportFilter(serverTransportFilter);

        final ServerServiceDefinition service = ServerInterceptors.intercept(new AgentService(dispatchHandler), new RequestHeaderServerInterceptor());
        builder.addService(service);
//        builder.sslContext(this.sslContext);

        this.server = builder.build();
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

    public void setSslContext(final SslContext sslContext) {
        this.sslContext = sslContext;
    }

    private SslContextBuilder getSslContextBuilder() {
        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(new File(certChainFilePath), new File(privateKeyFilePath));
        if (trustCertCollectionFilePath != null) {
            sslClientContextBuilder.trustManager(new File(trustCertCollectionFilePath));
            sslClientContextBuilder.clientAuth(ClientAuth.REQUIRE);
        }
        return GrpcSslContexts.configure(sslClientContextBuilder, SslProvider.OPENSSL);
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public void setAddressFilter(AddressFilter addressFilter) {
        this.addressFilter = addressFilter;
    }

    public AddressFilter getAddressFilter() {
        return addressFilter;
    }

    public DispatchHandler getDispatchHandler() {
        return dispatchHandler;
    }

    public void setDispatchHandler(DispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
    }
}