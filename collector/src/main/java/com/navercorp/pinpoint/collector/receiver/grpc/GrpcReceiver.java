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

import com.navercorp.pinpoint.collector.grpc.config.GrpcSslConfiguration;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import com.navercorp.pinpoint.grpc.security.SslServerConfig;
import com.navercorp.pinpoint.grpc.server.MetadataServerTransportFilter;
import com.navercorp.pinpoint.grpc.server.ServerFactory;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import com.navercorp.pinpoint.grpc.server.TransportMetadataFactory;
import com.navercorp.pinpoint.grpc.server.TransportMetadataServerInterceptor;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;

import java.io.Closeable;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

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

    private List<Object> serviceList = new ArrayList<>();

    private AddressFilter addressFilter;

    private List<ServerInterceptor> serverInterceptorList;
    private List<ServerTransportFilter> transportFilterList;

    private ServerOption serverOption;
    private GrpcSslConfiguration grpcSslConfiguration;

    private Server server;
    private ChannelzRegistry channelzRegistry;


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

        if (grpcSslConfiguration != null) {
            final SslServerConfig sslServerConfig = grpcSslConfiguration.toSslServerConfig();
            this.serverFactory = new ServerFactory(beanName, this.bindAddress.getIp(), this.bindAddress.getPort(), this.executor, serverOption, sslServerConfig);
        } else {
            this.serverFactory = new ServerFactory(beanName, this.bindAddress.getIp(), this.bindAddress.getPort(), this.executor, serverOption);
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

        // Mandatory interceptor
        ServerInterceptor transportMetadataServerInterceptor = new TransportMetadataServerInterceptor();
        this.serverFactory.addInterceptor(transportMetadataServerInterceptor);

        if (CollectionUtils.hasLength(serverInterceptorList)) {
            for (ServerInterceptor serverInterceptor : serverInterceptorList) {
                this.serverFactory.addInterceptor(serverInterceptor);
            }
        }
        if (channelzRegistry != null) {
            this.serverFactory.setChannelzRegistry(channelzRegistry);
        }

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
        for (Object service : serviceList) {
            if (service instanceof BindableService) {
                this.serverFactory.addService((BindableService) service);
            } else if (service instanceof ServerServiceDefinition) {
                this.serverFactory.addService((ServerServiceDefinition) service);
            } else {
                throw new IllegalStateException("unsupported service type " + service);
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Destroy {} server {}", this.beanName, this.server);
        }

        if (this.server != null) {
            this.server.shutdown();
        }

        for (Object bindableService : serviceList) {
            if (bindableService instanceof Closeable) {
                ((Closeable) bindableService).close();
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
    public void setBeanName(final String beanName) {
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

    public void setServerOption(ServerOption serverOption) {
        this.serverOption = serverOption;
    }

    public void setGrpcSslConfiguration(GrpcSslConfiguration grpcSslConfiguration) {
        this.grpcSslConfiguration = grpcSslConfiguration;
    }

    private static final Class<?>[] BINDABLESERVICE_TYPE = {BindableService.class, ServerServiceDefinition.class};

    private static boolean supportType(Object service) {
        for (Class<?> bindableService : BINDABLESERVICE_TYPE) {
            if (bindableService.isInstance(service)) {
                return true;
            }
        }
        return false;
    }

    public void setBindableServiceList(List<Object> serviceList) {
        for (Object service : serviceList) {
            if (!supportType(service)) {
                throw new IllegalStateException("unsupported type " + service);
            }
        }

        this.serviceList = serviceList;
    }

    public void setTransportFilterList(List<ServerTransportFilter> transportFilterList) {
        this.transportFilterList = transportFilterList;
    }

    @Autowired
    public void setServerInterceptorList(List<ServerInterceptor> serverInterceptorList) {
        this.serverInterceptorList = serverInterceptorList;
    }

    public void setChannelzRegistry(ChannelzRegistry channelzRegistry) {
        this.channelzRegistry = Objects.requireNonNull(channelzRegistry, "channelzRegistry");
    }

}