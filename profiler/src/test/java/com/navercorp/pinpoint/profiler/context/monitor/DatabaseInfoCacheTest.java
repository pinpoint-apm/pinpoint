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
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcConnectionStringParser;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.DefaultJdbcConnectionStringParserContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Taejin Koo
 */
public class DatabaseInfoCacheTest {

    private static final String MYSQL_NORMALIZED_URL = "jdbc:mysql://ip_address:3306/database_name";
    private static final String MYSQL_JDBC_URL = MYSQL_NORMALIZED_URL + "?useUnicode=yes&amp;characterEncoding=UTF-8";

    private final JdbcConnectionStringParser jdbcUrlParser = new MockJdbcUrlParser();

    @Test
    public void cacheTest1() throws Exception {
        DefaultJdbcConnectionStringParserContext jdbcUrlParserContext = new DefaultJdbcConnectionStringParserContext(Arrays.asList(jdbcUrlParser));

        DatabaseInfo databaseInfo = jdbcUrlParserContext.getDatabaseInfo(MYSQL_JDBC_URL);
        Assert.assertNull(databaseInfo);

        DatabaseInfo parsingResult = jdbcUrlParserContext.parse(ServiceType.TEST, MYSQL_JDBC_URL);
        Assert.assertTrue(parsingResult.isParsingComplete());

        DatabaseInfo cache1 = jdbcUrlParserContext.getDatabaseInfo(MYSQL_JDBC_URL);
        DatabaseInfo cache2 = jdbcUrlParserContext.getDatabaseInfo(MYSQL_JDBC_URL);

        Assert.assertTrue(parsingResult == cache1 && parsingResult == cache2);
    }

    @Test
    public void cacheTest2() throws Exception {
        DefaultJdbcConnectionStringParserContext jdbcUrlParserContext = new DefaultJdbcConnectionStringParserContext(Arrays.asList(jdbcUrlParser));

        DatabaseInfo parsingResult = jdbcUrlParserContext.parse(ServiceType.TEST, MYSQL_JDBC_URL);
        Assert.assertTrue(parsingResult.isParsingComplete());

        DatabaseInfo cache1 = jdbcUrlParserContext.getDatabaseInfo(ServiceType.TEST, MYSQL_JDBC_URL);
        DatabaseInfo cache2 = jdbcUrlParserContext.getDatabaseInfo(ServiceType.UNKNOWN_DB, MYSQL_JDBC_URL);

        Assert.assertNotEquals(cache1, cache2);
    }

    private static class MockJdbcUrlParser implements JdbcConnectionStringParser {

        @Override
        public DatabaseInfo parse(String url) {
            if (MYSQL_JDBC_URL.equals(url)) {
                DatabaseInfo dbInfo = new DefaultDatabaseInfo(ServiceType.UNKNOWN_DB, ServiceType.UNKNOWN_DB_EXECUTE_QUERY, MYSQL_JDBC_URL, MYSQL_NORMALIZED_URL, Arrays.asList("ip_address:3306"), "database_name");
                return dbInfo;
            }
            return UnKnownDatabaseInfo.createUnknownDataBase(url);
        }

        @Override
        public ServiceType getServiceType() {
            return ServiceType.TEST;
        }

    }

}
