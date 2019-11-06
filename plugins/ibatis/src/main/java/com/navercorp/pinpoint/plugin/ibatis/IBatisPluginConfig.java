/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.plugin.ibatis;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Woonduk Kang(emeroad)
 */
public class IBatisPluginConfig {
    private final boolean iBatisEnabled;

    public IBatisPluginConfig(ProfilerConfig profilerConfig) {
        this.iBatisEnabled = profilerConfig.readBoolean("profiler.orm.ibatis", true);
    }

    public boolean isIBatisEnabled() {
        return this.iBatisEnabled;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IBatisPluginConfig{");
        sb.append("iBatisEnabled=").append(iBatisEnabled);
        sb.append('}');
        return sb.toString();
    }
}
