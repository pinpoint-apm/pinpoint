package com.nhn.pinpoint.profiler.modifier.db.jtds;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.modifier.db.ConnectionStringParser;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JtdsConnectionStringParserTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testParse1() throws Exception {
//        jdbc:jtds:sqlserver://server[:port][/database][;property=value[;...]]
//        jdbc:jtds:sqlserver://server/db;user=userName;password=password
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx:1433;DatabaseName=CAFECHAT;sendStringParametersAsUnicode=false;useLOBs=false;loginTimeout=3";
        ConnectionStringParser parser = new JtdsConnectionStringParser();
        DatabaseInfo info = parser.parse(url);
        logger.debug("{}", info);

        Assert.assertEquals(info.getType(), ServiceType.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx:1433");
        Assert.assertEquals(info.getDatabaseId(), "CAFECHAT");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx:1433");

    }

    @Test
    public void testParse2() throws Exception {
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT;sendStringParametersAsUnicode=false;useLOBs=false;loginTimeout=3";
        ConnectionStringParser parser = new JtdsConnectionStringParser();
        DatabaseInfo info = parser.parse(url);
        logger.debug("{}", info);

        Assert.assertEquals(info.getType(), ServiceType.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx:1433");
        Assert.assertEquals(info.getDatabaseId(), "CAFECHAT");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT");

    }

    @Test
    public void testParse3() throws Exception {
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT";
        ConnectionStringParser parser = new JtdsConnectionStringParser();
        DatabaseInfo info = parser.parse(url);
        logger.debug("{}", info);

        Assert.assertEquals(info.getType(), ServiceType.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx:1433");
        Assert.assertEquals(info.getDatabaseId(), "CAFECHAT");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT");
    }

    @Test
    public void testParse4() throws Exception {
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx:1433";
        ConnectionStringParser parser = new JtdsConnectionStringParser();
        DatabaseInfo info = parser.parse(url);
        logger.debug("{}", info);

        Assert.assertEquals(info.getType(), ServiceType.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx:1433");
        Assert.assertEquals(info.getDatabaseId(), "");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx:1433");
    }


    @Test
    public void testParse5() throws Exception {
//        jdbc:jtds:sqlserver://server[:port][/database][;property=value[;...]]
//        jdbc:jtds:sqlserver://server/db;user=userName;password=password
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx;DatabaseName=CAFECHAT";
        ConnectionStringParser parser = new JtdsConnectionStringParser();
        DatabaseInfo info = parser.parse(url);
        logger.debug("{}", info);

        Assert.assertEquals(info.getType(), ServiceType.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx");
        Assert.assertEquals(info.getDatabaseId(), "CAFECHAT");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx");

    }

    @Test
    public void testParse6() throws Exception {
//        jdbc:jtds:sqlserver://server[:port][/database][;property=value[;...]]
//        jdbc:jtds:sqlserver://server/db;user=userName;password=password
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx";
        ConnectionStringParser parser = new JtdsConnectionStringParser();
        DatabaseInfo info = parser.parse(url);
        logger.debug("{}", info);

        Assert.assertEquals(info.getType(), ServiceType.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx");
        Assert.assertEquals(info.getDatabaseId(), "");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx");

    }


    @Test
    public void testParse7() throws Exception {
        String url = "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT;abc=1;bcd=2";
        ConnectionStringParser parser = new JtdsConnectionStringParser();
        DatabaseInfo info = parser.parse(url);
        logger.debug("{}", info);

        Assert.assertEquals(info.getType(), ServiceType.MSSQL);
        Assert.assertEquals(info.getMultipleHost(), "10.xx.xx.xx:1433");
        Assert.assertEquals(info.getDatabaseId(), "CAFECHAT");
        Assert.assertEquals(info.getUrl(), "jdbc:jtds:sqlserver://10.xx.xx.xx:1433/CAFECHAT");
    }
}