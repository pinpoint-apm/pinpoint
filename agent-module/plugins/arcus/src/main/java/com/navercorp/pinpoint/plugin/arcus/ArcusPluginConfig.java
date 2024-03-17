/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.arcus;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class ArcusPluginConfig {
    private final boolean arcus;
    private final boolean arcusKeyTrace;
    private final boolean arcusAsync;
    private final boolean memcached;
    private final boolean memcachedKeyTrace;
    private final boolean memcachedAsync;

    public ArcusPluginConfig(ProfilerConfig src) {
        this.arcus = src.readBoolean("profiler.arcus", true);
        this.arcusKeyTrace = src.readBoolean("profiler.arcus.keytrace", false);
        this.arcusAsync = src.readBoolean("profiler.arcus.async", true);
        this.memcached = src.readBoolean("profiler.memcached", true);
        this.memcachedKeyTrace = src.readBoolean("profiler.memcached.keytrace", false);
        this.memcachedAsync = src.readBoolean("profiler.memcached.async", true);
    }

    public boolean isArcus() {
        return arcus;
    }

    public boolean isArcusKeyTrace() {
        return arcusKeyTrace;
    }

    public boolean isMemcached() {
        return memcached;
    }

    public boolean isMemcachedKeyTrace() {
        return memcachedKeyTrace;
    }

    public boolean isArcusAsync() {
        return arcusAsync;
    }

    public boolean isMemcachedAsync() {
        return memcachedAsync;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ArcusPluginConfig{");
        sb.append("arcus=").append(arcus);
        sb.append(", arcusKeyTrace=").append(arcusKeyTrace);
        sb.append(", arcusAsync=").append(arcusAsync);
        sb.append(", memcached=").append(memcached);
        sb.append(", memcachedKeyTrace=").append(memcachedKeyTrace);
        sb.append(", memcachedAsync=").append(memcachedAsync);
        sb.append('}');
        return sb.toString();
    }
}
