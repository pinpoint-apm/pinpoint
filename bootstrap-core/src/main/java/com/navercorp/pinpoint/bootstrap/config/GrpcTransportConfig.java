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

package com.navercorp.pinpoint.bootstrap.config;

/**
 * @author Woonduk Kang(emeroad)
 */
public class GrpcTransportConfig {

    private static final String DEFAULT_IP = "127.0.0.1";

    private String collectorSpanServerIp = DEFAULT_IP;
    private int collectorSpanServerPort = 9997;


    public void read(DefaultProfilerConfig profilerConfig) {
        final DefaultProfilerConfig.ValueResolver placeHolderResolver = new DefaultProfilerConfig.PlaceHolderResolver();

        this.collectorSpanServerIp = profilerConfig.readString("profiler.transport.grpc.collector.span.ip", DEFAULT_IP, placeHolderResolver);
        this.collectorSpanServerPort = profilerConfig.readInt("profiler.transport.grpc.collector.span.port", 9997);

    }

//    @Override
    public String getCollectorSpanServerIp() {
        return collectorSpanServerIp;
    }

//    @Override
    public int getCollectorSpanServerPort() {
        return collectorSpanServerPort;
    }

    @Override
    public String toString() {
        return "GrpcTransportConfig{" +
                "collectorSpanServerIp='" + collectorSpanServerIp + '\'' +
                ", collectorSpanServerPort=" + collectorSpanServerPort +
                '}';
    }
}
