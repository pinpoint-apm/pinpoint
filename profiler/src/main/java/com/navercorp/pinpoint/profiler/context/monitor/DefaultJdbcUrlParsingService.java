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

package com.navercorp.pinpoint.profiler.context.monitor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Taejin Koo
 */
public class DefaultJdbcUrlParsingService implements JdbcUrlParsingService {

    private final List<JdbcUrlParserV2> jdbcUrlParserList;

    private final ConcurrentHashMap<String, DatabaseInfo> cache = new ConcurrentHashMap<String, DatabaseInfo>();
    private final ConcurrentHashMap<CacheKey, DatabaseInfo> eachServiceTypeCache = new ConcurrentHashMap<CacheKey, DatabaseInfo>();

    public DefaultJdbcUrlParsingService(List<JdbcUrlParserV2> jdbcUrlParserList) {
        this.jdbcUrlParserList = jdbcUrlParserList;
    }

    @Override
    public DatabaseInfo getDatabaseInfo(String jdbcUrl) {
        DatabaseInfo databaseInfo = cache.get(jdbcUrl);
        return databaseInfo;
    }

    @Override
    public DatabaseInfo getDatabaseInfo(ServiceType serviceType, String jdbcUrl) {
        CacheKey cacheKey = new CacheKey(serviceType, jdbcUrl);
        DatabaseInfo databaseInfo = eachServiceTypeCache.get(cacheKey);
        if (databaseInfo != null && databaseInfo.isParsingComplete()) {
            return databaseInfo;
        }
        return null;
    }

    @Override
    public DatabaseInfo parseJdbcUrl(ServiceType serviceType, String jdbcUrl) {
        if (serviceType == null) {
            return UnKnownDatabaseInfo.INSTANCE;
        }

        if (jdbcUrl == null) {
            return UnKnownDatabaseInfo.INSTANCE;
        }

        CacheKey cacheKey = new CacheKey(serviceType, jdbcUrl);
        DatabaseInfo cacheValue = eachServiceTypeCache.get(cacheKey);
        if (cacheValue != null) {
            return cacheValue;
        }

        for (JdbcUrlParserV2 parser : jdbcUrlParserList) {
            if (parser.getServiceType() != null && serviceType.getCode() == parser.getServiceType().getCode()) {
                DatabaseInfo databaseInfo = parser.parse(jdbcUrl);
                return putCacheIfAbsent(cacheKey, databaseInfo);
            }
        }

        return putCacheIfAbsent(cacheKey, UnKnownDatabaseInfo.createUnknownDataBase(jdbcUrl));
    }

    private DatabaseInfo putCacheIfAbsent(CacheKey cacheKey, DatabaseInfo databaseInfo) {
        if (databaseInfo.isParsingComplete()) {
            cache.putIfAbsent(cacheKey.getJdbcUrl(), databaseInfo);
        }

        DatabaseInfo old = eachServiceTypeCache.putIfAbsent(cacheKey, databaseInfo);
        if (old != null) {
            return old;
        }

        return databaseInfo;
    }

    private static class CacheKey {

        private final ServiceType serviceType;
        private final String jdbcUrl;

        public CacheKey(ServiceType serviceType, String jdbcUrl) {
            this.serviceType = serviceType;
            this.jdbcUrl = jdbcUrl;
        }

        public ServiceType getServiceType() {
            return serviceType;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (serviceType != null ? !serviceType.equals(cacheKey.serviceType) : cacheKey.serviceType != null) return false;
            return jdbcUrl != null ? jdbcUrl.equals(cacheKey.jdbcUrl) : cacheKey.jdbcUrl == null;

        }

        @Override
        public int hashCode() {
            int result = serviceType != null ? serviceType.hashCode() : 0;
            result = 31 * result + (jdbcUrl != null ? jdbcUrl.hashCode() : 0);
            return result;
        }

    }
}
