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

package com.navercorp.pinpoint.collector.grpc.ssl;

import com.navercorp.pinpoint.collector.receiver.BindAddress;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * @author Taejin Koo
 */
@Configuration
public class GrpcAgentSslConfiguration {

    public static final String BIND_ADDRESS = "collector.receiver.grpc.agent.ssl.bindaddress";

    public static final String SSL = "collector.receiver.grpc.ssl";

    public GrpcAgentSslConfiguration() {
    }

    @Bean(BIND_ADDRESS)
    @Validated
    @ConfigurationProperties(BIND_ADDRESS)
    public BindAddress.Builder newBindAddressBuilder() {
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(9441);
        return builder;
    }

    @Bean(SSL)
    @ConfigurationProperties(SSL)
    public GrpcSslProperties.Builder newGrpcSslConfigurationBuilder() {
        return GrpcSslProperties.newBuilder();
    }

    @Bean
    public GrpcSslReceiverProperties grpcAgentSslReceiverProperties(
            @Qualifier(GrpcAgentSslConfiguration.SSL) GrpcSslProperties.Builder sslPropertiesBuilder) {

        BindAddress bindAddress = newBindAddressBuilder().build();

        GrpcSslProperties grpcSslConfiguration = sslPropertiesBuilder.build();

        return new GrpcSslReceiverProperties(bindAddress, grpcSslConfiguration);
    }

}
