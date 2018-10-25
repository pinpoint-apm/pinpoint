/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.config;

import io.grpc.internal.GrpcUtil;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class GrpcSpanServerConfiguration {
    private String host;
    private int port;
    private int maxConcurrentCallsPerConnection;
    private int flowControlWindow;
    private int maxMessageSize;
    private int maxHeaderListSize;
    private long keepAliveTimeInNanos;
    private long keepAliveTimeoutInNanos;
    private long maxConnectionIdleInNanos;
    private long maxConnectionAgeInNanos;
    private long maxConnectionAgeGraceInNanos;
    private boolean permitKeepAliveWithoutCalls;
    private long permitKeepAliveTimeInNanos;

    public GrpcSpanServerConfiguration(final Properties properties) {



    }
}
