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

package com.navercorp.pinpoint.profiler.context.grpc.config;

import com.navercorp.pinpoint.bootstrap.config.Value;
import com.navercorp.pinpoint.bootstrap.config.util.ValueAnnotationProcessor;
import com.navercorp.pinpoint.bootstrap.module.JavaModule;
import com.navercorp.pinpoint.bootstrap.util.spring.PropertyPlaceholderHelper;
import com.navercorp.pinpoint.grpc.client.config.ClientOption;

import java.util.Properties;

/**
 * NOTE module accessibility
 * @see com.navercorp.pinpoint.bootstrap.java9.module.ModuleSupport#addPermissionToValueAnnotation(JavaModule)
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class GrpcTransportConfig {

    public static final String SYSTEM_PROPERTY_NETTY_TRY_REFLECTION_SET_ACCESSIBLE = "io.netty.tryReflectionSetAccessible";
    public static final String KEY_PROFILER_CONFIG_NETTY_TRY_REFLECTION_SET_ACCESSIBLE = "profiler.system.property." + SYSTEM_PROPERTY_NETTY_TRY_REFLECTION_SET_ACCESSIBLE;

    public static final String SYSTEM_PROPERTY_NETTY_NOPREFERDIRECT = "io.netty.noPreferDirect";
    public static final String KEY_PROFILER_CONFIG_NETTY_NOPREFERDIRECT = "profiler.system.property." + SYSTEM_PROPERTY_NETTY_NOPREFERDIRECT;

    private static final String DEFAULT_IP = "127.0.0.1";
    private static final long DEFAULT_CLIENT_REQUEST_TIMEOUT = 6000;
    private static final int DEFAULT_AGENT_SENDER_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_METADATA_SENDER_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_SPAN_SENDER_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_STAT_SENDER_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_AGENT_COLLECTOR_PORT = 9991;
    private static final int DEFAULT_STAT_COLLECTOR_PORT = 9992;
    private static final int DEFAULT_SPAN_COLLECTOR_PORT = 9993;
    private static final int DEFAULT_AGENT_CHANNEL_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_METADATA_CHANNEL_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_STAT_CHANNEL_EXECUTOR_QUEUE_SIZE = 1000;
    private static final int DEFAULT_SPAN_CHANNEL_EXECUTOR_QUEUE_SIZE = 1000;

    private static final int DEFAULT_DISCARD_LOG_RATE_LIMIT = 100;
    private static final long DEFAULT_DISCARD_MAX_PENDING_THRESHOLD = 1024;
    private static final long DEFAULT_DISCARD_COUNT_FOR_RECONNECT = 1000;
    private static final long DEFAULT_NOT_READY_TIMEOUT_MILLIS = 5 * 60 * 1000;

    private static final int DEFAULT_METADATA_RETRY_MAX_COUNT = 3;
    private static final int DEFAULT_METADATA_RETRY_DELAY_MILLIS = 1000;
    public static final boolean DEFAULT_NETTY_SYSTEM_PROPERTY_TRY_REFLECTIVE_SET_ACCESSIBLE = true;

    private ClientOption agentClientOption = new ClientOption();
    private ClientOption metadataClientOption = new ClientOption();
    private ClientOption statClientOption = new ClientOption();
    private ClientOption spanClientOption = new ClientOption();

    @Value("${profiler.transport.grpc.agent.collector.ip}")
    private String agentCollectorIp = DEFAULT_IP;
    @Value("${profiler.transport.grpc.agent.collector.port}")
    private int agentCollectorPort = DEFAULT_AGENT_COLLECTOR_PORT;
    @Value("${profiler.transport.grpc.agent.sender.request.timeout.millis}")
    private long agentRequestTimeout = DEFAULT_CLIENT_REQUEST_TIMEOUT;
    @Value("${profiler.transport.grpc.agent.sender.executor.queue.size}")
    private int agentSenderExecutorQueueSize = DEFAULT_AGENT_SENDER_EXECUTOR_QUEUE_SIZE;
    @Value("${profiler.transport.grpc.agent.sender.channel.executor.queue.size}")
    private int agentChannelExecutorQueueSize = DEFAULT_AGENT_CHANNEL_EXECUTOR_QUEUE_SIZE;


    // Metadata
    @Value("${profiler.transport.grpc.metadata.collector.ip}")
    private String metadataCollectorIp = DEFAULT_IP;
    @Value("${profiler.transport.grpc.metadata.collector.port}")
    private int metadataCollectorPort = DEFAULT_AGENT_COLLECTOR_PORT;
    @Value("${profiler.transport.grpc.metadata.sender.request.timeout.millis}")
    private long metadataRequestTimeout = DEFAULT_CLIENT_REQUEST_TIMEOUT;
    @Value("${profiler.transport.grpc.metadata.sender.executor.queue.size}")
    private int metadataSenderExecutorQueueSize = DEFAULT_METADATA_SENDER_EXECUTOR_QUEUE_SIZE;
    @Value("${profiler.transport.grpc.metadata.sender.channel.executor.queue.size}")
    private int metadataChannelExecutorQueueSize = DEFAULT_METADATA_CHANNEL_EXECUTOR_QUEUE_SIZE;
    @Value("${profiler.transport.grpc.metadata.sender.retry.max.count}")
    private int metadataRetryMaxCount = DEFAULT_METADATA_RETRY_MAX_COUNT;
    @Value("${profiler.transport.grpc.metadata.sender.retry.delay.millis}")
    private int metadataRetryDelayMillis = DEFAULT_METADATA_RETRY_DELAY_MILLIS;


    @Value("${profiler.transport.grpc.stat.collector.ip}")
    private String statCollectorIp = DEFAULT_IP;
    @Value("${profiler.transport.grpc.stat.collector.port}")
    private int statCollectorPort = DEFAULT_STAT_COLLECTOR_PORT;
    @Value("${profiler.transport.grpc.stat.sender.request.timeout.millis}")
    private long statRequestTimeout = DEFAULT_CLIENT_REQUEST_TIMEOUT;
    @Value("${profiler.transport.grpc.stat.sender.executor.queue.size}")
    private int statSenderExecutorQueueSize = DEFAULT_STAT_SENDER_EXECUTOR_QUEUE_SIZE;
    @Value("${profiler.transport.grpc.stat.sender.channel.executor.queue.size}")
    private int statChannelExecutorQueueSize = DEFAULT_STAT_CHANNEL_EXECUTOR_QUEUE_SIZE;

    @Value("${profiler.transport.grpc.span.collector.ip}")
    private String spanCollectorIp = DEFAULT_IP;
    @Value("${profiler.transport.grpc.span.collector.port}")
    private int spanCollectorPort = DEFAULT_SPAN_COLLECTOR_PORT;
    @Value("${profiler.transport.grpc.span.sender.request.timeout.millis}")
    private long spanRequestTimeout = DEFAULT_CLIENT_REQUEST_TIMEOUT;
    @Value("${profiler.transport.grpc.span.sender.executor.queue.size}")
    private int spanSenderExecutorQueueSize = DEFAULT_SPAN_SENDER_EXECUTOR_QUEUE_SIZE;
    @Value("${profiler.transport.grpc.span.sender.channel.executor.queue.size}")
    private int spanChannelExecutorQueueSize = DEFAULT_SPAN_CHANNEL_EXECUTOR_QUEUE_SIZE;

    @Value("${profiler.transport.grpc.span.sender.discardpolicy.logger.discard.ratelimit}")
    private int spanDiscardLogRateLimit = DEFAULT_DISCARD_LOG_RATE_LIMIT;
    @Value("${profiler.transport.grpc.span.sender.discardpolicy.maxpendingthreshold}")
    private long spanDiscardMaxPendingThreshold = DEFAULT_DISCARD_MAX_PENDING_THRESHOLD;
    @Value("${profiler.transport.grpc.span.sender.discardpolicy.discard-count-for-reconnect}")
    private long spanDiscardCountForReconnect = DEFAULT_DISCARD_COUNT_FOR_RECONNECT;
    @Value("${profiler.transport.grpc.span.sender.discardpolicy.not-ready-timeout-millis}")
    private long spanNotReadyTimeoutMillis = DEFAULT_NOT_READY_TIMEOUT_MILLIS;

    @Value("${" + KEY_PROFILER_CONFIG_NETTY_TRY_REFLECTION_SET_ACCESSIBLE + "}")
    private boolean nettySystemPropertyTryReflectiveSetAccessible = DEFAULT_NETTY_SYSTEM_PROPERTY_TRY_REFLECTIVE_SET_ACCESSIBLE;


    public void read(Properties properties) {
        ValueAnnotationProcessor reader = new ValueAnnotationProcessor();
        reader.process(this, properties);


        this.agentClientOption = readAgentClientOption(properties);

        // Metadata
        this.metadataClientOption = readMetadataClientOption(properties);

        // Stat
        this.statClientOption = readStatClientOption(properties);

        // Span
        this.spanClientOption = readSpanClientOption(properties);
    }

    private ClientOption readAgentClientOption(final Properties properties) {
        return readClientOption(properties, "profiler.transport.grpc.agent.sender.");
    }

    private ClientOption readMetadataClientOption(final Properties properties) {
        return readClientOption(properties, "profiler.transport.grpc.metadata.sender.");
    }

    private ClientOption readStatClientOption(final Properties properties) {
        return readClientOption(properties, "profiler.transport.grpc.stat.sender.");
    }

    private ClientOption readSpanClientOption(final Properties properties) {
        return readClientOption(properties, "profiler.transport.grpc.span.sender.");
    }

    private ClientOption readClientOption(final Properties properties, final String transportName) {
        final ClientOption clientOption = new ClientOption();

        ValueAnnotationProcessor reader = new ValueAnnotationProcessor();
        reader.process(clientOption, new PropertyPlaceholderHelper.PlaceholderResolver() {
            @Override
            public String resolvePlaceholder(String placeholderName) {
                String prefix = transportName + placeholderName;
                return properties.getProperty(prefix);
            }
        });
        return clientOption;
    }

    public String getAgentCollectorIp() {
        return agentCollectorIp;
    }

    public int getAgentCollectorPort() {
        return agentCollectorPort;
    }

    public String getMetadataCollectorIp() {
        return metadataCollectorIp;
    }

    public int getMetadataCollectorPort() {
        return metadataCollectorPort;
    }

    public String getStatCollectorIp() {
        return statCollectorIp;
    }

    public int getStatCollectorPort() {
        return statCollectorPort;
    }

    public String getSpanCollectorIp() {
        return spanCollectorIp;
    }

    public int getSpanCollectorPort() {
        return spanCollectorPort;
    }

    public int getAgentSenderExecutorQueueSize() {
        return agentSenderExecutorQueueSize;
    }

    public int getMetadataSenderExecutorQueueSize() {
        return metadataSenderExecutorQueueSize;
    }

    public int getSpanSenderExecutorQueueSize() {
        return spanSenderExecutorQueueSize;
    }

    public int getStatSenderExecutorQueueSize() {
        return statSenderExecutorQueueSize;
    }

    public int getSpanDiscardLogRateLimit() {
        return spanDiscardLogRateLimit;
    }

    public long getSpanDiscardMaxPendingThreshold() {
        return spanDiscardMaxPendingThreshold;
    }

    public long getSpanDiscardCountForReconnect() {
        return spanDiscardCountForReconnect;
    }

    public long getSpanNotReadyTimeoutMillis() {
        return spanNotReadyTimeoutMillis;
    }

    public long getAgentRequestTimeout() {
        return agentRequestTimeout;
    }

    public long getMetadataRequestTimeout() {
        return metadataRequestTimeout;
    }

    public long getStatRequestTimeout() {
        return statRequestTimeout;
    }

    public long getSpanRequestTimeout() {
        return spanRequestTimeout;
    }

    public ClientOption getAgentClientOption() {
        return agentClientOption;
    }

    public ClientOption getMetadataClientOption() {
        return metadataClientOption;
    }

    public ClientOption getStatClientOption() {
        return statClientOption;
    }

    public ClientOption getSpanClientOption() {
        return spanClientOption;
    }

    public int getAgentChannelExecutorQueueSize() {
        return agentChannelExecutorQueueSize;
    }

    public int getMetadataChannelExecutorQueueSize() {
        return metadataChannelExecutorQueueSize;
    }

    public int getStatChannelExecutorQueueSize() {
        return statChannelExecutorQueueSize;
    }

    public int getSpanChannelExecutorQueueSize() {
        return spanChannelExecutorQueueSize;
    }

    public int getMetadataRetryMaxCount() {
        return metadataRetryMaxCount;
    }

    public int getMetadataRetryDelayMillis() {
        return metadataRetryDelayMillis;
    }

    public boolean isNettySystemPropertyTryReflectiveSetAccessible() {
        return nettySystemPropertyTryReflectiveSetAccessible;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GrpcTransportConfig{");
        sb.append("agentCollectorIp='").append(agentCollectorIp).append('\'');
        sb.append(", agentCollectorPort=").append(agentCollectorPort);
        sb.append(", metadataCollectorIp='").append(metadataCollectorIp).append('\'');
        sb.append(", metadataCollectorPort=").append(metadataCollectorPort);
        sb.append(", statCollectorIp='").append(statCollectorIp).append('\'');
        sb.append(", statCollectorPort=").append(statCollectorPort);
        sb.append(", spanCollectorIp='").append(spanCollectorIp).append('\'');
        sb.append(", spanCollectorPort=").append(spanCollectorPort);
        sb.append(", agentClientOption=").append(agentClientOption);
        sb.append(", metadataClientOption=").append(metadataClientOption);
        sb.append(", statClientOption=").append(statClientOption);
        sb.append(", spanClientOption=").append(spanClientOption);
        sb.append(", agentSenderExecutorQueueSize=").append(agentSenderExecutorQueueSize);
        sb.append(", metadataSenderExecutorQueueSize=").append(metadataSenderExecutorQueueSize);
        sb.append(", spanSenderExecutorQueueSize=").append(spanSenderExecutorQueueSize);
        sb.append(", statSenderExecutorQueueSize=").append(statSenderExecutorQueueSize);
        sb.append(", agentChannelExecutorQueueSize=").append(agentChannelExecutorQueueSize);
        sb.append(", metadataChannelExecutorQueueSize=").append(metadataChannelExecutorQueueSize);
        sb.append(", statChannelExecutorQueueSize=").append(statChannelExecutorQueueSize);
        sb.append(", spanChannelExecutorQueueSize=").append(spanChannelExecutorQueueSize);
        sb.append(", agentRequestTimeout=").append(agentRequestTimeout);
        sb.append(", metadataRequestTimeout=").append(metadataRequestTimeout);
        sb.append(", spanRequestTimeout=").append(spanRequestTimeout);
        sb.append(", statRequestTimeout=").append(statRequestTimeout);
        sb.append(", metadataRetryMaxCount=").append(metadataRetryMaxCount);
        sb.append(", metadataRetryDelayMillis=").append(metadataRetryDelayMillis);
        sb.append(", nettySystemPropertyTryReflectiveSetAccessible=").append(nettySystemPropertyTryReflectiveSetAccessible);
        sb.append(", spanDiscardLogRateLimit=").append(spanDiscardLogRateLimit);
        sb.append(", spanDiscardMaxPendingThreshold=").append(spanDiscardMaxPendingThreshold);
        sb.append('}');
        return sb.toString();
    }
}