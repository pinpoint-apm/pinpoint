/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.loader.plugins.profiler;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.loader.plugins.PinpointPluginLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * TODO Loading all plugins with a single class loader could cause class collisions.
 * Also, with current implementation, plugins can use dependencies by putting them in the plugin directory too.
 * But it can lead to dependency collision between plugins because they are loaded by a single class loader.
 * <p>
 * How can we prevent this?
 * A ClassLoader per plugin could do it but then we have to create "N of target class loader" x "N of plugin" class loaders.
 * It seems too much. For now, Just leave it as it is.
 *
 * @author Jongho Moon <jongho.moon@navercorp.com>
 * @author emeroad
 * @author HyunGil Jeong
 */
public class ProfilerPluginLoader implements PinpointPluginLoader<ProfilerPlugin> {

    @Override
    public List<ProfilerPlugin> load(ClassLoader classLoader) {
        List<ProfilerPlugin> profilerPlugins = new ArrayList<ProfilerPlugin>();
        ServiceLoader<ProfilerPlugin> serviceLoader = ServiceLoader.load(ProfilerPlugin.class, classLoader);
        for (ProfilerPlugin profilerPlugin : serviceLoader) {
            profilerPlugins.add(profilerPlugin);
        }
        return profilerPlugins;
    }
}
