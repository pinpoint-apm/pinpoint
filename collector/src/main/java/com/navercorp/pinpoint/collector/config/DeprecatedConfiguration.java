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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Properties;

/**
 * @author Taejin Koo
 */
@Deprecated
public final class DeprecatedConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String TCP_LISTEN_IP = "collector.tcpListenIp";
    private static final String TCP_LISTEN_PORT = "collector.tcpListenPort";

    private static final String TCP_WORKER_THREAD = "collector.tcpWorkerThread";
    private static final String TCP_WORKER_QUEUE_SIZE = "collector.tcpWorkerQueueSize";
    private static final String TCP_WORKER_MONITOR_ENABLE = "collector.tcpWorker.monitor";

    private static final String UDP_STAT_LISTEN_IP = "collector.udpStatListenIp";
    private static final String UDP_STAT_LISTEN_PORT = "collector.udpStatListenPort";

    private static final String UDP_STAT_WORKER_THREAD = "collector.udpStatWorkerThread";
    private static final String UDP_STAT_WORKER_QUEUE_SIZE = "collector.udpStatWorkerQueueSize";
    private static final String UDP_STAT_WORKER_MONITOR = "collector.udpStatWorker.monitor";
    private static final String UDP_STAT_SOCKET_RECEIVE_BUFFER_SIZE = "collector.udpStatSocketReceiveBufferSize";

    private static final String UDP_SPAN_LISTEN_IP = "collector.udpSpanListenIp";
    private static final String UDP_SPAN_LISTEN_PORT = "collector.udpSpanListenPort";

    private static final String UDP_SPAN_WORKER_THREAD = "collector.udpSpanWorkerThread";
    private static final String UDP_SPAN_WORKER_QUEUE_SIZE = "collector.udpSpanWorkerQueueSize";
    private static final String UDP_SPAN_WORKER_MONITOR = "collector.udpSpanWorker.monitor";
    private static final String UDP_SPAN_SOCKET_RECEIVE_BUFFER_SIZE = "collector.udpSpanSocketReceiveBufferSize";

    private static String[] DEPRECATED_PROPERTY_ARRAY = {
            TCP_LISTEN_IP,
            TCP_LISTEN_PORT,
            TCP_WORKER_THREAD,
            TCP_WORKER_QUEUE_SIZE,
            TCP_WORKER_MONITOR_ENABLE,
            UDP_STAT_LISTEN_IP,
            UDP_STAT_LISTEN_PORT,
            UDP_STAT_WORKER_THREAD,
            UDP_STAT_WORKER_QUEUE_SIZE,
            UDP_STAT_WORKER_MONITOR,
            UDP_STAT_SOCKET_RECEIVE_BUFFER_SIZE,
            UDP_SPAN_LISTEN_IP,
            UDP_SPAN_LISTEN_PORT,
            UDP_SPAN_WORKER_THREAD,
            UDP_SPAN_WORKER_QUEUE_SIZE,
            UDP_SPAN_WORKER_MONITOR,
            UDP_SPAN_SOCKET_RECEIVE_BUFFER_SIZE
    };

    private final Properties properties = new Properties();

    public DeprecatedConfiguration() {
    }

    public DeprecatedConfiguration(Properties properties) {
        Objects.requireNonNull(properties, "properties must not be null");

        for (String deprecatedPropertyKey : DEPRECATED_PROPERTY_ARRAY) {
            if (properties.containsKey(deprecatedPropertyKey)) {
                String value = properties.getProperty(deprecatedPropertyKey);

                logger.warn("deprecated configuration property {}={}. it will be removed 1.8.x", deprecatedPropertyKey, value);
                this.properties.put(deprecatedPropertyKey, value);
            }
        }
    }

    boolean isSetTcpListenIp() {
        return properties.containsKey(TCP_LISTEN_IP);
    }

    String getTcpListenIp() {
        return CollectorConfiguration.readString(properties, TCP_LISTEN_IP, null);
    }

    boolean isSetTcpListenPort() {
        return properties.containsKey(TCP_LISTEN_PORT);
    }

    int getTcpListenPort() {
        return CollectorConfiguration.readInt(properties, TCP_LISTEN_PORT, -1);
    }

    boolean isSetTcpWorkerThread() {
        return properties.containsKey(TCP_WORKER_THREAD);
    }

    int getTcpWorkerThread() {
        return CollectorConfiguration.readInt(properties, TCP_WORKER_THREAD, -1);
    }

    boolean isSetTcpWorkerQueueSize() {
        return properties.containsKey(TCP_WORKER_QUEUE_SIZE);
    }

    int getTcpWorkerQueueSize() {
        return CollectorConfiguration.readInt(properties, TCP_WORKER_QUEUE_SIZE, -1);
    }

    boolean isSetTcpWorkerMonitor() {
        return properties.containsKey(TCP_WORKER_MONITOR_ENABLE);
    }

    boolean isTcpWorkerMonitor() {
        return CollectorConfiguration.readBoolean(properties, TCP_WORKER_MONITOR_ENABLE);
    }

    boolean isSetUdpStatListenIp() {
        return properties.containsKey(UDP_STAT_LISTEN_IP);
    }

    String getUdpStatListenIp() {
        return CollectorConfiguration.readString(properties, UDP_STAT_LISTEN_IP, null);
    }

    boolean isSetUdpStatListenPort() {
        return properties.containsKey(UDP_STAT_LISTEN_PORT);
    }

    int getUdpStatListenPort() {
        return CollectorConfiguration.readInt(properties, UDP_STAT_LISTEN_PORT, -1);
    }

    boolean isSetUdpStatWorkerThread() {
        return properties.containsKey(UDP_STAT_WORKER_THREAD);
    }

    int getUdpStatWorkerThread() {
        return CollectorConfiguration.readInt(properties, UDP_STAT_WORKER_THREAD, -1);
    }

    boolean isSetUdpStatWorkerQueueSize() {
        return properties.containsKey(UDP_STAT_WORKER_QUEUE_SIZE);
    }

    int getUdpStatWorkerQueueSize() {
        return CollectorConfiguration.readInt(properties, UDP_STAT_WORKER_QUEUE_SIZE, -1);
    }

    boolean isSetUdpStatWorkerMonitor() {
        return properties.containsKey(UDP_STAT_WORKER_MONITOR);
    }

    boolean isUdpStatWorkerMonitor() {
        return CollectorConfiguration.readBoolean(properties, UDP_STAT_WORKER_MONITOR);
    }

    boolean isSetUdpStatSocketReceiveBufferSize() {
        return properties.containsKey(UDP_STAT_SOCKET_RECEIVE_BUFFER_SIZE);
    }

    public int getUdpStatSocketReceiveBufferSize() {
        return CollectorConfiguration.readInt(properties, UDP_STAT_SOCKET_RECEIVE_BUFFER_SIZE, -1);
    }

    boolean isSetUdpSpanListenIp() {
        return properties.containsKey(UDP_SPAN_LISTEN_IP);
    }

    String getUdpSpanListenIp() {
        return CollectorConfiguration.readString(properties, UDP_SPAN_LISTEN_IP, null);
    }

    boolean isSetUdpSpanListenPort() {
        return properties.containsKey(UDP_SPAN_LISTEN_PORT);
    }

    int getUdpSpanListenPort() {
        return CollectorConfiguration.readInt(properties, UDP_SPAN_LISTEN_PORT, -1);
    }

    boolean isSetUdpSpanWorkerThread() {
        return properties.containsKey(UDP_SPAN_WORKER_THREAD);
    }

    int getUdpSpanWorkerThread() {
        return CollectorConfiguration.readInt(properties, UDP_SPAN_WORKER_THREAD, -1);
    }

    boolean isSetUdpSpanWorkerQueueSize() {
        return properties.containsKey(UDP_SPAN_WORKER_QUEUE_SIZE);
    }

    int getUdpSpanWorkerQueueSize() {
        return CollectorConfiguration.readInt(properties, UDP_SPAN_WORKER_QUEUE_SIZE, -1);
    }

    boolean isSetUdpSpanWorkerMonitor() {
        return properties.containsKey(UDP_SPAN_WORKER_MONITOR);
    }

    boolean isUdpSpanWorkerMonitor() {
        return CollectorConfiguration.readBoolean(properties, UDP_SPAN_WORKER_MONITOR);
    }

    boolean isSetUdpSpanSocketReceiveBufferSize() {
        return properties.containsKey(UDP_SPAN_SOCKET_RECEIVE_BUFFER_SIZE);
    }

    int getUdpSpanSocketReceiveBufferSize() {
        return CollectorConfiguration.readInt(properties, UDP_SPAN_SOCKET_RECEIVE_BUFFER_SIZE, -1);
    }

}
