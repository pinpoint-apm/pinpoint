/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.server.ServerOption;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Taejin Koo
 */
public final class AgentBaseDataReceiverConfiguration {

    private static final String PREFIX = "collector.receiver.base";
    private static final String BIND_IP = PREFIX + ".ip";
    private static final String BIND_PORT = PREFIX + ".port";
    private static final String WORKER_THREAD_SIZE = PREFIX + ".worker.threadSize";
    private static final String WORKER_QUEUE_SIZE = PREFIX + ".worker.queueSize";
    private static final String WORKER_MONITOR_ENABLE = PREFIX + ".worker.monitor";

    private static final String GRPC_PREFIX = "collector.receiver.grpc.agent";
    private static final String GRPC_ENABLE = GRPC_PREFIX + ".enable";
    private static final String GRPC_BIND_IP = GRPC_PREFIX + ".ip";
    private static final String GRPC_BIND_PORT = GRPC_PREFIX + ".port";
    private static final String GRPC_SERVER_EXECUTOR_THREAD_SIZE = GRPC_PREFIX + ".server.executor.thread.size";
    private static final String GRPC_SERVER_EXECUTOR_QUEUE_SIZE = GRPC_PREFIX + ".server.executor.queue.size";
    private static final String GRPC_SERVER_EXECUTOR_MONITOR_ENABLE = GRPC_PREFIX + ".server.executor.monitor.enable";

    private static final String GRPC_WORKER_EXECUTOR_THREAD_SIZE = GRPC_PREFIX + ".worker.executor.thread.size";
    private static final String GRPC_WORKER_EXECUTOR_QUEUE_SIZE = GRPC_PREFIX + ".worker.executor.queue.size";
    private static final String GRPC_WORKER_EXECUTOR_MONITOR_ENABLE = GRPC_PREFIX + ".worker.executor.monitor.enable";

    private final String bindIp;
    private final int bindPort;
    private final int workerThreadSize;
    private final int workerQueueSize;
    private final boolean workerMonitorEnable;

    private final boolean grpcEnable;
    private final String grpcBindIp;
    private final int grpcBindPort;
    private final int grpcServerExecutorThreadSize;
    private final int grpcServerExecutorQueueSize;
    private final boolean grpcServerExecutorMonitorEnable;
    private final int grpcWorkerExecutorThreadSize;
    private final int grpcWorkerExecutorQueueSize;
    private final boolean grpcWorkerExecutorMonitorEnable;
    private final ServerOption grpcServerOption;

    public AgentBaseDataReceiverConfiguration(Properties properties, DeprecatedConfiguration deprecatedConfiguration) {
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(deprecatedConfiguration, "deprecatedConfiguration must not be null");

        this.bindIp = getBindIp(properties, deprecatedConfiguration, CollectorConfiguration.DEFAULT_LISTEN_IP);
        Objects.requireNonNull(bindIp);
        this.bindPort = getBindPort(properties, deprecatedConfiguration, 9994);
        Assert.isTrue(bindPort > 0, "bindPort must be greater than 0");
        this.workerThreadSize = getWorkerThreadSize(properties, deprecatedConfiguration, 128);
        Assert.isTrue(workerThreadSize > 0, "workerThreadSize must be greater than 0");
        this.workerQueueSize = getWorkerQueueSize(properties, deprecatedConfiguration, 1024 * 5);
        Assert.isTrue(workerQueueSize > 0, "workerQueueSize must be greater than 0");
        this.workerMonitorEnable = isWorkerThreadMonitorEnable(properties, deprecatedConfiguration);

        // gRPC
        this.grpcEnable = CollectorConfiguration.readBoolean(properties, GRPC_ENABLE);
        this.grpcBindIp = CollectorConfiguration.readString(properties, GRPC_BIND_IP, CollectorConfiguration.DEFAULT_LISTEN_IP);
        Objects.requireNonNull(grpcBindIp);
        this.grpcBindPort = CollectorConfiguration.readInt(properties, GRPC_BIND_PORT, 9991);
        Assert.isTrue(grpcBindPort > 0, "grpcBindPort must be greater than 0");

        // Server executor
        this.grpcServerExecutorThreadSize = CollectorConfiguration.readInt(properties, GRPC_SERVER_EXECUTOR_THREAD_SIZE, 128);
        Assert.isTrue(grpcServerExecutorThreadSize > 0, "grpcServerExecutorThreadSize must be greater than 0");
        this.grpcServerExecutorQueueSize = CollectorConfiguration.readInt(properties, GRPC_SERVER_EXECUTOR_QUEUE_SIZE, 1024 * 5);
        Assert.isTrue(grpcServerExecutorQueueSize > 0, "grpcServerExecutorQueueSize must be greater than 0");
        this.grpcServerExecutorMonitorEnable = CollectorConfiguration.readBoolean(properties, GRPC_SERVER_EXECUTOR_MONITOR_ENABLE);

        // Work executor
        this.grpcWorkerExecutorThreadSize = CollectorConfiguration.readInt(properties, GRPC_WORKER_EXECUTOR_THREAD_SIZE, 128);
        Assert.isTrue(grpcWorkerExecutorThreadSize > 0, "grpcWorkerExecutorThreadSize must be greater than 0");
        this.grpcWorkerExecutorQueueSize = CollectorConfiguration.readInt(properties, GRPC_WORKER_EXECUTOR_QUEUE_SIZE, 1024 * 5);
        Assert.isTrue(grpcWorkerExecutorQueueSize > 0, "grpcWorkerExecutorQueueSize must be greater than 0");
        this.grpcWorkerExecutorMonitorEnable = CollectorConfiguration.readBoolean(properties, GRPC_WORKER_EXECUTOR_MONITOR_ENABLE);

        // Server option
        final ServerOption.Builder serverOptionBuilder = GrpcPropertiesServerOptionBuilder.newBuilder(properties, GRPC_PREFIX);
        this.grpcServerOption = serverOptionBuilder.build();
    }

