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
package com.navercorp.pinpoint.plugin.redis.lettuce;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class LettucePluginConfig {
    private boolean enable = true;

    public LettucePluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.redis.lettuce.enable", true);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LettucePluginConfig{");
        sb.append("enable=").append(enable);
        sb.append('}');
        return sb.toString();
    }
}