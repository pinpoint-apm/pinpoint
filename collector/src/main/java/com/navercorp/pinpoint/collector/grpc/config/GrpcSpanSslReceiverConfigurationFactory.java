/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.collector.grpc.config;

import com.navercorp.pinpoint.collector.receiver.BindAddress;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Taejin Koo
 */
@Configuration
public class GrpcSpanSslReceiverConfigurationFactory {

    static final String SPAN_SSL_CONFIG = "grpcSpanSslReceiverConfig";

    public static final String BIND_ADDRESS = "collector.receiver.grpc.span.ssl.bindaddress";

    public GrpcSpanSslReceiverConfigurationFactory() {
    }

    @Bean(BIND_ADDRESS)
    @ConfigurationProperties(BIND_ADDRESS)
    public BindAddress.Builder newBindAddressBuilder() {
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(9443);
        return builder;
    }

    @Bean(SPAN_SSL_CONFIG)
    public GrpcSslReceiverConfiguration newAgentReceiverConfig(
            Environment environment,
            @Qualifier(BIND_ADDRESS) BindAddress.Builder bindAddressBuilder,
            @Qualifier(GrpcAgentDataSslReceiverConfigurationFactory.SSL) GrpcSslConfiguration.Builder sslConfigurationBuilder) throws Exception {

        boolean enable = environment.getProperty("collector.receiver.grpc.span.ssl.enable", boolean.class, false);

        BindAddress bindAddress = bindAddressBuilder.build();

        GrpcSslConfiguration grpcSslConfiguration = sslConfigurationBuilder.build();

        return new GrpcSslReceiverConfiguration(enable, bindAddress, grpcSslConfiguration);
    }

}
