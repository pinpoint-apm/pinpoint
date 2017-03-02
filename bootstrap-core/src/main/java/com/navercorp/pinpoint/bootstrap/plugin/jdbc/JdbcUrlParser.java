/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Jongho Moon
 *
 */
/**
 * @deprecated Since 1.6.1. Use {@link JdbcUrlParserV2 )}
 */
@Deprecated
public abstract class JdbcUrlParser {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final ConcurrentMap<String, DatabaseInfo> cache = new ConcurrentHashMap<String, DatabaseInfo>();

    //http://www.petefreitag.com/articles/jdbc_urls/
    public DatabaseInfo parse(String url) {
        if (url == null) {
            return UnKnownDatabaseInfo.INSTANCE;
        }
        
        final DatabaseInfo hit = cache.get(url);
        if (hit != null) {
            logger.debug("database url cache hit:{} {}", url, hit);
            return hit;
        }

        DatabaseInfo databaseInfo = null;
        try {
            databaseInfo = doParse(url);
        } catch (Exception e) {
            logger.error("connectionString parse fail. url:{} ", url);
            databaseInfo = UnKnownDatabaseInfo.INSTANCE;
        }

        final DatabaseInfo old = cache.putIfAbsent(url, databaseInfo);
 
        if (old != null) {
            return old;
        }
        
        return databaseInfo;
    }

    protected abstract DatabaseInfo doParse(String url);

}
