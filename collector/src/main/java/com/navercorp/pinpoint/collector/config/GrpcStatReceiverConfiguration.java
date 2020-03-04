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
import com.navercorp.pinpoint.common.util.Assert;
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
@Configuration
public class GrpcStatReceiverConfiguration {
    private final Logger logger = LoggerFactory.getLogger(GrpcStatReceiverConfiguration.class);

    private static final String GRPC_PREFIX = "collector.receiver.grpc.stat";

    @Value("${collector.receiver.grpc.stat.enable:false}")
    private boolean isGrpcEnable;
    @Value("${collector.receiver.grpc.stat.ip:0.0.0.0}")
    private String grpcBindIp;
    @Value("${collector.receiver.grpc.stat.port:9992}")
    private int grpcBindPort;

    @Value("${collector.receiver.grpc.stat.server.executor.thread.size:128}")
    private int grpcServerExecutorThreadSize;
    @Value("${collector.receiver.grpc.stat.server.executor.queue.size:5120}")
    private int grpcServerExecutorQueueSize;
    @Value("${collector.receiver.grpc.stat.server.executor.monitor.enable:false}")
    private boolean grpcServerExecutorMonitorEnable;

    @Value("${collector.receiver.grpc.stat.worker.executor.thread.size:128}")
    private int grpcWorkerExecutorThreadSize;
    @Value("${collector.receiver.grpc.stat.worker.executor.queue.size:5120}")
    private int grpcWorkerExecutorQueueSize;
    @Value("${collector.receiver.grpc.stat.worker.executor.monitor.enable:false}")
    private boolean grpcWorkerExecutorMonitorEnable;

    @Value("${collector.receiver.grpc.stat.stream.scheduler.thread.size:1}")
    private int grpcStreamSchedulerThreadSize;
    @Value("${collector.receiver.grpc.stat.stream.call.init.request.count:1000}")
    private int grpcStreamCallInitRequestCount;
    @Value("${collector.receiver.grpc.stat.stream.scheduler.period.millis:64}")
    private int grpcStreamSchedulerPeriodMillis;
    @Value("${collector.receiver.grpc.stat.stream.scheduler.recovery.message.count:10}")
    private int grpcStreamSchedulerRecoveryMessageCount;

    private ServerOption grpcServerOption;

    public GrpcStatReceiverConfiguration() {
    }

    public GrpcStatReceiverConfiguration(Properties properties) {
        // WARNING ServerOption does not support PropertyPlaceholder
        loadServerOption(properties);
    }

    public void loadServerOption(Properties properties) {
        Objects.requireNonNull(properties, "properties");
        // Server option
        final ServerOption.Builder serverOptionBuilder = GrpcPropertiesServerOptionBuilder.newBuilder(properties, GRPC_PREFIX);
        this.grpcServerOption = serverOptionBuilder.build();
    }

    @PostConstruct
    public void validate() {
        logger.info("{}", this);
        AnnotationVisitor visitor = new AnnotationVisitor(Value.class);
        visitor.visit(this, new LoggingEvent(logger));

        // Server executor
        Assert.isTrue(grpcServerExecutorThreadSize > 0, "grpcServerExecutorThreadSize must be greater than 0");
        Assert.isTrue(grpcServerExecutorQueueSize > 0, "grpcServerExecutorQueueSize must be greater than 0");

        // Work executor
        Assert.isTrue(grpcWorkerExecutorThreadSize > 0, "grpcWorkerExecutorThreadSize must be greater than 0");
        Assert.isTrue(grpcWorkerExecutorQueueSize > 0, "grpcWorkerExecutorQueueSize must be greater than 0");
        Assert.isTrue(grpcStreamSchedulerThreadSize > 0, "grpcStreamSchedulerThreadSize must be greater than 0");


    }


    public boolean isGrpcEnable() {
        return isGrpcEnable;
    }

    public String getGrpcBindIp() {
        return grpcBindIp;
    }

    public int getGrpcBindPort() {
        return grpcBindPort;
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

    public int getGrpcStreamSchedulerThreadSize() {
        return grpcStreamSchedulerThreadSize;
    }

    public int getGrpcStreamCallInitRequestCount() {
        return grpcStreamCallInitRequestCount;
    }

    public int getGrpcStreamSchedulerPeriodMillis() {
        return grpcStreamSchedulerPeriodMillis;
    }

    public int getGrpcStreamSchedulerRecoveryMessageCount() {
        return grpcStreamSchedulerRecoveryMessageCount;
    }

    public ServerOption getGrpcServerOption() {
        return grpcServerOption;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GrpcStatReceiverConfiguration{");
        sb.append("isGrpcEnable=").append(isGrpcEnable);
        sb.append(", grpcBindIp='").append(grpcBindIp).append('\'');
        sb.append(", grpcBindPort=").append(grpcBindPort);
        sb.append(", grpcServerExecutorThreadSize=").append(grpcServerExecutorThreadSize);
        sb.append(", grpcServerExecutorQueueSize=").append(grpcServerExecutorQueueSize);
        sb.append(", grpcServerExecutorMonitorEnable=").append(grpcServerExecutorMonitorEnable);
        sb.append(", grpcWorkerExecutorThreadSize=").append(grpcWorkerExecutorThreadSize);
        sb.append(", grpcWorkerExecutorQueueSize=").append(grpcWorkerExecutorQueueSize);
        sb.append(", grpcWorkerExecutorMonitorEnable=").append(grpcWorkerExecutorMonitorEnable);
        sb.append(", grpcStreamSchedulerThreadSize=").append(grpcStreamSchedulerThreadSize);
        sb.append(", grpcStreamCallInitRequestCount=").append(grpcStreamCallInitRequestCount);
        sb.append(", grpcStreamSchedulerPeriodMillis=").append(grpcStreamSchedulerPeriodMillis);
        sb.append(", grpcStreamSchedulerRecoveryMessageCount=").append(grpcStreamSchedulerRecoveryMessageCount);
        sb.append(", grpcServerOption=").append(grpcServerOption);
        sb.append('}');
        return sb.toString();
    }
}