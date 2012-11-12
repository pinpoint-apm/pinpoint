package com.profiler.modifier.db.util;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

/**
 *
 */
public class ConnectionStringParserTest {

    private Logger logger = LoggerFactory.getLogger(ConnectionStringParserTest.class);

    @Test
    public void testURIParse() throws Exception {

        URI uri = URI.create("mysql:replication://10.98.133.22:3306/test_lucy_db");
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
}
