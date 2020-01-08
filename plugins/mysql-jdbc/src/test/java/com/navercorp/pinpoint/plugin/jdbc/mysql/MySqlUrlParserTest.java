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

package com.navercorp.pinpoint.plugin.jdbc.mysql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author emeroad
 */
public class MySqlUrlParserTest {

    private Logger logger = LoggerFactory.getLogger(MySqlUrlParserTest.class);
    private MySqlJdbcUrlParser jdbcUrlParser = new MySqlJdbcUrlParser();

     @Test
    public void mysqlParse1() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql://ip_address:3306/database_name?useUnicode=yes&amp;characterEncoding=UTF-8");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), MySqlConstants.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), ("ip_address:3306"));
        Assert.assertEquals(dbInfo.getDatabaseId(), "database_name");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://ip_address:3306/database_name");
    }

    @Test
    public void mysqlParse2() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql://10.98.133.22:3306/test_lucy_db");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), MySqlConstants.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "10.98.133.22:3306");

        Assert.assertEquals(dbInfo.getDatabaseId(), "test_lucy_db");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://10.98.133.22:3306/test_lucy_db");
        logger.debug(dbInfo.toString());
        logger.debug(dbInfo.getMultipleHost());
    }

    @Test
    public void mysqlParse3() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql://61.74.71.31/log?useUnicode=yes&amp;characterEncoding=UTF-8");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), MySqlConstants.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "61.74.71.31");
        Assert.assertEquals(dbInfo.getDatabaseId(), "log");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://61.74.71.31/log");
        logger.debug(dbInfo.toString());
    }

    @Test
    public void mysqlParse4() {
        final String url = "jdbc:mysql://1.2.3.4:3306/database_name?characterEncoding=UTF-8&serverTimezone=Asia/Seoul";

        DatabaseInfo dbInfo = jdbcUrlParser.parse(url);
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), MySqlConstants.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "1.2.3.4:3306");
        Assert.assertEquals(dbInfo.getDatabaseId(), "database_name");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://1.2.3.4:3306/database_name");
        logger.debug(dbInfo.toString());
    }

    @Test
    public void mysqlParse5() {
        final String url = "jdbc:mysql:loadbalance://1.2.3.4:3306/database_name?characterEncoding=UTF-8&serverTimezone=Asia/Seoul";

        DatabaseInfo dbInfo = jdbcUrlParser.parse(url);
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), MySqlConstants.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "1.2.3.4:3306");
        Assert.assertEquals(dbInfo.getDatabaseId(), "database_name");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql:loadbalance://1.2.3.4:3306/database_name");
        logger.debug(dbInfo.toString());
    }

    @Test
    public void mysqlParseCookierunMaster() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql://10.115.8.209:5605/db_cookierun?useUnicode=true&characterEncoding=UTF-8&noAccessToProcedureBodies=true&autoDeserialize=true&elideSetAutoCommits=true&sessionVariables=time_zone='%2B09:00',tx_isolation='READ-COMMITTED'");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), MySqlConstants.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "10.115.8.209:5605");
        Assert.assertEquals(dbInfo.getDatabaseId(), "db_cookierun");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://10.115.8.209:5605/db_cookierun");
        logger.debug(dbInfo.toString());
    }


    @Test
    public void mysqlParseCookierunSlave() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql:loadbalance://10.118.222.35:5605/db_cookierun?useUnicode=true&characterEncoding=UTF-8&noAccessToProcedureBodies=true&autoDeserialize=true&elideSetAutoCommits=true&sessionVariables=time_zone='%2B09:00',tx_isolation='READ-UNCOMMITTED'");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), MySqlConstants.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "10.118.222.35:5605");
        Assert.assertEquals(dbInfo.getDatabaseId(), "db_cookierun");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql:loadbalance://10.118.222.35:5605/db_cookierun");
        logger.debug(dbInfo.toString());
    }

    @Test
    public void mysqlParseCookierunSlave2() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql:loadbalance://10.118.222.35:5605,10.118.222.36:5605/db_cookierun?useUnicode=true&characterEncoding=UTF-8&noAccessToProcedureBodies=true&autoDeserialize=true&elideSetAutoCommits=true&sessionVariables=time_zone='%2B09:00',tx_isolation='READ-UNCOMMITTED'");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), MySqlConstants.MYSQL);
        Assert.assertEquals(dbInfo.getHost().get(0), "10.118.222.35:5605");
        Assert.assertEquals(dbInfo.getHost().get(1), "10.118.222.36:5605");
        Assert.assertEquals(dbInfo.getDatabaseId(), "db_cookierun");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql:loadbalance://10.118.222.35:5605,10.118.222.36:5605/db_cookierun");
        logger.debug(dbInfo.toString());
    }

    @Test
    public void parseFailTest1() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse(null);
        Assert.assertFalse(dbInfo.isParsingComplete());

        Assert.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

    @Test
    public void parseFailTest2() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:oracle:loadbalance://10.118.222.35:5605,10.118.222.36:5605/db_cookierun?useUnicode=true&characterEncoding=UTF-8&noAccessToProcedureBodies=true&autoDeserialize=true&elideSetAutoCommits=true&sessionVariables=time_zone='%2B09:00',tx_isolation='READ-UNCOMMITTED'");
        Assert.assertFalse(dbInfo.isParsingComplete());

        Assert.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

}
