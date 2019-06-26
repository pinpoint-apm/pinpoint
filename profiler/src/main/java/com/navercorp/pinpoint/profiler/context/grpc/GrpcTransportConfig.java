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

package com.navercorp.pinpoint.profiler.context.grpc;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.grpc.client.ClientOption;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcTransportConfig {

    private static final String DEFAULT_IP = "127.0.0.1";
    private static final long DEFAULT_CLIENT_REQUEST_TIMEOUT = 6000;

    private String collectorAgentServerIp = DEFAULT_IP;
    private int collectorAgentServerPort = 9991;

    private String collectorStatServerIp = DEFAULT_IP;
    private int collectorStatServerPort = 9992;

    private String collectorSpanServerIp = DEFAULT_IP;
    private int collectorSpanServerPort = 9993;

    private ClientOption agentClientOption = new ClientOption.Builder().build();
    private ClientOption statClientOption = new ClientOption.Builder().build();
    private ClientOption spanClientOption = new ClientOption.Builder().build();

    private long clientRequestTimeout = DEFAULT_CLIENT_REQUEST_TIMEOUT;
    private int spanSenderExecutorQueueSize = 1024;
    private int statSenderExecutorQueueSize = 1024;

    public void read(ProfilerConfig profilerConfig) {
        final ProfilerConfig.ValueResolver placeHolderResolver = new DefaultProfilerConfig.PlaceHolderResolver();
        // Agent Collector
        this.collectorAgentServerIp = profilerConfig.readString("profiler.transport.grpc.collector.agent.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorAgentServerPort = profilerConfig.readInt("profiler.transport.grpc.collector.agent.port", 9991);
        // Stat Collector
        this.collectorStatServerIp = profilerConfig.readString("profiler.transport.grpc.collector.stat.ip", DEFAULT_IP, placeHolderResolver);

        this.clientRequestTimeout = profilerConfig.readLong("profiler.transport.grpc.client.request.timeout", DEFAULT_CLIENT_REQUEST_TIMEOUT);
        this.collectorStatServerPort = profilerConfig.readInt("profiler.transport.grpc.collector.stat.port", 9992);
        // Span Collector
        this.collectorSpanServerIp = profilerConfig.readString("profiler.transport.grpc.collector.span.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorSpanServerPort = profilerConfig.readInt("profiler.transport.grpc.collector.span.port", 9993);

        this.statSenderExecutorQueueSize = profilerConfig.readInt("profiler.transport.grpc.stat.sender.executor.queue.size", 1024);
        this.spanSenderExecutorQueueSize = profilerConfig.readInt("profiler.transport.grpc.span.sender.executor.queue.size", 1024);

        // ClientOption
        this.agentClientOption = readAgentClientOption(profilerConfig);
        this.statClientOption = readStatClientOption(profilerConfig);
        this.spanClientOption = readSpanClientOption(profilerConfig);
    }

    private ClientOption readAgentClientOption(final ProfilerConfig profilerConfig) {
        return readClientOption(profilerConfig, "profiler.transport.grpc.agent");
    }

    private ClientOption readStatClientOption(final ProfilerConfig profilerConfig) {
        return readClientOption(profilerConfig, "profiler.transport.grpc.stat");
    }

    private ClientOption readSpanClientOption(final ProfilerConfig profilerConfig) {
        return readClientOption(profilerConfig, "profiler.transport.grpc.span");
    }

    private ClientOption readClientOption(final ProfilerConfig profilerConfig, final String transportName) {
        final ClientOption.Builder builder = new ClientOption.Builder();
        profilerConfig.readLong(transportName + ".keepalive.time", ClientOption.DEFAULT_KEEPALIVE_TIME);
        profilerConfig.readLong(transportName + ".keepalive.timeout", ClientOption.DEFAULT_KEEPALIVE_TIMEOUT);
        profilerConfig.readBoolean(transportName + ".keepalive.without-calls", ClientOption.DEFAULT_KEEPALIVE_WITHOUT_CALLS);
        profilerConfig.readLong(transportName + ".idle.timeout", ClientOption.IDLE_TIMEOUT_MILLIS_DISABLE);
        profilerConfig.readInt(transportName + ".headers.size.max", ClientOption.DEFAULT_MAX_HEADER_LIST_SIZE);
        profilerConfig.readInt(transportName + ".message.inbound.size.max", ClientOption.DEFAULT_MAX_MESSAGE_SIZE);
        profilerConfig.readInt(transportName + ".connect.timeout", ClientOption.DEFAULT_CONNECT_TIMEOUT);
        profilerConfig.readInt(transportName + ".write.buffer.highwatermark", ClientOption.DEFAULT_WRITE_BUFFER_HIGH_WATER_MARK);
        profilerConfig.readInt(transportName + ".write.buffer.lowwatermark", ClientOption.DEFAULT_WRITE_BUFFER_LOW_WATER_MARK);

        return builder.build();
    }

    public String getCollectorSpanServerIp() {
        return collectorSpanServerIp;
    }

    public int getCollectorSpanServerPort() {
        return collectorSpanServerPort;
    }

    public String getCollectorAgentServerIp() {
        return collectorAgentServerIp;
    }

    public int getCollectorAgentServerPort() {
        return collectorAgentServerPort;
    }

    public String getCollectorStatServerIp() {
        return collectorStatServerIp;
    }

    public int getCollectorStatServerPort() {
        return collectorStatServerPort;
    }

    public int getSpanSenderExecutorQueueSize() {
        return spanSenderExecutorQueueSize;
    }

    public int getStatSenderExecutorQueueSize() {
        return statSenderExecutorQueueSize;
    }

    public long getClientRequestTimeout() {
        return clientRequestTimeout;
    }

    public ClientOption getAgentClientOption() {
        return agentClientOption;
    }

    public ClientOption getStatClientOption() {
        return statClientOption;
    }

    public ClientOption getSpanClientOption() {
        return spanClientOption;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GrpcTransportConfig{");
        sb.append("collectorAgentServerIp='").append(collectorAgentServerIp).append('\'');
        sb.append(", collectorAgentServerPort=").append(collectorAgentServerPort);
        sb.append(", collectorStatServerIp='").append(collectorStatServerIp).append('\'');
        sb.append(", collectorStatServerPort=").append(collectorStatServerPort);
        sb.append(", collectorSpanServerIp='").append(collectorSpanServerIp).append('\'');
        sb.append(", collectorSpanServerPort=").append(collectorSpanServerPort);
        sb.append(", agentClientOption=").append(agentClientOption);
        sb.append(", statClientOption=").append(statClientOption);
        sb.append(", spanClientOption=").append(spanClientOption);
        sb.append(", clientRequestTimeout=").append(clientRequestTimeout);
        sb.append(", spanSenderExecutorQueueSize=").append(spanSenderExecutorQueueSize);
        sb.append(", statSenderExecutorQueueSize=").append(statSenderExecutorQueueSize);
        sb.append('}');
        return sb.toString();
    }
}
