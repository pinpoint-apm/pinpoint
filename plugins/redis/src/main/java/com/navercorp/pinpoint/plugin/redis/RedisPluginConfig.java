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

    private boolean enabled = true;
    private boolean pipelineEnabled = true;

    public RedisPluginConfig(ProfilerConfig src) {
        enabled = src.readBoolean("profiler.redis", true);
        pipelineEnabled = src.readBoolean("profiler.redis.pipeline", true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPipelineEnabled() {
        return pipelineEnabled;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{enabled=");
        builder.append(enabled);
        builder.append(", pipelineEnabled=");
        builder.append(pipelineEnabled);
        builder.append("}");
        return builder.toString();
    }
}
