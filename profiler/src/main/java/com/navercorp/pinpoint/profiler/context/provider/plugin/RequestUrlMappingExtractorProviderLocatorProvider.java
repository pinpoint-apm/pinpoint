/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider.plugin;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractorProvider;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractorProviderLocator;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.RequestUrlMappingExtractorProviderRegistry;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;

import com.google.inject.Inject;
import com.google.inject.Provider;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class RequestUrlMappingExtractorProviderLocatorProvider implements Provider<RequestUrlMappingExtractorProviderLocator> {

    private final Provider<PluginContextLoadResult> pluginContextLoadResultProvider;

    @Inject
    public RequestUrlMappingExtractorProviderLocatorProvider(Provider<PluginContextLoadResult> pluginContextLoadResultProvider) {
        this.pluginContextLoadResultProvider = Assert.requireNonNull(pluginContextLoadResultProvider, "pluginContextLoadResultProvider");
    }

    @Override
    public RequestUrlMappingExtractorProviderLocator get() {
        PluginContextLoadResult pluginContextLoadResult = this.pluginContextLoadResultProvider.get();
        List<RequestUrlMappingExtractorProvider> requestUrlMappingExtractorProviderList = pluginContextLoadResult.getRequestUrlMappingExtractorProviderList();
        return new RequestUrlMappingExtractorProviderRegistry(requestUrlMappingExtractorProviderList);
    }

}

