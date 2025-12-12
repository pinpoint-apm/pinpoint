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

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcOption;
import com.navercorp.pinpoint.profiler.cache.SimpleCache;
import com.navercorp.pinpoint.profiler.cache.UidCache;
import com.navercorp.pinpoint.profiler.cache.UidGenerator;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SimpleCacheFactory {
    private final int sqlCacheSize;
    private final int sqlCacheBypassLength;
    private final long sqlCacheExpireHours;

    public SimpleCacheFactory(JdbcOption jdbcOption) {
        this.sqlCacheSize = jdbcOption.getJdbcSqlCacheSize();
        this.sqlCacheBypassLength = jdbcOption.getMaxSqlCacheLength();
        this.sqlCacheExpireHours = jdbcOption.getSqlCacheExpireHours();
    }

    public <T> SimpleCache<T, Integer> newSimpleCache() {
        return SimpleCache.newIdCache();
    }

    public SimpleCache<String, Integer> newSqlCache() {
        return SimpleCache.newIdCache(sqlCacheSize);
    }

    public SimpleCache<String, byte[]> newSqlUidCache() {
        return new UidCache(sqlCacheSize, sqlCacheExpireHours, new UidGenerator.Murmur(), sqlCacheBypassLength);
    }
}

