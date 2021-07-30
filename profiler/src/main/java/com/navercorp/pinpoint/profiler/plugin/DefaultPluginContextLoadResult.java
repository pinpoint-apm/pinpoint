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

package com.navercorp.pinpoint.profiler.plugin;

import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.uri.UriExtractorProvider;
import com.navercorp.pinpoint.common.trace.ServiceType;
import java.util.Objects;
import com.navercorp.pinpoint.loader.plugins.profiler.ProfilerPluginLoader;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;

import java.lang.instrument.ClassFileTransformer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultPluginContextLoadResult implements PluginContextLoadResult {

    private final PluginsSetupResult pluginsSetupResult;

    public DefaultPluginContextLoadResult(ProfilerPluginContextLoader profilerPluginContextLoader, ClassLoader pluginClassLoader) {
        Objects.requireNonNull(profilerPluginContextLoader, "profilerPluginConfigurer");
        Objects.requireNonNull(pluginClassLoader, "pluginClassLoader");
        ProfilerPluginLoader profilerPluginLoader = new ProfilerPluginLoader();
        List<ProfilerPlugin> profilerPlugins = profilerPluginLoader.load(pluginClassLoader);
        this.pluginsSetupResult = profilerPluginContextLoader.load(profilerPlugins);
    }

    @Override
    public List<ClassFileTransformer> getClassFileTransformer() {
        // TODO Need plugin context level grouping
        final List<ClassFileTransformer> transformerList = new ArrayList<>();
        for (PluginSetupResult pluginContext : pluginsSetupResult.getPluginSetupResults()) {
            List<ClassFileTransformer> classTransformerList = pluginContext.getClassTransformerList();
            transformerList.addAll(classTransformerList);
        }
        return transformerList;
    }

    @Override
    public ServiceType getApplicationType() {
        return pluginsSetupResult.getApplicationType();
    }

    @Override
    public List<JdbcUrlParserV2> getJdbcUrlParserList() {
        final List<JdbcUrlParserV2> result = new ArrayList<>();

        for (PluginSetupResult context : pluginsSetupResult.getPluginSetupResults()) {
            List<JdbcUrlParserV2> jdbcUrlParserList = context.getJdbcUrlParserList();
            result.addAll(jdbcUrlParserList);
        }

        return result;
    }

    @Override
    public List<UriExtractorProvider> getUriExtractorProviderList() {
        final List<UriExtractorProvider> result = new ArrayList<>();

        for (PluginSetupResult context : pluginsSetupResult.getPluginSetupResults()) {
            List<UriExtractorProvider> uriExtractorProviderList= context.getUriExtractorProviderList();
            result.addAll(uriExtractorProviderList);
        }

        return result;
    }


}
