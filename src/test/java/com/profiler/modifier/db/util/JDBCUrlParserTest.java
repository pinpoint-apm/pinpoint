package com.profiler.modifier.db.util;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 *
 */
public class JDBCUrlParserTest {

    private Logger logger = LoggerFactory.getLogger(JDBCUrlParserTest.class);
    private JDBCUrlParser JDBCUrlParser = new JDBCUrlParser();

    @Test
    public void testURIParse() throws Exception {

        URI uri = URI.create("jdbc:mysql:replication://10.98.133.22:3306/test_lucy_db");
        logger.debug(uri.toString());
        logger.debug(uri.getScheme());

        // URI로 파싱하는건 제한적임 한계가 있음.
        try {
            URI oracleRac = URI.create("jdbc:oracle:thin:@(DESCRIPTION=(LOAD_BALANCE=on)" +
                    "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.4) (PORT=1521))" +
                    "(ADDRESS=(PROTOCOL=TCP)(HOST=1.2.3.5) (PORT=1521))" +
                    "(CONNECT_DATA=(SERVICE_NAME=service)))");

            logger.debug(oracleRac.toString());
            logger.debug(oracleRac.getScheme());
            Assert.fail();
        } catch (Exception e) {
        }
    }

    @Test
    public void mysqlParse1() {

        DatabaseInfo dbInfo = JDBCUrlParser.parse("jdbc:mysql://ip_address:3306/database_name?useUnicode=yes&amp;characterEncoding=UTF-8");
        Assert.assertEquals(dbInfo.getType(), DatabaseInfo.DBType.MYSQL);
        Assert.assertEquals(dbInfo.getHost(), "ip_address");
        Assert.assertEquals(dbInfo.getPort(), "3306");
        Assert.assertEquals(dbInfo.getDatabaseId(), "database_name");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://ip_address:3306/database_name");
    }

    @Test
    public void mysqlParse2() {

        DatabaseInfo dbInfo = JDBCUrlParser.parse("jdbc:mysql://10.98.133.22:3306/test_lucy_db");
        Assert.assertEquals(dbInfo.getType(), DatabaseInfo.DBType.MYSQL);
        Assert.assertEquals(dbInfo.getHost(), "10.98.133.22");
        Assert.assertEquals(dbInfo.getPort(), "3306");
        Assert.assertEquals(dbInfo.getDatabaseId(), "test_lucy_db");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://10.98.133.22:3306/test_lucy_db");
    }

    @Test
    public void mysqlParse3() {
        DatabaseInfo dbInfo = JDBCUrlParser.parse("jdbc:mysql://61.74.71.31/log?useUnicode=yes&amp;characterEncoding=UTF-8");
        Assert.assertEquals(dbInfo.getType(), DatabaseInfo.DBType.MYSQL);
        Assert.assertEquals(dbInfo.getHost(), "61.74.71.31");
        Assert.assertEquals(dbInfo.getPort(), "");
        Assert.assertEquals(dbInfo.getDatabaseId(), "log");
        Assert.assertEquals(dbInfo.getUrl(), "jdbc:mysql://61.74.71.31/log");
    }
}
