/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.module.PluginClassLoader;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.ProfilerPluginContextLoader;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockPluginContextLoadResultProvider implements Provider<PluginContextLoadResult> {

    private final ProfilerPluginContextLoader profilerPluginContextLoader;
    private final ClassLoader pluginClassLoader;

    @Inject
    public MockPluginContextLoadResultProvider(ProfilerPluginContextLoader profilerPluginContextLoader, @PluginClassLoader ClassLoader pluginClassLoader) {
        this.profilerPluginContextLoader = Objects.requireNonNull(profilerPluginContextLoader, "profilerPluginContextLoader");
        this.pluginClassLoader = Objects.requireNonNull(pluginClassLoader, "pluginLoader");
    }

    @Override
    public PluginContextLoadResult get() {
        return new MockPluginContextLoadResult(profilerPluginContextLoader, pluginClassLoader);
    }


}
