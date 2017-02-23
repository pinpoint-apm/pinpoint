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
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DefaultDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParser;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class DatabaseInfoCacheTest {

    private static final String MYSQL_NORMALIZED_URL = "jdbc:mysql://ip_address:3306/database_name";
    private static final String MYSQL_JDBC_URL = MYSQL_NORMALIZED_URL + "?useUnicode=yes&amp;characterEncoding=UTF-8";

    private final JdbcUrlParser jdbcUrlParser = new MockJdbcUrlParser();

    @Test
    public void cacheTest() throws Exception {
        DatabaseInfoCache databaseInfoCache = new DatabaseInfoCache(Arrays.asList(jdbcUrlParser));

        DatabaseInfo databaseInfo = databaseInfoCache.getDatabaseInfo(MYSQL_JDBC_URL);
        Assert.assertNull(databaseInfo);

        databaseInfo = jdbcUrlParser.parse(MYSQL_JDBC_URL);
        DatabaseInfo cache1 = databaseInfoCache.getDatabaseInfo(MYSQL_JDBC_URL);
        DatabaseInfo cache2 = databaseInfoCache.getDatabaseInfo(MYSQL_JDBC_URL);

        Assert.assertTrue(databaseInfo == cache1 && databaseInfo == cache2);
    }

    private static class MockJdbcUrlParser extends JdbcUrlParser {

        @Override
        protected DatabaseInfo doParse(String url) {
            if (MYSQL_JDBC_URL.equals(url)) {
                DatabaseInfo dbInfo = new DefaultDatabaseInfo(ServiceType.UNKNOWN_DB, ServiceType.UNKNOWN_DB_EXECUTE_QUERY, MYSQL_JDBC_URL, MYSQL_NORMALIZED_URL, Arrays.asList("ip_address:3306"), "database_name");
                return dbInfo;
            }
            return UnKnownDatabaseInfo.createUnknownDataBase(url);
        }
    }

}
