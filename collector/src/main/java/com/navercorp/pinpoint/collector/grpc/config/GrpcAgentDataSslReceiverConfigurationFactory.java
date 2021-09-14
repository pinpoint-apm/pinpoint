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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author Taejin Koo
 */
@Configuration
public class GrpcAgentDataSslReceiverConfigurationFactory {

    static final String AGENT_SSL_CONFIG = "grpcAgentSslReceiverConfig";

    public static final String BIND_ADDRESS = "collector.receiver.grpc.agent.ssl.bindaddress";

    public static final String SSL = "collector.receiver.grpc.ssl";

    public GrpcAgentDataSslReceiverConfigurationFactory() {
    }

    @Bean(BIND_ADDRESS)
    @ConfigurationProperties(BIND_ADDRESS)
    public BindAddress.Builder newBindAddressBuilder() {
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(9441);
        return builder;
    }

    @Bean(SSL)
    @ConfigurationProperties(SSL)
    public GrpcSslConfiguration.Builder newGrpcSslConfigurationBuilder() {
        return GrpcSslConfiguration.newBuilder();
    }

    @Bean(AGENT_SSL_CONFIG)
    public GrpcSslReceiverConfiguration newAgentReceiverConfig(Environment environment) throws Exception {

        boolean enable = environment.getProperty("collector.receiver.grpc.agent.ssl.enable", boolean.class, false);

        BindAddress bindAddress = newBindAddressBuilder().build();

        GrpcSslConfiguration grpcSslConfiguration = newGrpcSslConfigurationBuilder().build();

        return new GrpcSslReceiverConfiguration(enable, bindAddress, grpcSslConfiguration);
    }

}
