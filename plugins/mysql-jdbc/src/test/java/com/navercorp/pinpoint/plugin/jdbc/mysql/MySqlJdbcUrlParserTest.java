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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class MySqlJdbcUrlParserTest {

    private final Logger logger = LogManager.getLogger(getClass());
    private final MySqlJdbcUrlParser jdbcUrlParser = new MySqlJdbcUrlParser();

    private static final ServiceType SERVICE_TYPE = MySqlConstants.MYSQL;
    private static final String DATABASE_ID = "database_name";
    private static final String IP = "1.2.3.4";
    private static final String PORT = "3306";
    private static final String IP_PORT = IP + ":" + PORT;

    private static final String IP2 = "5.6.7.8";
    private static final String PORT2 = "5000";
    private static final String IP_PORT2 = IP2 + ":" + PORT2;

    private static final String URL = MySqlJdbcUrlParser.URL_PREFIX;
    private static final String CONNECTION_STRING = URL + "//" + IP_PORT + "/" + DATABASE_ID;
    private static final String CONNECTION_STRING_NO_PORT = URL + "//" + IP + "/" + DATABASE_ID;

    private static final String LOADBALANCE_CONNECTION_STRING = URL + "loadbalance:" + "//" + IP_PORT + "/" + DATABASE_ID;
    private static final String LOADBALANCE_CONNECTION_STRING2 = URL + "loadbalance:" + "//" + IP_PORT + "," + IP_PORT2 + "/" + DATABASE_ID;

    @Test
    public void mysqlParse1() {
        final String jdbcUrl = CONNECTION_STRING + "?useUnicode=yes&amp;characterEncoding=UTF-8";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), (IP_PORT));
        Assertions.assertEquals(dbInfo.getDatabaseId(), DATABASE_ID);
        Assertions.assertEquals(dbInfo.getUrl(), CONNECTION_STRING);
    }

    @Test
    public void mysqlParse2() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse(CONNECTION_STRING);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), IP_PORT);
        Assertions.assertEquals(dbInfo.getDatabaseId(), DATABASE_ID);
        Assertions.assertEquals(dbInfo.getUrl(), CONNECTION_STRING);
        logger.debug(dbInfo);
        logger.debug(dbInfo.getMultipleHost());
    }

    @Test
    public void mysqlParse3() {
        final String jdbcUrl = CONNECTION_STRING_NO_PORT + "?useUnicode=yes&amp;characterEncoding=UTF-8";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), IP);
        Assertions.assertEquals(dbInfo.getDatabaseId(), DATABASE_ID);
        Assertions.assertEquals(dbInfo.getUrl(), CONNECTION_STRING_NO_PORT);
        logger.debug(dbInfo);
    }

    @Test
    public void mysqlParse4() {
        final String jdbcUrl = CONNECTION_STRING + "?useUnicode=yes&amp;characterEncoding=UTF-8&serverTimezone=Asia/Seoul";

        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), IP_PORT);
        Assertions.assertEquals(dbInfo.getDatabaseId(), DATABASE_ID);
        Assertions.assertEquals(dbInfo.getUrl(), CONNECTION_STRING);
        logger.debug(dbInfo);
    }

    @Test
    public void mysqlParse5() {
        final String jdbcUrl = LOADBALANCE_CONNECTION_STRING + "?characterEncoding=UTF-8&serverTimezone=Asia/Seoul";

        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), IP_PORT);
        Assertions.assertEquals(dbInfo.getDatabaseId(), DATABASE_ID);
        Assertions.assertEquals(dbInfo.getUrl(), LOADBALANCE_CONNECTION_STRING);
        logger.debug(dbInfo);
    }

    @Test
    public void mysqlParse6() {
        final String jdbcUrl = CONNECTION_STRING + "?useUnicode=true&characterEncoding=UTF-8&noAccessToProcedureBodies=true&autoDeserialize=true&elideSetAutoCommits=true&sessionVariables=time_zone='%2B09:00',tx_isolation='READ-COMMITTED'";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), IP_PORT);
        Assertions.assertEquals(dbInfo.getDatabaseId(), DATABASE_ID);
        Assertions.assertEquals(dbInfo.getUrl(), CONNECTION_STRING);
        logger.debug(dbInfo);
    }

    @Test
    public void mysqlParse_loadbalance1() {
        final String jdbcUrl = LOADBALANCE_CONNECTION_STRING + "?useUnicode=true&characterEncoding=UTF-8&noAccessToProcedureBodies=true&autoDeserialize=true&elideSetAutoCommits=true&sessionVariables=time_zone='%2B09:00',tx_isolation='READ-UNCOMMITTED'";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), IP_PORT);
        Assertions.assertEquals(dbInfo.getDatabaseId(), DATABASE_ID);
        Assertions.assertEquals(dbInfo.getUrl(), LOADBALANCE_CONNECTION_STRING);
        logger.debug(dbInfo);
    }

    @Test
    public void mysqlParse_loadbalance2() {
        final String jdbcUrl = LOADBALANCE_CONNECTION_STRING2 + "?useUnicode=true&characterEncoding=UTF-8&noAccessToProcedureBodies=true&autoDeserialize=true&elideSetAutoCommits=true&sessionVariables=time_zone='%2B09:00',tx_isolation='READ-UNCOMMITTED'";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), SERVICE_TYPE);
        Assertions.assertEquals(dbInfo.getHost().get(0), IP_PORT);
        Assertions.assertEquals(dbInfo.getHost().get(1), IP_PORT2);
        Assertions.assertEquals(dbInfo.getDatabaseId(), DATABASE_ID);
        Assertions.assertEquals(dbInfo.getUrl(), LOADBALANCE_CONNECTION_STRING2);
        logger.debug(dbInfo);
    }

    @Test
    public void parseFailTest1() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse(null);
        Assertions.assertFalse(dbInfo.isParsingComplete());

        Assertions.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

    @Test
    public void parseFailTest2() {
        final String jdbcUrl = "jdbc:oracle:loadbalance://1.2.3.4:3306";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertFalse(dbInfo.isParsingComplete());

        Assertions.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

    @Test
    public void emptyDataBase() {
        final String jdbcUrl = "jdbc:mysql://1.2.3.4:3306";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(SERVICE_TYPE, dbInfo.getType());
        Assertions.assertEquals(MySqlJdbcUrlParser.EMPTY_DATABASE, dbInfo.getDatabaseId());
    }

}
