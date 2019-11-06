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

package com.navercorp.pinpoint.plugin.jdbc.jtds;


import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.junit.Assert;
import org.junit.Test;

public class JtdsJdbcUrlParserTest {

    private final JtdsJdbcUrlParser parser = new JtdsJdbcUrlParser();

    @Test
    public void testParse1() throws Exception {
//        jdbc:jtds:sqlserver://server[:port][/database][;property=value[;...]]
//        jdbc:jtds:sqlserver://server/db;user=userName;password=password
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx:1433;DatabaseName=CAFECHAT;sendStringParametersAsUnicode=false;useLOBs=false;loginTimeout=3";
        DatabaseInfo info = parser.parse(url);
        Assert.assertTrue(info.isParsingComplete());

        Assert.assertEquals(info.getType(), JtdsConstants.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx:1433");
        Assert.assertEquals(info.getDatabaseId(), "CAFECHAT");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx:1433");

    }

    @Test
    public void testParse2() throws Exception {
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT;sendStringParametersAsUnicode=false;useLOBs=false;loginTimeout=3";
        DatabaseInfo info = parser.parse(url);
        Assert.assertTrue(info.isParsingComplete());

        Assert.assertEquals(info.getType(), JtdsConstants.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx:1433");
        Assert.assertEquals(info.getDatabaseId(), "CAFECHAT");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT");

    }

    @Test
    public void testParse3() throws Exception {
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT";
        DatabaseInfo info = parser.parse(url);
        Assert.assertTrue(info.isParsingComplete());

        Assert.assertEquals(info.getType(), JtdsConstants.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx:1433");
        Assert.assertEquals(info.getDatabaseId(), "CAFECHAT");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT");
    }

    @Test
    public void testParse4() throws Exception {
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx:1433";
        DatabaseInfo info = parser.parse(url);
        Assert.assertTrue(info.isParsingComplete());

        Assert.assertEquals(info.getType(), JtdsConstants.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx:1433");
        Assert.assertEquals(info.getDatabaseId(), "");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx:1433");
    }


    @Test
    public void testParse5() throws Exception {
//        jdbc:jtds:sqlserver://server[:port][/database][;property=value[;...]]
//        jdbc:jtds:sqlserver://server/db;user=userName;password=password
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx;DatabaseName=CAFECHAT";
        DatabaseInfo info = parser.parse(url);
        Assert.assertTrue(info.isParsingComplete());

        Assert.assertEquals(info.getType(), JtdsConstants.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx");
        Assert.assertEquals(info.getDatabaseId(), "CAFECHAT");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx");

    }

    @Test
    public void testParse6() throws Exception {
//        jdbc:jtds:sqlserver://server[:port][/database][;property=value[;...]]
//        jdbc:jtds:sqlserver://server/db;user=userName;password=password
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx";
        DatabaseInfo info = parser.parse(url);
        Assert.assertTrue(info.isParsingComplete());

        Assert.assertEquals(info.getType(), JtdsConstants.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx");
        Assert.assertEquals(info.getDatabaseId(), "");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx");

    }


    @Test
    public void testParse7() throws Exception {
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT;abc=1;bcd=2";
        DatabaseInfo info = parser.parse(url);
        Assert.assertTrue(info.isParsingComplete());

        Assert.assertEquals(info.getType(), JtdsConstants.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx:1433");
        Assert.assertEquals(info.getDatabaseId(), "CAFECHAT");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT");
    }

    @Test
    public void parseFailTest1() {
        DatabaseInfo info = parser.parse(null);
        Assert.assertFalse(info.isParsingComplete());

        Assert.assertEquals(ServiceType.UNKNOWN_DB, info.getType());
    }

    @Test
    public void parseFailTest2() {
        String url = "jdbc:oracle:sqlserver://10.xx.xx.xx:1433/CAFECHAT;abc=1;bcd=2";
        DatabaseInfo info = parser.parse(url);
        Assert.assertFalse(info.isParsingComplete());

        Assert.assertEquals(ServiceType.UNKNOWN_DB, info.getType());
    }

}