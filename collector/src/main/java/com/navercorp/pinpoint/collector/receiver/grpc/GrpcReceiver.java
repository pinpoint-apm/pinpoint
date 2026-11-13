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
import org.jspecify.annotations.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * The server is built and the port is opened in {@link #start()}, after all singletons are ready.
 * The port is closed and the resources are released in {@link #stop()}, before the other beans are destroyed.
 * The default phase is kept, so the receiver starts last and stops first.
 * <p>
 * Spring calls {@link #stop()} only for a running lifecycle, so a failed {@link #start()} releases
 * what it has built by itself.
 *
 * @author Taejin Koo
 */
public class GrpcReceiver implements SmartLifecycle, BeanNameAware {
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
    private ByteBufAllocator byteBufAllocator = ByteBufAllocator.DEFAULT;

    private SslContext sslContext;

    private Server server;
    private volatile boolean running;
    private ChannelzRegistry channelzRegistry;

    private Monitor monitor = Monitor.NONE;


    @Override
    public void start() {
        if (this.running || this.server != null) {
            return;
        }
        if (Boolean.FALSE == this.enable) {
            logger.warn("{} is {}", this.beanName, enable);
            return;
        }

        try {
            this.server = buildServer();
        } catch (Exception e) {
            release();
            throw new IllegalStateException(this.beanName + " server build failed. address:" + this.bindAddress, e);
        }
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
            release();
            throw new IllegalStateException(this.beanName + " server start failed. address:" + this.bindAddress, th);
        }
        this.running = true;
    }

    private Server buildServer() throws Exception {
        Objects.requireNonNull(this.beanName, "beanName");
        Objects.requireNonNull(this.bindAddress, "bindAddress");
        Objects.requireNonNull(this.addressFilter, "addressFilter");
        Assert.notEmpty(this.serviceList, "serviceList");
        Objects.requireNonNull(this.serverOption, "serverOption");

        if (sslContext != null) {
            this.serverFactory = new ServerFactory(beanName, this.bindAddress.getIp(), this.bindAddress.getPort(), this.executor, this.serverCallExecutorSupplier, serverOption, byteBufAllocator, sslContext);
        } else {
            this.serverFactory = new ServerFactory(beanName, this.bindAddress.getIp(), this.bindAddress.getPort(), this.executor, this.serverCallExecutorSupplier, serverOption, byteBufAllocator);
        }

        ServerTransportFilter permissionServerTransportFilter = new PermissionServerTransportFilter(this.beanName, addressFilter);
        this.serverFactory.addTransportFilter(permissionServerTransportFilter);

        TransportMetadataFactory transportMetadataFactory = new TransportMetadataFactory(beanName);
        // Mandatory interceptor
        final ServerTransportFilter metadataTransportFilter = new MetadataServerTransportFilter(transportMetadataFactory);
        this.serverFactory.addTransportFilter(metadataTransportFilter);

        if (CollectionUtils.hasLength(transportFilterList)) {
            for (ServerTransportFilter transportFilter : transportFilterList) {
                this.serverFactory.addTransportFilter(transportFilter);
            }
        }

        // Must be the last filter: if a later filter rejected the transport in transportReady(),
        // transportTerminated() would receive null attributes and the count would never be decremented
        ConnectionCountServerTransportFilter countFilter = new ConnectionCountServerTransportFilter();
        this.serverFactory.addTransportFilter(countFilter);

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

        return serverFactory.build();
    }

    @Override
    public void stop() {
        if (logger.isInfoEnabled()) {
            logger.info("Stop {} server {}", this.beanName, this.server);
        }
        release();
        this.running = false;
    }

    private void release() {
        monitor.close();

        shutdownServer();

        for (Object bindableService : serviceList) {
            if (bindableService instanceof Closeable closeable) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    logger.warn("{} service close failed. {}", this.beanName, bindableService, e);
                }
            }
        }

        if (this.serverFactory != null) {
            this.serverFactory.close();
            this.serverFactory = null;
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
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

    // Test only
    void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    @Override
    public void setBeanName(@NonNull final String beanName) {
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