/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.log.collector.grpc;

import com.navercorp.pinpoint.collector.grpc.config.GrpcPropertiesServerOptionBuilder;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author youngjin.kim2
 */
@Configuration
@EnableConfigurationProperties
public class GrpcLogReceiverPropertiesConfig {

    public static final String BIND_ADDRESS = "collector.receiver.grpc.log.bindaddress";

    public static final String SERVER_EXECUTOR = "collector.receiver.grpc.log.server.executor";

    public static final String SERVER_CALL_EXECUTOR = "collector.receiver.grpc.log.server-call.executor";

    public static final String WORKER_EXECUTOR = "collector.receiver.grpc.log.worker.executor";

    public static final String SERVER_OPTION = "collector.receiver.grpc.log";

    public GrpcLogReceiverPropertiesConfig() {
    }

    @Bean(BIND_ADDRESS)
    @ConfigurationProperties(BIND_ADDRESS)
    public BindAddress.Builder newBindAddressBuilder() {
        return BindAddress.newBuilder();
    }

    @Bean
    @ConfigurationProperties(SERVER_EXECUTOR)
    public MonitoringExecutorProperties grpcLogServerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @ConfigurationProperties(SERVER_CALL_EXECUTOR)
    public MonitoringExecutorProperties grpcLogServerCallExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean
    @ConfigurationProperties(WORKER_EXECUTOR)
    public MonitoringExecutorProperties grpcLogWorkerExecutorProperties() {
        return new MonitoringExecutorProperties();
    }

    @Bean(SERVER_OPTION)
    @ConfigurationProperties(SERVER_OPTION)
    public GrpcPropertiesServerOptionBuilder newServerOption() {
        // Server option
        return new GrpcPropertiesServerOptionBuilder();
    }

    @Bean
    public GrpcLogReceiverProperties grpcLogReceiverConfig(Environment environment) {
        boolean enable = environment.getProperty("collector.receiver.grpc.log.enable", boolean.class, false);

        final ServerOption serverOption = newServerOption().build();
        final BindAddress bindAddress = newBindAddressBuilder().build();

        return new GrpcLogReceiverProperties(
                enable,
                bindAddress,
                serverOption
        );
    }

}
