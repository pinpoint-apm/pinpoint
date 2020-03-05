package com.navercorp.pinpoint.plugin.jdbc.mssql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.common.profiler.trace.TraceMetadataRegistrar;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeLocator;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Harris Gwag ( gwagdalf )
 */
public class MssqlJdbcUrlParserTest {

    public static final short TYPE_CODE = 2250;
    public static final String TYPE_MSSQL_JDBC = "MSSQL_JDBC";

    public static final short EXECUTE_CODE = 2251;
    public static final String EXECUTE_MSSQL_JDBC_QUERY = "MSSQL_JDBC_QUERY";

    private Logger logger = LoggerFactory.getLogger(MssqlJdbcUrlParserTest.class);

    private MssqlJdbcUrlParser jdbcUrlParser = new MssqlJdbcUrlParser();

    @BeforeClass
    public static void beforeClass() throws Exception {
        ServiceTypeLocator serviceTypeLocator = mock(ServiceTypeLocator.class);
        ServiceType type = newServiceType(TYPE_CODE, TYPE_MSSQL_JDBC);
        ServiceType executeQuery = newServiceType(EXECUTE_CODE, EXECUTE_MSSQL_JDBC_QUERY);
        when(serviceTypeLocator.findServiceType(TYPE_CODE)).thenReturn(type);
        when(serviceTypeLocator.findServiceType(EXECUTE_CODE)).thenReturn(executeQuery);
        TraceMetadataRegistrar.registerServiceTypes(serviceTypeLocator);
    }

    private static ServiceType newServiceType(short code, String name) {
        ServiceType serviceType = mock(ServiceType.class);
        when(serviceType.getCode()).thenReturn(code);
        when(serviceType.getName()).thenReturn(name);
        return serviceType;
    }

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
                .parse("jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;integratedSecurity=true;applicationName=MyApp");
        Assert.assertTrue(dbInfo.isParsingComplete());

        Assert.assertEquals(MssqlConstants.MSSQL_JDBC, dbInfo.getType());
        Assert.assertEquals(("localhost:1433"), dbInfo.getHost().get(0));
        Assert.assertEquals("AdventureWorks", dbInfo.getDatabaseId());
        Assert.assertEquals("jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks",
                dbInfo.getUrl());
    }

}