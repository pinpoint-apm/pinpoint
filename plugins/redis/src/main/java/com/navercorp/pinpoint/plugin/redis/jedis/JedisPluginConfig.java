/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.redis.jedis;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Arrays;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class JedisPluginConfig {
    private boolean enable = true;
    private boolean pipeline = true;
    private boolean io = true;

    public JedisPluginConfig(ProfilerConfig src) {
        this.enable = readBoolean(src, Arrays.asList("profiler.redis.jedis.enable", "profiler.redis.enable", "profiler.redis"), true);
        this.pipeline = readBoolean(src, Arrays.asList( "profiler.redis.jedis.pipeline", "profiler.redis.pipeline"), true);
        this.io = readBoolean(src, Arrays.asList("profiler.redis.jedis.io", "profiler.redis.io"), true);
    }

    private boolean readBoolean(final ProfilerConfig src, final List<String> nameList, final boolean defaultValue) {
        for (String name : nameList) {
            final String value = src.readString(name, null);
            if (value != null) {
                return src.readBoolean(name, defaultValue);
            }
        }
        return defaultValue;
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