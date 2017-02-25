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

package com.navercorp.pinpoint.bootstrap.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;

import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class JdbcUrlParserManagerTest {

    private final String mysqlNormalizedUrl = "jdbc:mysql://ip_address:3306/database_name";
    private final String mysqlJdbcUrl = mysqlNormalizedUrl + "?useUnicode=yes&amp;characterEncoding=UTF-8";

    @Mock
    JdbcUrlParser jdbcUrlParser;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(jdbcUrlParser.getServiceType()).thenReturn(ServiceType.UNKNOWN_DB);
        when(jdbcUrlParser.isPrefixMatch(mysqlJdbcUrl)).thenReturn(true);

        DatabaseInfo dbInfo = new DefaultDatabaseInfo(ServiceType.UNKNOWN_DB, ServiceType.UNKNOWN_DB_EXECUTE_QUERY, mysqlJdbcUrl, mysqlNormalizedUrl, Arrays.asList("ip_address:3306"), "database_name");
        when(jdbcUrlParser.parse(mysqlJdbcUrl)).thenReturn(new JdbcUrlParsingResult(dbInfo));
    }

    @Test
    public void parseTest1() throws Exception {
        JdbcUrlParserManager parserManager = new DefaultJdbcUrlParserManager();

        JdbcUrlParsingResult parsingResult1 = parserManager.parseWithResult(mysqlJdbcUrl);
        Assert.assertFalse(parsingResult1.isSuccess());

        // get at fail cache
        JdbcUrlParsingResult parsingResult2 = parserManager.parseWithResult(mysqlJdbcUrl);
        Assert.assertFalse(parsingResult2.isSuccess());

        Assert.assertEquals(parsingResult1, parsingResult2);
    }

    @Test
    public void parseTest2() throws Exception {
        JdbcUrlParserManager parserManager = new DefaultJdbcUrlParserManager();

        JdbcUrlParsingResult parsingResult1 = parserManager.parseWithResult(mysqlJdbcUrl);
        Assert.assertFalse(parsingResult1.isSuccess());

        parserManager.addJdbcUrlParser(jdbcUrlParser);

        JdbcUrlParsingResult parsingResult2 = parserManager.parseWithResult(mysqlJdbcUrl);
        Assert.assertTrue(parsingResult2.isSuccess());
    }

    @Test
    public void parseTest3() throws Exception {
        JdbcUrlParserManager parserManager = new DefaultJdbcUrlParserManager();

        parserManager.addJdbcUrlParser(jdbcUrlParser);

        JdbcUrlParsingResult parsingResult1 = parserManager.parseWithResult(mysqlJdbcUrl);
        Assert.assertTrue(parsingResult1.isSuccess());

        // get at success cache
        JdbcUrlParsingResult parsingResult2 = parserManager.parseWithResult(mysqlJdbcUrl);
        Assert.assertTrue(parsingResult2.isSuccess());

        Assert.assertEquals(parsingResult1, parsingResult2);
    }

    @Test
    public void parseTest4() throws Exception {
        JdbcUrlParserManager parserManager = new DefaultJdbcUrlParserManager();

        parserManager.addJdbcUrlParser(jdbcUrlParser);

        JdbcUrlParsingResult parsingResult1 = parserManager.parseWithResult(ServiceType.INTERNAL_METHOD, mysqlJdbcUrl);
        Assert.assertFalse(parsingResult1.isSuccess());

        JdbcUrlParsingResult parsingResult2 = parserManager.parseWithResult(ServiceType.INTERNAL_METHOD, mysqlJdbcUrl);
        Assert.assertFalse(parsingResult2.isSuccess());

        Assert.assertTrue(parsingResult1 == parsingResult2);

        JdbcUrlParsingResult parsingResult3 = parserManager.parseWithResult(ServiceType.ASYNC, mysqlJdbcUrl);
        Assert.assertFalse(parsingResult3.isSuccess());

        Assert.assertTrue(parsingResult2 != parsingResult3);

        JdbcUrlParsingResult parsingResult4 = parserManager.parseWithResult(jdbcUrlParser.getServiceType(), mysqlJdbcUrl);
        Assert.assertTrue(parsingResult4.isSuccess());

        JdbcUrlParsingResult parsingResult5 = parserManager.parseWithResult(ServiceType.INTERNAL_METHOD, mysqlJdbcUrl);
        Assert.assertTrue(parsingResult5.isSuccess());

        Assert.assertEquals(parsingResult4, parsingResult5);
    }

}
