/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParser;
import com.navercorp.pinpoint.profiler.context.monitor.DatabaseInfoCache;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class DatabaseInfoCacheProvider implements Provider<DatabaseInfoCache> {

    private final Provider<PluginContextLoadResult> pluginContextLoadResultProvider;

    @Inject
    public DatabaseInfoCacheProvider(Provider<PluginContextLoadResult> pluginContextLoadResultProvider) {
        if (pluginContextLoadResultProvider == null) {
            throw new NullPointerException("pluginContextLoadResult must not be null");
        }
        this.pluginContextLoadResultProvider = pluginContextLoadResultProvider;
    }

    @Override
    public DatabaseInfoCache get() {
        PluginContextLoadResult pluginContextLoadResult = this.pluginContextLoadResultProvider.get();
        List<JdbcUrlParser> jdbcUrlParserList = pluginContextLoadResult.getJdbcUrlParserList();
        DatabaseInfoCache databaseInfoCache = new DatabaseInfoCache(jdbcUrlParserList);
        return databaseInfoCache;
    }

}
