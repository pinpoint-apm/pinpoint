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

import com.navercorp.pinpoint.collector.receiver.DispatchHandler;
import com.navercorp.pinpoint.collector.receiver.grpc.service.TraceService;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.common.util.Assert;
import io.grpc.Server;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import io.grpc.netty.NettyServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * @author jaehong.kim
 */
public class SpanServer implements InitializingBean, DisposableBean, BeanNameAware {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String beanName;
    private boolean enable;

    private String bindIp;
    private int bindPort;

    private DispatchHandler dispatchHandler;
    private AddressFilter addressFilter;

    private Server server;

    @Override
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

        // Add options

        // Add service
        final ServerServiceDefinition service = ServerInterceptors.intercept(new TraceService(this.dispatchHandler), new RequestHeaderServerInterceptor());
        builder.addService(service);

        this.server = builder.build();
        if (logger.isInfoEnabled()) {
            logger.info("Start span server {}", this.server);
        }
        this.server.start();
    }


    @Override
    public void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Destroy span server {}", this.server);
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

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    public void setDispatchHandler(DispatchHandler dispatchHandler) {
        this.dispatchHandler = dispatchHandler;
    }

    public DispatchHandler getDispatchHandler() {
        return dispatchHandler;
    }

    public void setAddressFilter(AddressFilter addressFilter) {
        this.addressFilter = addressFilter;
    }

    public AddressFilter getAddressFilter() {
        return addressFilter;
    }
}