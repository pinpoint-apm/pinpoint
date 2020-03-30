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

package com.navercorp.pinpoint.tools.network.grpcTransportConfig;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Roy Kim
 */
public class GrpcTransportConfig {

    private static final String DEFAULT_IP = "127.0.0.1";
    private static final int DEFAULT_AGENT_COLLECTOR_PORT = 9991;
    private static final int DEFAULT_STAT_COLLECTOR_PORT = 9992;
    private static final int DEFAULT_SPAN_COLLECTOR_PORT = 9993;

    private String agentCollectorIp = DEFAULT_IP;
    private int agentCollectorPort = DEFAULT_AGENT_COLLECTOR_PORT;

    private String metadataCollectorIp = DEFAULT_IP;
    private int metadataCollectorPort = DEFAULT_AGENT_COLLECTOR_PORT;

    private String statCollectorIp = DEFAULT_IP;
    private int statCollectorPort = DEFAULT_STAT_COLLECTOR_PORT;

    private String spanCollectorIp = DEFAULT_IP;
    private int spanCollectorPort = DEFAULT_SPAN_COLLECTOR_PORT;

    public void read(ProfilerConfig profilerConfig) {
        final ProfilerConfig.ValueResolver placeHolderResolver = new DefaultProfilerConfig.PlaceHolderResolver();
        // Agent
        this.agentCollectorIp = profilerConfig.readString("profiler.transport.grpc.agent.collector.ip", DEFAULT_IP, placeHolderResolver);
        this.agentCollectorPort = profilerConfig.readInt("profiler.transport.grpc.agent.collector.port", DEFAULT_AGENT_COLLECTOR_PORT);

        // Metadata
        this.metadataCollectorIp = profilerConfig.readString("profiler.transport.grpc.metadata.collector.ip", DEFAULT_IP, placeHolderResolver);
        this.metadataCollectorPort = profilerConfig.readInt("profiler.transport.grpc.metadata.collector.port", DEFAULT_AGENT_COLLECTOR_PORT);

        // Stat
        this.statCollectorIp = profilerConfig.readString("profiler.transport.grpc.stat.collector.ip", DEFAULT_IP, placeHolderResolver);
        this.statCollectorPort = profilerConfig.readInt("profiler.transport.grpc.stat.collector.port", DEFAULT_STAT_COLLECTOR_PORT);

        // Span
        this.spanCollectorIp = profilerConfig.readString("profiler.transport.grpc.span.collector.ip", DEFAULT_IP, placeHolderResolver);
        this.spanCollectorPort = profilerConfig.readInt("profiler.transport.grpc.span.collector.port", DEFAULT_SPAN_COLLECTOR_PORT);
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
        sb.append('}');
        return sb.toString();
    }
}