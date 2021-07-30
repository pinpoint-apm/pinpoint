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

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractorProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.plugins.profiler.ProfilerPluginLoader;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.PluginSetupResult;
import com.navercorp.pinpoint.profiler.plugin.PluginsSetupResult;
import com.navercorp.pinpoint.profiler.plugin.ProfilerPluginContextLoader;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockPluginContextLoadResult implements PluginContextLoadResult {

    private final ProfilerPluginContextLoader profilerPluginContextLoader;
    private final ClassLoader pluginClassLoader;
    private PluginsSetupResult lazy;

    public MockPluginContextLoadResult(ProfilerPluginContextLoader profilerPluginContextLoader, ClassLoader pluginClassLoader) {
        this.profilerPluginContextLoader = Objects.requireNonNull(profilerPluginContextLoader, "profilerPluginConfigurer");
        this.pluginClassLoader = Objects.requireNonNull(pluginClassLoader, "pluginClassLoader");
    }

    private PluginsSetupResult getPluginsSetupResult() {
        if (lazy == null) {
            lazy = load();
        }
        return lazy;
    }

    private PluginsSetupResult load() {
        ProfilerPluginLoader profilerPluginLoader = new ProfilerPluginLoader();
        List<ProfilerPlugin> profilerPlugins = profilerPluginLoader.load(pluginClassLoader);
        return profilerPluginContextLoader.load(profilerPlugins);
    }


    @Override
    public List<ClassFileTransformer> getClassFileTransformer() {
        List<ClassFileTransformer> classFileTransformerList = new ArrayList<>();
        PluginsSetupResult pluginsSetupResult = getPluginsSetupResult();
        for (PluginSetupResult pluginContext : pluginsSetupResult.getPluginSetupResults()) {
            List<ClassFileTransformer> classFileTransformer = pluginContext.getClassTransformerList();
            classFileTransformerList.addAll(classFileTransformer);
        }

        return classFileTransformerList;
    }

    @Override
    public ServiceType getApplicationType() {
        PluginsSetupResult pluginsSetupResult = getPluginsSetupResult();
        return pluginsSetupResult.getApplicationType();
    }

    @Override
    public List<JdbcUrlParserV2> getJdbcUrlParserList() {
        final List<JdbcUrlParserV2> result = new ArrayList<>();
        PluginsSetupResult pluginsSetupResult = getPluginsSetupResult();
        for (PluginSetupResult context : pluginsSetupResult.getPluginSetupResults()) {
            List<JdbcUrlParserV2> jdbcUrlParserList = context.getJdbcUrlParserList();
            result.addAll(jdbcUrlParserList);
        }
        return result;
    }

    @Override
    public List<UriExtractorProvider> getUriExtractorProviderList() {
        final List<UriExtractorProvider> result = new ArrayList<>();
        PluginsSetupResult pluginsSetupResult = getPluginsSetupResult();
        for (PluginSetupResult context : pluginsSetupResult.getPluginSetupResults()) {
            List<UriExtractorProvider> uriExtractorProviderList = context.getUriExtractorProviderList();
            result.addAll(uriExtractorProviderList);
        }
        return result;
    }
}
