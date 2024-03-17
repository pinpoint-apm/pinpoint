package com.navercorp.pinpoint.plugin.jdbc.clickhouse;


import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author intr3p1d
 */
public class ClickHouseJdbcUrlParserTest {

    private final Logger logger = LogManager.getLogger(getClass());
    private final ClickHouseJdbcUrlParser clickHouseJdbcUrlParser = new ClickHouseJdbcUrlParser();

    private static final ServiceType SERVICE_TYPE = ClickHouseConstants.CLICK_HOUSE;

    @Test
    public void clickHouseParse1() {
        final String jdbcUrl = "jdbc:ch://(https://explorer@play.clickhouse.com:443),"
                + "(https://demo:demo@github.demo.trial.altinity.cloud)"
                + "/default?failover=1&load_balancing_policy=random";
        DatabaseInfo dbInfo = clickHouseJdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(SERVICE_TYPE, dbInfo.getType());
        Assertions.assertEquals("(https://explorer@play.clickhouse.com:443)", dbInfo.getHost().get(0));
        Assertions.assertEquals("(https://demo:demo@github.demo.trial.altinity.cloud)", dbInfo.getHost().get(1));
        Assertions.assertEquals("default", dbInfo.getDatabaseId());
        Assertions.assertEquals("jdbc:ch://(https://explorer@play.clickhouse.com:443),"
                + "(https://demo:demo@github.demo.trial.altinity.cloud)/default", dbInfo.getUrl());
    }

    @Test
    public void clickHouseParse2() {
        final String jdbcUrl = "jdbc:clickhouse://(https://explorer@play.clickhouse.com:443),"
                + "(https://demo:demo@github.demo.trial.altinity.cloud)"
                + "/default?failover=1&load_balancing_policy=random";
        DatabaseInfo dbInfo = clickHouseJdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(SERVICE_TYPE, dbInfo.getType());
        Assertions.assertEquals("(https://explorer@play.clickhouse.com:443)", dbInfo.getHost().get(0));
        Assertions.assertEquals("(https://demo:demo@github.demo.trial.altinity.cloud)", dbInfo.getHost().get(1));
        Assertions.assertEquals("default", dbInfo.getDatabaseId());
        Assertions.assertEquals("jdbc:clickhouse://(https://explorer@play.clickhouse.com:443),"
                + "(https://demo:demo@github.demo.trial.altinity.cloud)/default", dbInfo.getUrl());
    }

    @Test
    public void parseFailTest1() {
        DatabaseInfo dbInfo = clickHouseJdbcUrlParser.parse(null);
        Assertions.assertFalse(dbInfo.isParsingComplete());

        Assertions.assertEquals(ServiceType.UNKNOWN_DB, dbInfo.getType());
    }


    @Test
    public void parse() {
        final String jdbcUrl = "jdbc:clickhouse:http://localhost:32773/default";

        DatabaseInfo dbInfo = clickHouseJdbcUrlParser.parse(jdbcUrl);
        Assertions.assertTrue(dbInfo.isParsingComplete());

        Assertions.assertEquals(SERVICE_TYPE, dbInfo.getType());
        Assertions.assertEquals("localhost:32773", dbInfo.getHost().get(0));
        Assertions.assertEquals("default", dbInfo.getDatabaseId());
        Assertions.assertEquals("jdbc:clickhouse:http://localhost:32773/default", dbInfo.getUrl());
    }
}