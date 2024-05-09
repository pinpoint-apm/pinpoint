/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.resttemplate6;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author Taejin Koo
 */
public class RestTemplateConfig {

    static final String PLUGIN_ENABLE = "profiler.resttemplate6";

    private final boolean pluginEnable;
    private final boolean versionForcedMatch;

    public RestTemplateConfig(ProfilerConfig config) {
        pluginEnable = config.readBoolean(PLUGIN_ENABLE, false);
        this.versionForcedMatch = config.readBoolean("profiler.resttemplate6.version.forced.match", false);
    }

    public boolean isPluginEnable() {
        return pluginEnable;
    }

    public boolean isVersionForcedMatch() {
        return versionForcedMatch;
    }

    @Override
    public String toString() {
        return "RestTemplateConfig{" +
                "pluginEnable=" + pluginEnable +
                ", versionForcedMatch=" + versionForcedMatch +
                '}';
    }
}
