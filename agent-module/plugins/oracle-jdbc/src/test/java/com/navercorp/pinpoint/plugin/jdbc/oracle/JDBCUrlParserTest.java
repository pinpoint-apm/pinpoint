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

package com.navercorp.pinpoint.plugin.jdbc.oracle;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author emeroad
 */
public class JDBCUrlParserTest {

    private Logger logger = LogManager.getLogger(JDBCUrlParserTest.class);
    private OracleJdbcUrlParser jdbcUrlParser = new OracleJdbcUrlParser();


    @Test
    public void oracleParser1() {
        //    jdbc:oracle:thin:@hostname:port:SID
//      "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:oracle:thin:@hostname:port:SID");
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), OracleConstants.ORACLE);
        Assertions.assertEquals(dbInfo.getHost().get(0), "hostname:port");
        Assertions.assertEquals(dbInfo.getDatabaseId(), "SID");
        Assertions.assertEquals(dbInfo.getUrl(), "jdbc:oracle:thin:@hostname:port:SID");
        logger.info(dbInfo);
    }

    @Test
    public void oracleParser2() {
        //    jdbc:oracle:thin:@hostname:port:SID
//      "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE");
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), OracleConstants.ORACLE);
        Assertions.assertEquals(dbInfo.getHost().get(0), "localhost:1521");
        Assertions.assertEquals(dbInfo.getDatabaseId(), "XE");
        Assertions.assertEquals(dbInfo.getUrl(), "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE");
        logger.info(dbInfo);
    }

    @Test
    public void oracleParserServiceName() {
        //    jdbc:oracle:thin:@hostname:port:SID
//      "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:oracle:thin:@hostname:port/serviceName");
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), OracleConstants.ORACLE);
        Assertions.assertEquals(dbInfo.getHost().get(0), "hostname:port");
        Assertions.assertEquals(dbInfo.getDatabaseId(), "serviceName");
        Assertions.assertEquals(dbInfo.getUrl(), "jdbc:oracle:thin:@hostname:port/serviceName");
        logger.info(dbInfo);
    }

    @Test
    public void oracleRacParser1() {
//    "jdbc:oracle:thin:@(Description1=(LOAD_BALANCE=on)" +
//    "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.4) (PORT=1521))" +
//            "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.5) (PORT=1521))" +
//            "(CONNECT_DATA=(SERVICE_NAME=service)))"
        String rac = "jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=on)" +
                "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.4) (PORT=1521))" +
                "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.5) (PORT=1522))" +
                "(CONNECT_DATA=(SERVICE_NAME=service)))";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(rac);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(dbInfo.getType(), OracleConstants.ORACLE);
        Assertions.assertEquals(dbInfo.getHost().get(0), "1.2.3.4:1521");
        Assertions.assertEquals(dbInfo.getHost().get(1), "1.2.3.5:1522");

        Assertions.assertEquals(dbInfo.getDatabaseId(), "service");
        Assertions.assertEquals(dbInfo.getUrl(), rac);
        logger.info(dbInfo);
    }

    @Test
    public void oracleDescriptionListParser1() {
        String url = "jdbc:oracle:thin:@(DESCRIPTION_LIST=" +
                "(LOAD_BALANCE=off)(FAILOVER=on)" +
                "(DESCRIPTION=" +
                "(LOAD_BALANCE=on)" +
                "(ADDRESS=(PROTOCOL=tcp)(HOST=1.2.3.4)(PORT=1521))" +
                "(ADDRESS=(PROTOCOL=tcp)(HOST=1.2.3.5)(PORT=1521))" +
                "(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=service_test)))" +
                "(DESCRIPTION=" +
                "(LOAD_BALANCE=on)" +
                "(ADDRESS=(PROTOCOL=tcp)(HOST=2.3.4.5)(PORT=1521))" +
                "(ADDRESS=(PROTOCOL=tcp)(HOST=2.3.4.6)(PORT=1521))" +
                "(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME=service_test))))";
        DatabaseInfo dbInfo = jdbcUrlParser.parse(url);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(OracleConstants.ORACLE, dbInfo.getType());
        Assertions.assertEquals("1.2.3.4:1521", dbInfo.getHost().get(0));
        Assertions.assertEquals("1.2.3.5:1521", dbInfo.getHost().get(1));
        Assertions.assertEquals("2.3.4.5:1521", dbInfo.getHost().get(2));
        Assertions.assertEquals("2.3.4.6:1521", dbInfo.getHost().get(3));

        Assertions.assertEquals("service_test", dbInfo.getDatabaseId());
        Assertions.assertEquals(url, dbInfo.getUrl());
        logger.info(dbInfo);
    }

    @Test
    public void parseFailTest1() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse(null);
        Assertions.assertFalse(dbInfo.isParsingComplete());

        Assertions.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }

    @Test
    public void parseFailTest2() {
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql:thin:@hostname:port:SID");
        Assertions.assertFalse(dbInfo.isParsingComplete());

        Assertions.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }


}
