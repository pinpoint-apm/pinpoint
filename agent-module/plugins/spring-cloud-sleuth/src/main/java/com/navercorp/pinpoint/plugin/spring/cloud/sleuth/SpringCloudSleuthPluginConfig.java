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

package com.navercorp.pinpoint.plugin.spring.cloud.sleuth;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

import java.util.Objects;

public class SpringCloudSleuthPluginConfig {
    private final boolean enable;

    public SpringCloudSleuthPluginConfig(ProfilerConfig config) {
        Objects.requireNonNull(config, "config");

        // plugin
        this.enable = config.readBoolean("profiler.spring.cloud.sleuth.enable", true);
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        return "SpringCloudSleuthPluginConfig{" +
                "enable=" + enable +
                '}';
    }
}