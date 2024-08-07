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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.Monitor;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import com.navercorp.pinpoint.grpc.server.ConnectionCountServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.MetadataServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.ServerFactory;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import com.navercorp.pinpoint.grpc.server.StreamCountInterceptor;
import com.navercorp.pinpoint.grpc.server.TransportMetadataFactory;
import com.navercorp.pinpoint.grpc.server.TransportMetadataServerInterceptor;
import com.navercorp.pinpoint.grpc.util.ServerUtils;
import io.grpc.Server;
import io.grpc.ServerCallExecutorSupplier;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import jakarta.annotation.Nonnull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.NestedExceptionUtils;

import java.io.Closeable;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class GrpcReceiver implements InitializingBean, DisposableBean, BeanNameAware {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private String beanName;
    private boolean enable;

    private BindAddress bindAddress;

    private ServerFactory serverFactory;
    private Executor executor;
    private ServerCallExecutorSupplier serverCallExecutorSupplier;

    private List<ServerServiceDefinition> serviceList = new ArrayList<>();

    private AddressFilter addressFilter;

    private List<ServerInterceptor> serverInterceptorList;
    private List<ServerTransportFilter> transportFilterList;

    private ServerOption serverOption;
    private ByteBufAllocator byteBufAllocator;

    private SslContext sslContext;

    private Server server;
    private ChannelzRegistry channelzRegistry;

    private Monitor monitor = Monitor.NONE;


    @Override
    public void afterPropertiesSet() throws Exception {
        if (Boolean.FALSE == this.enable) {
            logger.warn("{} is {}", this.beanName, enable);
            return;
        }

        Objects.requireNonNull(this.beanName, "beanName");
        Objects.requireNonNull(this.bindAddress, "bindAddress");
        Objects.requireNonNull(this.addressFilter, "addressFilter");
        Assert.isTrue(CollectionUtils.hasLength(this.serviceList), "serviceList must not be empty");
        Objects.requireNonNull(this.serverOption, "serverOption");

        if (sslContext != null) {
            this.serverFactory = new ServerFactory(beanName, this.bindAddress.getIp(), this.bindAddress.getPort(), this.executor, this.serverCallExecutorSupplier, serverOption, byteBufAllocator, sslContext);
        } else {
            this.serverFactory = new ServerFactory(beanName, this.bindAddress.getIp(), this.bindAddress.getPort(), this.executor, this.serverCallExecutorSupplier, serverOption, byteBufAllocator);
        }

        ServerTransportFilter permissionServerTransportFilter = new PermissionServerTransportFilter(this.beanName, addressFilter);
        this.serverFactory.addTransportFilter(permissionServerTransportFilter);

        ConnectionCountServerTransportFilter countFilter = new ConnectionCountServerTransportFilter();
        this.serverFactory.addTransportFilter(countFilter);

        TransportMetadataFactory transportMetadataFactory = new TransportMetadataFactory(beanName);
        // Mandatory interceptor
        final ServerTransportFilter metadataTransportFilter = new MetadataServerTransportFilter(transportMetadataFactory);
        this.serverFactory.addTransportFilter(metadataTransportFilter);

        if (CollectionUtils.hasLength(transportFilterList)) {
            for (ServerTransportFilter transportFilter : transportFilterList) {
                this.serverFactory.addTransportFilter(transportFilter);
            }
        }

        // Mandatory interceptor
        ServerInterceptor transportMetadataServerInterceptor = new TransportMetadataServerInterceptor();
        this.serverFactory.addInterceptor(transportMetadataServerInterceptor);

        StreamCountInterceptor streamCountInterceptor = new StreamCountInterceptor();
        this.serverFactory.addInterceptor(streamCountInterceptor);

        if (CollectionUtils.hasLength(serverInterceptorList)) {
            for (ServerInterceptor serverInterceptor : serverInterceptorList) {
                this.serverFactory.addInterceptor(serverInterceptor);
            }
        }
        if (channelzRegistry != null) {
            this.serverFactory.setChannelzRegistry(channelzRegistry);
        }

        this.monitor.register(() -> {
            logger.info("{} CurrentTransport:{}, CurrentGrpcStream:{}", beanName, countFilter.getCurrentConnection(), streamCountInterceptor.getCurrentStream());
        });

        // Add service
        addService();

        this.server = serverFactory.build();
        if (logger.isInfoEnabled()) {
            logger.info("Start {} server {}", this.beanName, this.server);
        }
        try {
            this.server.start();
        } catch (Throwable th) {
            final Throwable rootCause = NestedExceptionUtils.getRootCause(th);
            if (rootCause instanceof BindException) {
                logger.error("Server bind failed. {} address:{}", this.beanName, this.bindAddress, rootCause);
            } else {
                logger.error("Server start failed. {} address:{}", this.beanName, this.bindAddress);
            }
            throw th;
        }
    }

    private void addService() {
        // Add service
        for (ServerServiceDefinition service : serviceList) {
            this.serverFactory.addService(service);
        }
    }

    private void shutdownServer() {
        if (server == null || server.isTerminated()) {
            return;
        }

        final long maxWaitTime = serverOption.getGrpcMaxTermWaitTimeMillis();

        if (!ServerUtils.shutdownAndAwaitTermination(server, maxWaitTime, TimeUnit.MILLISECONDS)) {
            logger.warn("{} server shutdown error", beanName);
        }
    }


    @Override
    public void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Destroy {} server {}", this.beanName, this.server);
        }

        monitor.close();

        shutdownServer();

        for (Object bindableService : serviceList) {
            if (bindableService instanceof Closeable closeable) {
                closeable.close();
            }
        }

        if (this.serverFactory != null) {
            this.serverFactory.close();
        }
    }

    // Test only
    void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    @Override
    public void setBeanName(@Nonnull final String beanName) {
        this.beanName = beanName;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public void setBindAddress(BindAddress bindAddress) {
        this.bindAddress = Objects.requireNonNull(bindAddress, "bindAddress");
    }

    public void setAddressFilter(AddressFilter addressFilter) {
        this.addressFilter = addressFilter;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setServerCallExecutorSupplier(ServerCallExecutorSupplier serverCallExecutorSupplier) {
        this.serverCallExecutorSupplier = serverCallExecutorSupplier;
    }

    public void setServerOption(ServerOption serverOption) {
        this.serverOption = serverOption;
    }

    public void setByteBufAllocator(ByteBufAllocator byteBufAllocator) {
        this.byteBufAllocator = byteBufAllocator;
    }

    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    public void setBindableServiceList(List<ServerServiceDefinition> serviceList) {
        Objects.requireNonNull(serviceList, "serviceList");
        this.serviceList = List.copyOf(serviceList);
    }

    public void setTransportFilterList(List<ServerTransportFilter> transportFilterList) {
        this.transportFilterList = transportFilterList;
    }

    public void setServerInterceptorList(List<ServerInterceptor> serverInterceptorList) {
        this.serverInterceptorList = serverInterceptorList;
    }

    public void setChannelzRegistry(ChannelzRegistry channelzRegistry) {
        this.channelzRegistry = Objects.requireNonNull(channelzRegistry, "channelzRegistry");
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = Objects.requireNonNull(monitor, "monitor");
    }
}