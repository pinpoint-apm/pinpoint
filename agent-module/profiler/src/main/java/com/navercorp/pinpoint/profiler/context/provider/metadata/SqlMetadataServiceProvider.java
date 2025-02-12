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

package com.navercorp.pinpoint.profiler.context.provider.metadata;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.profiler.message.DataSender;
import com.navercorp.pinpoint.profiler.cache.SimpleCache;
import com.navercorp.pinpoint.profiler.context.module.MetadataDataSender;
import com.navercorp.pinpoint.profiler.context.monitor.config.MonitorConfig;
import com.navercorp.pinpoint.profiler.metadata.DefaultSqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.MetaDataType;
import com.navercorp.pinpoint.profiler.metadata.SqlCacheService;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.SqlUidMetaDataService;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SqlMetadataServiceProvider implements Provider<SqlMetaDataService> {
    private final ProfilerConfig profilerConfig;
    private final MonitorConfig monitorConfig;
    private final DataSender<MetaDataType> dataSender;
    private final SimpleCacheFactory simpleCacheFactory;

    @Inject
    public SqlMetadataServiceProvider(ProfilerConfig profilerConfig,
                                      MonitorConfig monitorConfig,
                                      @MetadataDataSender DataSender<MetaDataType> dataSender,
                                      SimpleCacheFactory simpleCacheFactory) {
        this.profilerConfig = Objects.requireNonNull(profilerConfig, "profilerConfig");
        this.monitorConfig = Objects.requireNonNull(monitorConfig, "monitorConfig");
        this.dataSender = Objects.requireNonNull(dataSender, "dataSender");
        this.simpleCacheFactory = Objects.requireNonNull(simpleCacheFactory, "simpleCacheFactory");
    }

    @Override
    public SqlMetaDataService get() {
        final int maxSqlLength = profilerConfig.getJdbcOption().getMaxSqlLength();

        if (monitorConfig.isSqlStatEnable()) {
            SimpleCache<String, byte[]> sqlCache = simpleCacheFactory.newSqlUidCache();
            SqlCacheService<byte[]> sqlCacheService = new SqlCacheService<>(sqlCache, maxSqlLength);
            return new SqlUidMetaDataService(dataSender, sqlCacheService);
        } else {
            SimpleCache<String, Integer> sqlCache = simpleCacheFactory.newSqlCache();
            SqlCacheService<Integer> sqlCacheService = new SqlCacheService<>(sqlCache, maxSqlLength);
            return new DefaultSqlMetaDataService(dataSender, sqlCacheService);
        }
    }
}
