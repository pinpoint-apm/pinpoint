/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.grpc;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class GrpcConfig {

    static final String CLIENT_ENABLE = "profiler.grpc.client.enable";

    static final String SERVER_ENABLE = "profiler.grpc.server.enable";
    static final String SERVER_STREAMING_ENABLE = "profiler.grpc.server.streaming.enable";
    static final String SERVER_STREAMING_ON_MESSAGE_ENABLE = "profiler.grpc.server.streaming.onmessage.enable";


    private final boolean clientEnable;

    private final boolean serverEnable;
    private final boolean serverStreamingEnable;
    private final boolean serverStreamingOnMessageEnable;

    public GrpcConfig(ProfilerConfig config) {
        this.clientEnable = config.readBoolean(CLIENT_ENABLE, false);

        this.serverEnable = config.readBoolean(SERVER_ENABLE, false);
        this.serverStreamingEnable = config.readBoolean(SERVER_STREAMING_ENABLE, false);
        this.serverStreamingOnMessageEnable = config.readBoolean(SERVER_STREAMING_ON_MESSAGE_ENABLE, false);
    }

    public boolean isClientEnable() {
        return clientEnable;
    }

    public boolean isServerEnable() {
        return serverEnable;
    }

    public boolean isServerStreamingEnable() {
        return serverStreamingEnable;
    }

    public boolean isServerStreamingOnMessageEnable() {
        return serverStreamingOnMessageEnable;
    }

}
