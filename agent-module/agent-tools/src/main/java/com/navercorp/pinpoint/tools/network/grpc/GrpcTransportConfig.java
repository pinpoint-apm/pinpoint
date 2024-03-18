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


import com.navercorp.pinpoint.tools.utils.PropertyResolver;

import java.util.Properties;

import static com.navercorp.pinpoint.tools.utils.NumberUtils.parseInt;

/**
 * @author Roy Kim
 */
public class GrpcTransportConfig {

    private static final String DEFAULT_IP = "127.0.0.1";
    private static final int DEFAULT_AGENT_COLLECTOR_PORT = 9991;
    private static final int DEFAULT_STAT_COLLECTOR_PORT = 9992;
    private static final int DEFAULT_SPAN_COLLECTOR_PORT = 9993;

    private final PropertyResolver resolver;


    public GrpcTransportConfig(Properties resolver) {
        this.resolver = new PropertyResolver(resolver);
    }

    public String getAgentCollectorIp() {
        return resolve("profiler.transport.grpc.agent.collector.ip", DEFAULT_IP);
    }

    private String resolve(String key) {
        return resolver.resolve(key, null);
    }

    private String resolve(String key, String defaultValue) {
        return resolver.resolve(key, defaultValue);
    }

    public int getAgentCollectorPort() {
        return parseInt(resolve("profiler.transport.grpc.agent.collector.port"), DEFAULT_AGENT_COLLECTOR_PORT);
    }

    public String getMetadataCollectorIp() {
        return resolve("profiler.transport.grpc.metadata.collector.ip", DEFAULT_IP);
    }

    public int getMetadataCollectorPort() {
        return parseInt(resolve("profiler.transport.grpc.metadata.collector.port"), DEFAULT_AGENT_COLLECTOR_PORT);
    }

    public String getStatCollectorIp() {
        return resolve("profiler.transport.grpc.stat.collector.ip", DEFAULT_IP);
    }

    public int getStatCollectorPort() {
        return parseInt(resolve("profiler.transport.grpc.stat.collector.port"), DEFAULT_STAT_COLLECTOR_PORT);
    }

    public String getSpanCollectorIp() {
        return resolve("profiler.transport.grpc.span.collector.ip", DEFAULT_IP);
    }

    public int getSpanCollectorPort() {
        return parseInt(resolve("profiler.transport.grpc.span.collector.port"), DEFAULT_SPAN_COLLECTOR_PORT);
    }

    @Override
    public String toString() {
        return resolver.toString();
    }
}