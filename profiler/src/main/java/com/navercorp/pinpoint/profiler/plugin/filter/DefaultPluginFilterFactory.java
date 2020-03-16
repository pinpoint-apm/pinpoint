/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.plugin.filter;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPluginFilterFactory implements PluginFilterFactory {

    private final List<String> disabledPlugins;

    public DefaultPluginFilterFactory(ProfilerConfig profilerConfig) {
        Assert.requireNonNull(profilerConfig, "profilerConfig");
        this.disabledPlugins = profilerConfig.getDisabledPlugins();
    }

    @Override
    public PluginFilter newPluginFilter() {
        if (CollectionUtils.isEmpty(disabledPlugins)) {
            return new JavaVersionFilter();
        }

        PluginFilter javaVersionFilter = new JavaVersionFilter();
        PluginFilter disabledPluginFilter = new DisabledPluginFilter(disabledPlugins);
        return new PluginFilters(disabledPluginFilter, javaVersionFilter);
    }
}