    private String getBindIp(Properties properties, DeprecatedConfiguration deprecatedConfiguration, String defaultValue) {
        if (properties.containsKey(BIND_IP)) {
            return CollectorConfiguration.readString(properties, BIND_IP, null);
        }

        if (deprecatedConfiguration.isSetTcpListenIp()) {
            return deprecatedConfiguration.getTcpListenIp();
        }

        return defaultValue;
    }

    private int getBindPort(Properties properties, DeprecatedConfiguration deprecatedConfiguration, int defaultValue) {
        if (properties.containsKey(BIND_PORT)) {
            return CollectorConfiguration.readInt(properties, BIND_PORT, -1);
        }

        if (deprecatedConfiguration.isSetTcpListenPort()) {
            return deprecatedConfiguration.getTcpListenPort();
        }

        return defaultValue;
    }

    private int getWorkerThreadSize(Properties properties, DeprecatedConfiguration deprecatedConfiguration, int defaultValue) {
        if (properties.containsKey(WORKER_THREAD_SIZE)) {
            return CollectorConfiguration.readInt(properties, WORKER_THREAD_SIZE, -1);
        }

        if (deprecatedConfiguration.isSetTcpWorkerThread()) {
            return deprecatedConfiguration.getTcpWorkerThread();
        }

        return defaultValue;
    }

    private int getWorkerQueueSize(Properties properties, DeprecatedConfiguration deprecatedConfiguration, int defaultValue) {
        if (properties.containsKey(WORKER_QUEUE_SIZE)) {
            return CollectorConfiguration.readInt(properties, WORKER_QUEUE_SIZE, -1);
        }

        if (deprecatedConfiguration.isSetTcpWorkerQueueSize()) {
            return deprecatedConfiguration.getTcpWorkerQueueSize();
        }

        return defaultValue;
    }

    private boolean isWorkerThreadMonitorEnable(Properties properties, DeprecatedConfiguration deprecatedConfiguration) {
        if (properties.containsKey(WORKER_MONITOR_ENABLE)) {
            return CollectorConfiguration.readBoolean(properties, WORKER_MONITOR_ENABLE);
        }

        if (deprecatedConfiguration.isSetTcpWorkerMonitor()) {
            return deprecatedConfiguration.isTcpWorkerMonitor();
        }

        return false;
    }

    public String getBindIp() {
        return bindIp;
    }

    public int getBindPort() {
        return bindPort;
    }

    public int getWorkerThreadSize() {
        return workerThreadSize;
    }

    public int getWorkerQueueSize() {
        return workerQueueSize;
    }

    public boolean isWorkerMonitorEnable() {
        return workerMonitorEnable;
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
        final StringBuilder sb = new StringBuilder("AgentBaseDataReceiverConfiguration{");
        sb.append("bindIp='").append(bindIp).append('\'');
        sb.append(", bindPort=").append(bindPort);
        sb.append(", workerThreadSize=").append(workerThreadSize);
        sb.append(", workerQueueSize=").append(workerQueueSize);
        sb.append(", workerMonitorEnable=").append(workerMonitorEnable);
        sb.append(", grpcEnable=").append(grpcEnable);
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