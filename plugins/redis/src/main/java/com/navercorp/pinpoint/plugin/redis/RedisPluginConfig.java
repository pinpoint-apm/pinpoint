/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.redis;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * 
 * @author jaehong.kim
 *
 */
public class RedisPluginConfig {
    private boolean pipelineEnabled = true;
    private boolean io = true;

    public RedisPluginConfig(ProfilerConfig src) {
        pipelineEnabled = src.readBoolean("profiler.redis.pipeline", true);
        io = src.readBoolean("profiler.redis.io", true);
    }

    public boolean isPipelineEnabled() {
        return pipelineEnabled;
    }

    public boolean isIo() {
        return io;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RedisPluginConfig{pipelineEnabled=");
        builder.append(pipelineEnabled);
        builder.append(", io=");
        builder.append(io);
        builder.append("}");
        return builder.toString();
    }
}