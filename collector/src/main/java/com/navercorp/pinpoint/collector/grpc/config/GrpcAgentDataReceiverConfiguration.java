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

import com.navercorp.pinpoint.collector.config.ExecutorProperties;
import com.navercorp.pinpoint.collector.receiver.BindAddress;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author emeroad
 */
@Configuration
public class GrpcAgentDataReceiverConfiguration {

    public static final String BIND_ADDRESS = "collector.receiver.grpc.agent.bindaddress";

    public static final String SERVER_EXECUTOR = "collector.receiver.grpc.agent.server.executor";

    public static final String SERVER_CALL_EXECUTOR = "collector.receiver.grpc.agent.server-call.executor";

    public static final String WORKER_EXECUTOR = "collector.receiver.grpc.agent.worker.executor";

    public static final String SERVER_OPTION = "collector.receiver.grpc.agent";

    public GrpcAgentDataReceiverConfiguration() {
    }

    @Bean(BIND_ADDRESS)
    @ConfigurationProperties(BIND_ADDRESS)
    public BindAddress.Builder newBindAddressBuilder() {
        BindAddress.Builder builder = BindAddress.newBuilder();
        builder.setPort(9991);
        return builder;
    }

    @Bean(SERVER_EXECUTOR)
    @ConfigurationProperties(SERVER_EXECUTOR)
    public ExecutorProperties.Builder newServerExecutorBuilder() {
        return ExecutorProperties.newBuilder();
    }

    @Bean(SERVER_CALL_EXECUTOR)
    @ConfigurationProperties(SERVER_CALL_EXECUTOR)
    public ExecutorProperties.Builder newServerCallExecutorBuilder() {
        return ExecutorProperties.newBuilder();
    }

    @Bean(WORKER_EXECUTOR)
    @ConfigurationProperties(WORKER_EXECUTOR)
    public ExecutorProperties.Builder newWorkerExecutorBuilder() {
        return ExecutorProperties.newBuilder();
    }

    @Bean(SERVER_OPTION)
    @ConfigurationProperties(SERVER_OPTION)
    public GrpcPropertiesServerOptionBuilder newServerOption() {
        // Server option
        return new GrpcPropertiesServerOptionBuilder();
    }

    @Bean
    public GrpcAgentDataReceiverProperties grpcAgentReceiverProperties(Environment environment) {
        boolean enable = environment.getProperty("collector.receiver.grpc.agent.enable", boolean.class, false);

        ServerOption serverOption = newServerOption().build();
        BindAddress bindAddress = newBindAddressBuilder().build();
        ExecutorProperties serverExecutor = newServerExecutorBuilder().build();
        ExecutorProperties serverCallExecutor = newServerCallExecutorBuilder().build();
        ExecutorProperties workerExecutor = newWorkerExecutorBuilder().build();

        return new GrpcAgentDataReceiverProperties(enable, bindAddress, serverExecutor, serverCallExecutor, workerExecutor, serverOption);
    }

}