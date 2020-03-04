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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.server.config.AnnotationVisitor;
import com.navercorp.pinpoint.common.server.config.LoggingEvent;
import com.navercorp.pinpoint.grpc.server.ServerOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Taejin Koo
 */
//ConfigurationProeprties(prefix="prefix") spring-boot
@Configuration
public class GrpcAgentDataReceiverConfiguration {

    private final Logger logger = LoggerFactory.getLogger(GrpcAgentDataReceiverConfiguration.class);

    private static final String GRPC_PREFIX = "collector.receiver.grpc.agent";

    @Value("${collector.receiver.grpc.agent.enable}")
    private boolean grpcEnable;
    @Value("${collector.receiver.grpc.agent.ip:0.0.0.0}")
    private String grpcBindIp;
    @Value("${collector.receiver.grpc.agent.port:9991}")
    private int grpcBindPort;
    @Value("${collector.receiver.grpc.agent.server.executor.thread.size:128}")
    private int grpcServerExecutorThreadSize;
    @Value("${collector.receiver.grpc.agent.server.executor.queue.size:5120}")
    private int grpcServerExecutorQueueSize;
    @Value("${collector.receiver.grpc.agent.server.executor.monitor.enable:false}")
    private boolean grpcServerExecutorMonitorEnable;

    @Value("${collector.receiver.grpc.agent.worker.executor.thread.size:128}")
    private int grpcWorkerExecutorThreadSize;
    @Value("${collector.receiver.grpc.agent.worker.executor.queue.size:5120}")
    private int grpcWorkerExecutorQueueSize;
    @Value("${collector.receiver.grpc.agent.worker.executor.monitor.enable:false}")
    private boolean grpcWorkerExecutorMonitorEnable;

    private ServerOption grpcServerOption;

    public GrpcAgentDataReceiverConfiguration() {
    }

    public GrpcAgentDataReceiverConfiguration(Properties properties) {
        // WARNING ServerOption does not support PropertyPlaceholder
        loadServerOption(properties);
    }

    // workaround
    public void loadServerOption(Properties properties) {
        Objects.requireNonNull(properties, "properties");
        // Server option
        final ServerOption.Builder serverOptionBuilder = GrpcPropertiesServerOptionBuilder.newBuilder(properties, GRPC_PREFIX);
        this.grpcServerOption = serverOptionBuilder.build();

    }

    @PostConstruct
    public void log() {
        logger.info("{}", this);
        Class<Value> valueClass = Value.class;
        AnnotationVisitor visitor = new AnnotationVisitor(valueClass);
        visitor.visit(this, new LoggingEvent(logger));
    }

    public String getGrpcBindIp() {
        return grpcBindIp;
    }

    public int getGrpcBindPort() {
        return grpcBindPort;
    }

    public boolean isGrpcEnable() {
        return grpcEnable;
    }

    public int getGrpcServerExecutorThreadSize() {
        return grpcServerExecutorThreadSize;
    }

    public int getGrpcServerExecutorQueueSize() {
        return grpcServerExecutorQueueSize;
    }

    public boolean isGrpcServerExecutorMonitorEnable() {
        return grpcServerExecutorMonitorEnable;
    }

    public int getGrpcWorkerExecutorThreadSize() {
        return grpcWorkerExecutorThreadSize;
    }

    public int getGrpcWorkerExecutorQueueSize() {
        return grpcWorkerExecutorQueueSize;
    }

    public boolean isGrpcWorkerExecutorMonitorEnable() {
        return grpcWorkerExecutorMonitorEnable;
    }

    public ServerOption getGrpcServerOption() {
        return grpcServerOption;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GrpcAgentDataReceiverConfiguration{");
        sb.append("grpcEnable=").append(grpcEnable);
        sb.append(", grpcBindIp='").append(grpcBindIp).append('\'');
        sb.append(", grpcBindPort=").append(grpcBindPort);
        sb.append(", grpcServerExecutorThreadSize=").append(grpcServerExecutorThreadSize);
        sb.append(", grpcServerExecutorQueueSize=").append(grpcServerExecutorQueueSize);
        sb.append(", grpcServerExecutorMonitorEnable=").append(grpcServerExecutorMonitorEnable);
        sb.append(", grpcWorkerExecutorThreadSize=").append(grpcWorkerExecutorThreadSize);
        sb.append(", grpcWorkerExecutorQueueSize=").append(grpcWorkerExecutorQueueSize);
        sb.append(", grpcWorkerExecutorMonitorEnable=").append(grpcWorkerExecutorMonitorEnable);
        sb.append(", grpcServerOption=").append(grpcServerOption);
        sb.append('}');
        return sb.toString();
    }
}