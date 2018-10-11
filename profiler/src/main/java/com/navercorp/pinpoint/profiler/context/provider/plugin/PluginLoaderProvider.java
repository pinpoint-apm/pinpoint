/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.plugin;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.common.plugin.PluginJar;
import com.navercorp.pinpoint.common.plugin.PluginLoader;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;

import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginLoaderProvider implements Provider<PluginLoader> {

    private final ClassLoader parentClassLoader;
    private final PluginLoader pluginLoader;

    @Inject
    public PluginLoaderProvider(@PluginJars List<PluginJar> pluginJars) {
        // TODO configuration support
        this.parentClassLoader = Object.class.getClassLoader();
        this.pluginLoader = new JarPluginLoader(pluginJars, parentClassLoader);
    }

    @Override
    public PluginLoader get() {
        return pluginLoader;
    }
}
