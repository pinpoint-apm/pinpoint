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
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.module.AgentServiceType;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.util.ApplicationServerTypeResolver;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ApplicationServerTypeResolverProvider implements Provider<ApplicationServerTypeResolver> {

    private final Provider<PluginContextLoadResult> pluginContextLoadResultProvider;
    private final ServiceType serviceType;
    private final ProfilerConfig profilerConfig;

    @Inject
    public ApplicationServerTypeResolverProvider(Provider<PluginContextLoadResult> pluginContextLoadResultProvider, @AgentServiceType ServiceType serviceType, ProfilerConfig profilerConfig) {
        if (pluginContextLoadResultProvider == null) {
            throw new NullPointerException("pluginContextLoadResultProvider must not be null");
        }
        if (serviceType == null) {
            throw new NullPointerException("serviceType must not be null");
        }
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        this.pluginContextLoadResultProvider = pluginContextLoadResultProvider;
        this.serviceType = serviceType;
        this.profilerConfig = profilerConfig;
    }

    @Override
    public ApplicationServerTypeResolver get() {
        PluginContextLoadResult pluginContextLoadResult = this.pluginContextLoadResultProvider.get();
        return new ApplicationServerTypeResolver(pluginContextLoadResult, serviceType, profilerConfig.getApplicationTypeDetectOrder());
    }
}
