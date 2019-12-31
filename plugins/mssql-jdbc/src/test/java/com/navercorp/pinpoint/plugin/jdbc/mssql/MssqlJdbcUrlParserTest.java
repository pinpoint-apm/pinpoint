package com.navercorp.pinpoint.plugin.jdbc.mssql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Harris Gwag ( gwagdalf )
 */
public class MssqlJdbcUrlParserTest {

  private Logger logger = LoggerFactory.getLogger(MssqlJdbcUrlParserTest.class);
  private MssqlJdbcUrlParser jdbcUrlParser = new MssqlJdbcUrlParser();

  @Test
  public void mssql_jdbc_string_should_be_parsed_1() {
    DatabaseInfo dbInfo = jdbcUrlParser
        .parse("jdbc:sqlserver://ip_address;databaseName=database_name;integratedSecurity=true");
    Assert.assertTrue(dbInfo.isParsingComplete());

    Assert.assertEquals(MssqlConstants.MSSQL_JDBC, dbInfo.getType());
    Assert.assertEquals(("ip_address"), dbInfo.getHost().get(0));
    Assert.assertEquals("database_name", dbInfo.getDatabaseId());
    Assert.assertEquals("jdbc:sqlserver://ip_address;databaseName=database_name", dbInfo.getUrl());
  }

  @Test
  public void mssql_jdbc_string_should_be_parsed_2() {
    DatabaseInfo dbInfo = jdbcUrlParser
        .parse(
            "jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;integratedSecurity=true;applicationName=MyApp");
    Assert.assertTrue(dbInfo.isParsingComplete());

    Assert.assertEquals(MssqlConstants.MSSQL_JDBC, dbInfo.getType());
    Assert.assertEquals(("localhost:1433"), dbInfo.getHost().get(0));
    Assert.assertEquals("AdventureWorks", dbInfo.getDatabaseId());
    Assert.assertEquals("jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks",
        dbInfo.getUrl());
  }

}