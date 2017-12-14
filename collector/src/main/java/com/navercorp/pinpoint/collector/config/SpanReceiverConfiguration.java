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

import java.util.Objects;
import java.util.Properties;

/**
 * @author Taejin Koo
 */
public final class SpanReceiverConfiguration implements DataReceiverGroupConfiguration {

    private static final String PREFIX = "collector.receiver.span";

    private static final String TCP_ENABLE = PREFIX + ".tcp";
    private final boolean isTcpEnable;
    private static final String TCP_BIND_IP = PREFIX + ".tcp.ip";
    private final String tcpBindIp;
    private static final String TCP_BIND_PORT = PREFIX + ".tcp.port";
    private final int tcpBindPort;

    private static final String UDP_ENABLE = PREFIX + ".udp";
    private final boolean isUdpEnable;
    private static final String UDP_BIND_IP = PREFIX + ".udp.ip";
    private final String udpBindIp;
    private static final String UDP_BIND_PORT = PREFIX + ".udp.port";
    private final int udpBindPort;
    private static final String UDP_RECEIVE_BUFFER_SIZE = PREFIX + ".udp.receiveBufferSize";
    private final int udpReceiveBufferSize;

    private static final String WORKER_THREAD_SIZE = PREFIX + ".worker.threadSize";
    private final int workerThreadSize;
    private static final String WORKER_QUEUE_SIZE = PREFIX + ".worker.queueSize";
    private final int workerQueueSize;

    private static final String WORKER_MONITOR_ENABLE = PREFIX + ".worker.monitor";
    private final boolean workerMonitorEnable;


    public SpanReceiverConfiguration(Properties properties, DeprecatedConfiguration deprecatedConfiguration) {
        Objects.requireNonNull(properties, "properties must not be null");
        Objects.requireNonNull(deprecatedConfiguration, "deprecatedConfiguration must not be null");

        this.isTcpEnable = CollectorConfiguration.readBoolean(properties, TCP_ENABLE);
        this.tcpBindIp = CollectorConfiguration.readString(properties, TCP_BIND_IP, CollectorConfiguration.DEFAULT_LISTEN_IP);
        this.tcpBindPort = CollectorConfiguration.readInt(properties, TCP_BIND_PORT, -1);

        this.isUdpEnable = isUdpEnable(properties, deprecatedConfiguration, true);
        this.udpBindIp = getUdpBindIp(properties, deprecatedConfiguration, CollectorConfiguration.DEFAULT_LISTEN_IP);
        this.udpBindPort = getUdpBindPort(properties, deprecatedConfiguration, 9996);
        this.udpReceiveBufferSize = getUdpReceiveBufferSize(properties, deprecatedConfiguration, 1024 * 4096);

        this.workerThreadSize = getWorkerThreadSize(properties, deprecatedConfiguration, 256);
        Assert.isTrue(workerThreadSize > 0, "workerThreadSize must be greater than 0");
        this.workerQueueSize = getWorkerQueueSize(properties, deprecatedConfiguration, 1024 * 5);
        Assert.isTrue(workerQueueSize > 0, "workerQueueSize must be greater than 0");

        this.workerMonitorEnable = isWorkerThreadMonitorEnable(properties, deprecatedConfiguration);

        validate();
    }

    private void validate() {
        Assert.isTrue(isTcpEnable || isUdpEnable, "spanReceiver does not allow tcp and udp disable");

        if (isTcpEnable) {
            Objects.requireNonNull(tcpBindIp, "tcpBindIp must not be null");
            Assert.isTrue(tcpBindPort > 0, "tcpBindPort must be greater than 0");
        }

        if (isUdpEnable) {
            Objects.requireNonNull(udpBindIp, "udpBindIp must not be null");
            Assert.isTrue(udpBindPort > 0, "udpBindPort must be greater than 0");
            Assert.isTrue(udpReceiveBufferSize > 0, "udpReceiveBufferSize must be greater than 0");
        }
    }

    private boolean isUdpEnable(Properties properties, DeprecatedConfiguration deprecatedConfiguration, boolean defaultValue) {
        if (properties.containsKey(UDP_ENABLE)) {
            return CollectorConfiguration.readBoolean(properties, UDP_ENABLE);
        }

        return defaultValue;
    }

