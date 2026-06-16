/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.trace.collector;

import com.navercorp.pinpoint.collector.grpc.config.GrpcReceiverProperties;
import com.navercorp.pinpoint.collector.grpc.config.ServerServiceDefinitions;
import com.navercorp.pinpoint.collector.grpc.ssl.GrpcSslProperties;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.collector.receiver.grpc.GrpcReceiver;
import com.navercorp.pinpoint.collector.receiver.grpc.monitor.Monitor;
import com.navercorp.pinpoint.common.server.util.IgnoreAddressFilter;
import com.navercorp.pinpoint.grpc.channelz.ChannelzRegistry;
import com.navercorp.pinpoint.grpc.security.SslContextFactory;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

/**
 * In-app TLS for the OTLP trace gRPC receiver. Gated by a dedicated flag so it can be toggled
 * independently of the basic collector's SSL ({@code pinpoint.modules.collector.grpc.ssl.enabled}).
 * Reuses the cert/key from {@code collector.receiver.grpc.ssl.*} and exposes a TLS listener on a
 * separate bind address (default 9448), sharing the plaintext receiver's service/executor.
 */
@Configuration
@ConditionalOnProperty(value = "pinpoint.modules.collector.grpc.otlptrace.ssl.enabled", havingValue = "true")
public class OtlpTraceCollectorGrpcSslModule {

    static final String SSL = "collector.receiver.grpc.ssl";
    static final String BIND_ADDRESS = "collector.receiver.grpc.otlp.trace.ssl.bindaddress";

    @Bean(SSL)
    @ConfigurationProperties(SSL)
    public GrpcSslProperties.Builder otlpTraceGrpcSslPropertiesBuilder() {
        return GrpcSslProperties.newBuilder();
    }

    @Bean(BIND_ADDRESS)
    @Validated
    @ConfigurationProperties(BIND_ADDRESS)
    public BindAddress.Builder otlpTraceGrpcSslBindAddressBuilder() {
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(9448);
        return builder;
    }

    @Bean
    public GrpcReceiver grpcOtlpTraceSslReceiver(@Qualifier(SSL)
                                                 GrpcSslProperties.Builder sslPropertiesBuilder,
                                                 @Qualifier(BIND_ADDRESS)
                                                 BindAddress.Builder sslBindAddressBuilder,
                                                 @Qualifier("grpcOtlpTraceReceiverProperties")
                                                 GrpcReceiverProperties grpcReceiverProperties,
                                                 @Qualifier("monitoredByteBufAllocator")
                                                 ByteBufAllocator byteBufAllocator,
                                                 IgnoreAddressFilter addressFilter,
                                                 @Qualifier("serviceList")
                                                 ServerServiceDefinitions spanServices,
                                                 ChannelzRegistry channelzRegistry,
                                                 @Qualifier("grpcOtlpTraceServerExecutor")
                                                 Executor grpcOtlpTraceExecutor,
                                                 Monitor monitor) throws SSLException {
        GrpcReceiver grpcReceiver = new GrpcReceiver();
        grpcReceiver.setBindAddress(sslBindAddressBuilder.build());
        grpcReceiver.setAddressFilter(addressFilter);
        grpcReceiver.setBindableServiceList(spanServices.getDefinitions());
        grpcReceiver.setChannelzRegistry(channelzRegistry);
        grpcReceiver.setExecutor(grpcOtlpTraceExecutor);
        grpcReceiver.setEnable(true);
        // Reuse the plaintext receiver's ServerOption (keepalive, inbound size, concurrent-calls, ...).
        grpcReceiver.setServerOption(grpcReceiverProperties.getServerOption());
        grpcReceiver.setByteBufAllocator(byteBufAllocator);
        grpcReceiver.setMonitor(monitor);
        grpcReceiver.setSslContext(newSslContext(sslPropertiesBuilder.build()));
        return grpcReceiver;
    }

    private SslContext newSslContext(GrpcSslProperties properties) throws SSLException {
        try (InputStream keyCertChain = properties.getKeyCertChainResource().getInputStream();
             InputStream key = properties.getKeyResource().getInputStream()) {
            SslContextFactory factory = new SslContextFactory(properties.getProviderType());
            return factory.forServer(keyCertChain, key);
        } catch (IOException e) {
            throw new SSLException(e);
        }
    }
}
