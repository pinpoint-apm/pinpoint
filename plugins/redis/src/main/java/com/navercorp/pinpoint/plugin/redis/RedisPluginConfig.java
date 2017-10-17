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
 * @author jaehong.kim
 */
public class RedisPluginConfig {
    private boolean enable = true;
    private boolean pipeline = true;
    private boolean io = true;

    public RedisPluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.redis", true);
        if (this.enable) {
            this.enable = src.readBoolean("profiler.redis.enable", true);
        }
        pipeline = src.readBoolean("profiler.redis.pipeline", true);
        io = src.readBoolean("profiler.redis.io", true);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isPipeline() {
        return pipeline;
    }

    public boolean isIo() {
        return io;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("enable=").append(enable);
        sb.append(", pipeline=").append(pipeline);
        sb.append(", io=").append(io);
        sb.append('}');
        return sb.toString();
    }
}