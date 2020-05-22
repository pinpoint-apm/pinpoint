/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://jdbc.postgresql.org/documentation/head/connect.html
 * @author Brad Hong
 */
public class PostgreSqlJdbcUrlParserTest {

    private Logger logger = LoggerFactory.getLogger(PostgreSqlJdbcUrlParserTest.class);
    private final PostgreSqlJdbcUrlParser jdbcUrlParser = new PostgreSqlJdbcUrlParser();

    @Test
    public void postgresqlParse1() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:postgresql://host1:5432/database_name?user=fred");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), PostgreSqlConstants.POSTGRESQL);
        Assert.assertEquals(dbInfo.getHost().get(0), ("host1:5432"));
        Assert.assertEquals(dbInfo.getDatabaseId(), "database_name");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:postgresql://host1:5432/database_name");
    }

    @Test
    public void postgresqlParse2() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:postgresql://host1:5432/databaseName");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), PostgreSqlConstants.POSTGRESQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "host1:5432");

        Assert.assertEquals(dbInfo.getDatabaseId(), "databaseName");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:postgresql://host1:5432/databaseName");
        logger.info(dbInfo.toString());
        logger.info(dbInfo.getMultipleHost());
    }

    @Test
    public void postgresqlParse3() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:postgresql://host1/databaseName?user=fred");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), PostgreSqlConstants.POSTGRESQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "host1");
        Assert.assertEquals(dbInfo.getDatabaseId(), "databaseName");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:postgresql://host1/databaseName");
        logger.info(dbInfo.toString());
    }

    @Test
    public void postgresqlParseFailover1() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:postgresql://host1:5432/databaseName?user=fred");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), PostgreSqlConstants.POSTGRESQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "host1:5432");
        Assert.assertEquals(dbInfo.getDatabaseId(), "databaseName");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:postgresql://host1:5432/databaseName");
        logger.info(dbInfo.toString());
    }


    @Test
    public void postgresqlParseFailover2() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:postgresql://host1:5432,host2:5432/databaseName?user=fred");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), PostgreSqlConstants.POSTGRESQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "host1:5432");
        Assert.assertEquals(dbInfo.getHost().get(1), "host2:5432");
        Assert.assertEquals(dbInfo.getDatabaseId(), "databaseName");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:postgresql://host1:5432,host2:5432/databaseName");
        logger.info(dbInfo.toString());
    }


    @Test
    public void parseFailTest1() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse(null);
        Assert.assertFalse(dbInfo.isParsingComplete());

        Assert.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

    @Test
    public void parseFailTest2() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql://host1:5432/databaseName?user=fred");
        Assert.assertFalse(dbInfo.isParsingComplete());

        Assert.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

}
