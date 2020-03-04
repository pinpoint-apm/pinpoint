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

package com.navercorp.pinpoint.plugin.redis.redisson;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author jaehong.kim
 */
public class RedissonPluginConfig {

    private boolean enable;
    private boolean keyTrace;

    public RedissonPluginConfig(ProfilerConfig src) {
        this.enable = src.readBoolean("profiler.redis.redisson.enable", true);
        this.keyTrace = src.readBoolean("profiler.redis.redisson.keytrace", false);
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isKeyTrace() {
        return keyTrace;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RedissonPluginConfig{");
        sb.append("enable=").append(enable);
        sb.append(", keyTrace=").append(keyTrace);
        sb.append('}');
        return sb.toString();
    }
}