    private String getUdpBindIp(Properties properties, DeprecatedConfiguration deprecatedConfiguration, String defaultValue) {
        if (properties.containsKey(UDP_BIND_IP)) {
            return CollectorConfiguration.readString(properties, UDP_BIND_IP, null);
        }

        if (deprecatedConfiguration.isSetUdpSpanListenIp()) {
            return deprecatedConfiguration.getUdpSpanListenIp();
        }

        return defaultValue;
    }

    private int getUdpBindPort(Properties properties, DeprecatedConfiguration deprecatedConfiguration, int defaultValue) {
        if (properties.containsKey(UDP_BIND_PORT)) {
            return CollectorConfiguration.readInt(properties, UDP_BIND_PORT, -1);
        }

        if (deprecatedConfiguration.isSetUdpSpanListenPort()) {
            return deprecatedConfiguration.getUdpSpanListenPort();
        }

        return defaultValue;
    }


    private int getUdpReceiveBufferSize(Properties properties, DeprecatedConfiguration deprecatedConfiguration, int defaultValue) {
        if (properties.containsKey(UDP_RECEIVE_BUFFER_SIZE)) {
            return CollectorConfiguration.readInt(properties, UDP_RECEIVE_BUFFER_SIZE, -1);
        }

        if (deprecatedConfiguration.isSetUdpSpanSocketReceiveBufferSize()) {
            return deprecatedConfiguration.getUdpSpanSocketReceiveBufferSize();
        }

        return defaultValue;
    }

    private int getWorkerThreadSize(Properties properties, DeprecatedConfiguration deprecatedConfiguration, int defaultValue) {
        if (properties.containsKey(WORKER_THREAD_SIZE)) {
            return CollectorConfiguration.readInt(properties, WORKER_THREAD_SIZE, -1);
        }

        if (deprecatedConfiguration.isSetUdpSpanWorkerThread()) {
            return deprecatedConfiguration.getUdpSpanWorkerThread();
        }

        return defaultValue;
    }

    private int getWorkerQueueSize(Properties properties, DeprecatedConfiguration deprecatedConfiguration, int defaultValue) {
        if (properties.containsKey(WORKER_QUEUE_SIZE)) {
            return CollectorConfiguration.readInt(properties, WORKER_QUEUE_SIZE, -1);
        }

        if (deprecatedConfiguration.isSetUdpSpanWorkerQueueSize()) {
            return deprecatedConfiguration.getUdpSpanWorkerQueueSize();
        }

        return defaultValue;
    }

    private boolean isWorkerThreadMonitorEnable(Properties properties, DeprecatedConfiguration deprecatedConfiguration) {
        if (properties.containsKey(WORKER_MONITOR_ENABLE)) {
            return CollectorConfiguration.readBoolean(properties, WORKER_MONITOR_ENABLE);
        }

        if (deprecatedConfiguration.isSetUdpSpanWorkerMonitor()) {
            return deprecatedConfiguration.isUdpSpanWorkerMonitor();
        }

        return false;
    }

    @Override
    public boolean isTcpEnable() {
        return isTcpEnable;
    }

    @Override
    public String getTcpBindIp() {
        return tcpBindIp;
    }

    @Override
    public int getTcpBindPort() {
        return tcpBindPort;
    }

    @Override
    public boolean isUdpEnable() {
        return isUdpEnable;
    }

    @Override
    public String getUdpBindIp() {
        return udpBindIp;
    }

    @Override
    public int getUdpBindPort() {
        return udpBindPort;
    }

    @Override
    public int getUdpReceiveBufferSize() {
        return udpReceiveBufferSize;
    }

    @Override
    public int getWorkerThreadSize() {
        return workerThreadSize;
    }

    @Override
    public int getWorkerQueueSize() {
        return workerQueueSize;
    }

    @Override
    public boolean isWorkerMonitorEnable() {
        return workerMonitorEnable;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SpanReceiverConfig{");
        sb.append("isTcpEnable=").append(isTcpEnable);
        sb.append(", tcpBindIp='").append(tcpBindIp).append('\'');
        sb.append(", tcpBindPort=").append(tcpBindPort);
        sb.append(", isUdpEnable=").append(isUdpEnable);
        sb.append(", udpBindIp='").append(udpBindIp).append('\'');
        sb.append(", udpBindPort=").append(udpBindPort);
        sb.append(", udpReceiveBufferSize=").append(udpReceiveBufferSize);
        sb.append(", workerThreadSize=").append(workerThreadSize);
        sb.append(", workerQueueSize=").append(workerQueueSize);
        sb.append(", workerMonitorEnable=").append(workerMonitorEnable);
        sb.append('}');
        return sb.toString();
    }

}
