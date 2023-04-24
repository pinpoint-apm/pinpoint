package com.navercorp.pinpoint.collector.grpc.ssl;

import com.navercorp.pinpoint.collector.grpc.config.GrpcReceiverProperties;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.common.server.util.AddressFilter;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import com.navercorp.pinpoint.grpc.security.SslContextFactory;
import com.navercorp.pinpoint.grpc.security.SslServerProperties;
import io.grpc.ServerCallExecutorSupplier;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerTransportFilter;
import io.netty.handler.ssl.SslContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.util.List;
import java.util.concurrent.Executor;

@Configuration
@ConditionalOnProperty(value = "pinpoint.modules.collector.grpc.ssl.enabled", havingValue = "true")
@ComponentScan(basePackages = "com.navercorp.pinpoint.collector.grpc.ssl")
public class GrpcSslModule {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Bean
    public GrpcReceiver grpcAgentSslReceiver(@Qualifier("grpcAgentSslReceiverProperties") GrpcSslReceiverProperties properties,
                                            @Qualifier("grpcAgentReceiverProperties") GrpcReceiverProperties grpcReceiverProperties,
                                            AddressFilter addressFilter,
                                            @Qualifier("agentServiceList") List<?> serviceList,
                                            @Qualifier("agentInterceptorList")List<ServerInterceptor> serverInterceptorList,
                                            ChannelzRegistry channelzRegistry,
                                            @Qualifier("grpcAgentServerExecutor") Executor executor,
                                            @Qualifier("grpcAgentServerCallExecutorSupplier") ServerCallExecutorSupplier serverCallExecutorSupplier) throws SSLException {
        GrpcReceiver receiver = createReceiver(properties, grpcReceiverProperties, addressFilter, serviceList, serverInterceptorList, channelzRegistry, executor);
        receiver.setServerCallExecutorSupplier(serverCallExecutorSupplier);

        return receiver;
    }

    @Bean
    public GrpcReceiver grpcSpanSslReceiver(@Qualifier("grpcSpanSslReceiverProperties") GrpcSslReceiverProperties properties,
                                            @Qualifier("grpcSpanReceiverProperties") GrpcReceiverProperties grpcReceiverProperties,
                                            AddressFilter addressFilter,
                                            @Qualifier("spanServiceList") List<ServerServiceDefinition> serviceList,
                                            @Qualifier("spanInterceptorList") List<ServerInterceptor> serverInterceptorList,
                                            ChannelzRegistry channelzRegistry,
                                            @Qualifier("grpcSpanServerExecutor") Executor executor,
                                            @Qualifier("serverTransportFilterList") List<ServerTransportFilter> transportFilterList) throws SSLException {
        GrpcReceiver receiver = createReceiver(properties, grpcReceiverProperties, addressFilter, serviceList, serverInterceptorList, channelzRegistry, executor);
        receiver.setTransportFilterList(transportFilterList);
        return receiver;
    }

    @Bean
    public GrpcReceiver grpcStatSslReceiver(@Qualifier("grpcStatSslReceiverProperties") GrpcSslReceiverProperties properties,
                                            @Qualifier("grpcStatReceiverProperties") GrpcReceiverProperties grpcReceiverProperties,
                                            AddressFilter addressFilter,
                                            @Qualifier("statServiceList") List<ServerServiceDefinition> serviceList,
                                            @Qualifier("statInterceptorList") List<ServerInterceptor> serverInterceptorList,
                                            ChannelzRegistry channelzRegistry,
                                            @Qualifier("grpcStatServerExecutor") Executor executor,
                                            @Qualifier("serverTransportFilterList") List<ServerTransportFilter> transportFilterList) throws SSLException {
        GrpcReceiver receiver = createReceiver(properties, grpcReceiverProperties, addressFilter, serviceList, serverInterceptorList, channelzRegistry, executor);
        receiver.setTransportFilterList(transportFilterList);
        return receiver;
    }

    private GrpcReceiver createReceiver(GrpcSslReceiverProperties properties,
                                        GrpcReceiverProperties grpcReceiverProperties,
                                        AddressFilter addressFilter,
                                        List<?> serviceList,
                                        List<ServerInterceptor> serverInterceptorList,
                                        ChannelzRegistry channelzRegistry,
                                        Executor executor) throws SSLException {
        GrpcReceiver receiver = new GrpcReceiver();
        receiver.setBindAddress(properties.getBindAddress());
        receiver.setServerOption(grpcReceiverProperties.getServerOption());

        receiver.setEnable(true);

        receiver.setExecutor(executor);
        receiver.setAddressFilter(addressFilter);
        receiver.setBindableServiceList(serviceList);
        receiver.setServerInterceptorList(serverInterceptorList);
        receiver.setChannelzRegistry(channelzRegistry);

        SslContext sslContext = newSslContext(properties);
        receiver.setSslContext(sslContext);
        return receiver;
    }

    private SslContext newSslContext(GrpcSslReceiverProperties properties) throws SSLException {
        final SslServerProperties sslServerConfig = properties.getGrpcSslProperties().toSslServerProperties();
        logger.debug("Enable sslConfig.({})", sslServerConfig);
        return SslContextFactory.create(sslServerConfig);
    }

}
