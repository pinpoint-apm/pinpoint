/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.plugin.config;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.Value;
import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Collections;
import java.util.List;

public class DefaultPluginLoadingConfig implements PluginLoadingConfig {
    // ArtifactIdUtils.ARTIFACT_SEPARATOR
    private static final String ARTIFACT_SEPARATOR = ";";

    private List<String> pluginLoadOrder = Collections.emptyList();
    private List<String> disabledPlugins = Collections.emptyList();

    private List<String> importPluginIds = Collections.emptyList();

    public DefaultPluginLoadingConfig() {
    }

    @Override
    public List<String> getPluginLoadOrder() {
        return pluginLoadOrder;
    }

    @Value("${profiler.plugin.load.order}")
    public void setPluginLoadOrder(String pluginLoadOrder) {
        this.pluginLoadOrder = StringUtils.tokenizeToStringList(pluginLoadOrder, ",");
    }

    @Override
    public List<String> getDisabledPlugins() {
        return disabledPlugins;
    }

    @Value("${profiler.plugin.disable}")
    public void setDisabledPlugins(String disabledPlugins) {
        this.disabledPlugins = StringUtils.tokenizeToStringList(disabledPlugins, ",");
    }

    @Override
    public List<String> getImportPluginIds() {
        return importPluginIds;
    }

    @Value("${" + DefaultProfilerConfig.IMPORT_PLUGIN + "}")
    public void setImportPluginIds(String importPluginIds) {
        this.importPluginIds = StringUtils.tokenizeToStringList(importPluginIds, ARTIFACT_SEPARATOR);
    }

    @Override
    public String toString() {
        return "DefaultPluginLoadingConfig{" +
                "pluginLoadOrder=" + pluginLoadOrder +
                ", disabledPlugins=" + disabledPlugins +
                ", importPluginIds=" + importPluginIds +
                '}';
    }
}
