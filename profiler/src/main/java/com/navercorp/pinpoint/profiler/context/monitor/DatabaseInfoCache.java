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

package com.navercorp.pinpoint.profiler.context.monitor;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParser;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Taejin Koo
 */
public class DatabaseInfoCache {

    private final List<JdbcUrlParser> jdbcUrlParserList;

    private final ConcurrentHashMap<String, DatabaseInfo> cache = new ConcurrentHashMap<String, DatabaseInfo>();

    public DatabaseInfoCache(List<JdbcUrlParser> jdbcUrlParserList) {
        this.jdbcUrlParserList = jdbcUrlParserList;
    }

    public DatabaseInfo getDatabaseInfo(String jdbcUrl) {
        DatabaseInfo databaseInfo = cache.get(jdbcUrl);
        if (databaseInfo != null) {
            return databaseInfo;
        }

        for (JdbcUrlParser jdbcUrlParser : jdbcUrlParserList) {
            if (jdbcUrlParser.hasCache(jdbcUrl)) {
                DatabaseInfo result = jdbcUrlParser.parse(jdbcUrl);
                DatabaseInfo old = cache.putIfAbsent(jdbcUrl, result);
                if (old != null) {
                    return old;
                }
                return result;
            }
        }
        return null;
    }

}
