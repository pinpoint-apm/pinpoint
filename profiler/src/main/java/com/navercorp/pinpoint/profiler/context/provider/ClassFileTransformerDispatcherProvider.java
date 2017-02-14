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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.navercorp.pinpoint.profiler.ClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.DefaultClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.context.ApplicationContext;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;

import javax.inject.Provider;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ClassFileTransformerDispatcherProvider implements Provider<ClassFileTransformerDispatcher> {

    private final ApplicationContext applicationContext;
    private final PluginContextLoadResult pluginContextLoadResult;

    @Inject
    public ClassFileTransformerDispatcherProvider(ApplicationContext applicationContext, PluginContextLoadResult pluginContextLoadResult) {
        if (applicationContext == null) {
            throw new NullPointerException("applicationContext must not be null");
        }
        if (pluginContextLoadResult == null) {
            throw new NullPointerException("pluginContextLoadResult must not be null");
        }
        this.applicationContext = applicationContext;
        this.pluginContextLoadResult = pluginContextLoadResult;
    }

    @Override
    public ClassFileTransformerDispatcher get() {
        return new DefaultClassFileTransformerDispatcher(applicationContext, pluginContextLoadResult);
    }
}
