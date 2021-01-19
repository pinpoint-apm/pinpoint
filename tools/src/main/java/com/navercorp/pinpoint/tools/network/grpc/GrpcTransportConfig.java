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

package com.navercorp.pinpoint.tools.network.grpc;

import com.navercorp.pinpoint.bootstrap.config.Value;
import com.navercorp.pinpoint.bootstrap.config.util.ValueAnnotationProcessor;

import java.util.Properties;

/**
 * @author Roy Kim
 */
public class GrpcTransportConfig {

    private static final String DEFAULT_IP = "127.0.0.1";
    private static final int DEFAULT_AGENT_COLLECTOR_PORT = 9991;
    private static final int DEFAULT_STAT_COLLECTOR_PORT = 9992;
    private static final int DEFAULT_SPAN_COLLECTOR_PORT = 9993;

    @Value("${profiler.transport.grpc.agent.collector.ip}")
    private String agentCollectorIp = DEFAULT_IP;
    @Value("${profiler.transport.grpc.agent.collector.port}")
    private int agentCollectorPort = DEFAULT_AGENT_COLLECTOR_PORT;

    @Value("${profiler.transport.grpc.metadata.collector.ip}")
    private String metadataCollectorIp = DEFAULT_IP;
    @Value("${profiler.transport.grpc.metadata.collector.port}")
    private int metadataCollectorPort = DEFAULT_AGENT_COLLECTOR_PORT;

    @Value("${profiler.transport.grpc.stat.collector.ip}")
    private String statCollectorIp = DEFAULT_IP;
    @Value("${profiler.transport.grpc.stat.collector.port}")
    private int statCollectorPort = DEFAULT_STAT_COLLECTOR_PORT;

    @Value("${profiler.transport.grpc.span.collector.ip}")
    private String spanCollectorIp = DEFAULT_IP;
    @Value("${profiler.transport.grpc.span.collector.port}")
    private int spanCollectorPort = DEFAULT_SPAN_COLLECTOR_PORT;

    public void read(Properties properties) {
        final ValueAnnotationProcessor reader = new ValueAnnotationProcessor();
        reader.process(this, properties);
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