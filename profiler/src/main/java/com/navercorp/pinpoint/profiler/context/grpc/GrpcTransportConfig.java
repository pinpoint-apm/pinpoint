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

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcTransportConfig {

    private static final String DEFAULT_IP = "127.0.0.1";

    private String collectorAgentServerIp = DEFAULT_IP;
    private int collectorAgentServerPort = 9997;

    private String collectorSpanServerIp = DEFAULT_IP;
    private int collectorSpanServerPort = 9998;

    private String collectorStatServerIp = DEFAULT_IP;
    private int collectorStatServerPort = 9999;

    public void read(ProfilerConfig profilerConfig) {
        final ProfilerConfig.ValueResolver placeHolderResolver = new DefaultProfilerConfig.PlaceHolderResolver();
        // Agent
        this.collectorAgentServerIp = profilerConfig.readString("profiler.transport.grpc.collector.agent.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorAgentServerPort = profilerConfig.readInt("profiler.transport.grpc.collector.agent.port", 9997);
        // Span
        this.collectorSpanServerIp = profilerConfig.readString("profiler.transport.grpc.collector.span.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorSpanServerPort = profilerConfig.readInt("profiler.transport.grpc.collector.span.port", 9998);
        // Stat
        this.collectorStatServerIp = profilerConfig.readString("profiler.transport.grpc.collector.stat.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorStatServerPort = profilerConfig.readInt("profiler.transport.grpc.collector.stat.port", 9999);
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GrpcTransportConfig{");
        sb.append("collectorAgentServerIp='").append(collectorAgentServerIp).append('\'');
        sb.append(", collectorAgentServerPort=").append(collectorAgentServerPort);
        sb.append(", collectorSpanServerIp='").append(collectorSpanServerIp).append('\'');
        sb.append(", collectorSpanServerPort=").append(collectorSpanServerPort);
        sb.append(", collectorStatServerIp='").append(collectorStatServerIp).append('\'');
        sb.append(", collectorStatServerPort=").append(collectorStatServerPort);
        sb.append('}');
        return sb.toString();
    }
}
