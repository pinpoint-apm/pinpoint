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
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 * @author emeroad
 */
public class JDBCUrlParserTest {

    private Logger logger = LoggerFactory.getLogger(JDBCUrlParserTest.class);
    private OracleJdbcUrlParser jdbcUrlParser = new OracleJdbcUrlParser();


    @Test
    public void oracleParser1() {
        //    jdbc:oracle:thin:@hostname:port:SID
//      "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:oracle:thin:@hostname:port:SID");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), OracleConstants.ORACLE);
        Assert.assertEquals(dbInfo.getHost().get(0), "hostname:port");
        Assert.assertEquals(dbInfo.getDatabaseId(), "SID");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:oracle:thin:@hostname:port:SID");
        logger.info(dbInfo.toString());
    }

    @Test
    public void oracleParser2() {
        //    jdbc:oracle:thin:@hostname:port:SID
//      "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), OracleConstants.ORACLE);
        Assert.assertEquals(dbInfo.getHost().get(0), "localhost:1521");
        Assert.assertEquals(dbInfo.getDatabaseId(), "XE");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE");
        logger.info(dbInfo.toString());
    }

    @Test
    public void oracleParserServiceName() {
        //    jdbc:oracle:thin:@hostname:port:SID
//      "jdbc:oracle:thin:MYWORKSPACE/qwerty@localhost:1521:XE";
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:oracle:thin:@hostname:port/serviceName");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), OracleConstants.ORACLE);
        Assert.assertEquals(dbInfo.getHost().get(0), "hostname:port");
        Assert.assertEquals(dbInfo.getDatabaseId(), "serviceName");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:oracle:thin:@hostname:port/serviceName");
        logger.info(dbInfo.toString());
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
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(dbInfo.getType(), OracleConstants.ORACLE);
        Assert.assertEquals(dbInfo.getHost().get(0), "1.2.3.4:1521");
        Assert.assertEquals(dbInfo.getHost().get(1), "1.2.3.5:1522");

        Assert.assertEquals(dbInfo.getDatabaseId(), "service");
        Assert.assertEquals(dbInfo.getUrl(), rac);
        logger.info(dbInfo.toString());
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
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(OracleConstants.ORACLE, dbInfo.getType());
        Assert.assertEquals("1.2.3.4:1521", dbInfo.getHost().get(0));
        Assert.assertEquals("1.2.3.5:1521", dbInfo.getHost().get(1));
        Assert.assertEquals("2.3.4.5:1521", dbInfo.getHost().get(2));
        Assert.assertEquals("2.3.4.6:1521", dbInfo.getHost().get(3));

        Assert.assertEquals("service_test", dbInfo.getDatabaseId());
        Assert.assertEquals(url, dbInfo.getUrl());
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
        DatabaseInfo dbInfo = jdbcUrlParser.parse("jdbc:mysql:thin:@hostname:port:SID");
        Assert.assertFalse(dbInfo.isParsingComplete());

        Assert.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }


}
