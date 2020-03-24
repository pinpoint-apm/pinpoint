/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.jdbc.mssql;

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.pluginit.jdbc.*;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.utility.DockerMachineClient;

import java.util.Properties;

/**
 * https://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc
 * [8.1.1.jre8] not exist
 * @author Woonduk Kang(emeroad)
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({"com.microsoft.sqlserver:mssql-jdbc:[6.1.0.jre8],[6.2.0.jre8],[6.4.0.jre8],[7.0.0.jre8],[7.2.0.jre8],[7.4.0.jre8]",
        "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5",
        JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.MSSQL})
@JvmVersion({8})
@PinpointConfig("pinpoint-mssql.config")
public class MSSqlIT extends DataBaseTestCase {

    private static final String MSSQL = "MSSQL_JDBC";
    private static final String MSSQL_EXECUTE_QUERY = "MSSQL_JDBC_QUERY";

    private static final Logger logger = LoggerFactory.getLogger(MSSqlIT.class);

    private static DriverProperties driverProperties;
    private static JDBCDriverClass driverClass;
    private static JDBCApi jdbcApi;

    private static JdbcUrlParserV2 jdbcUrlParser;
    public static final MSSQLServerContainer mssqlserver = MSSQLServerContainerFactory.newMSSQLServerContainer(logger);

    @BeforeClass
    public static void setup() {
        Assume.assumeTrue("Docker not enabled", DockerMachineClient.instance().isInstalled());
        mssqlserver.start();

        driverProperties = new DriverProperties(mssqlserver.getJdbcUrl(), mssqlserver.getUsername(), mssqlserver.getPassword(), new Properties());
        driverClass = new MSSqlJDBCDriverClass();
        jdbcApi = new DefaultJDBCApi(driverClass);

        jdbcUrlParser = new MssqlJdbcUrlParser();
    }

    @Override
    protected JDBCDriverClass getJDBCDriverClass() {
        return driverClass;
    }

    @Before
    public void before() {
        logger.info("before");
        setup(MSSQL, MSSQL_EXECUTE_QUERY, driverProperties, jdbcUrlParser, jdbcApi);
    }

    @Override
    public void testStatement() throws Exception {
        super.testStatement();
    }

    @Test
    @Override
    public void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        super.testStoredProcedure_with_IN_OUT_parameters();
    }

    @Override
    public void testStoredProcedure_with_INOUT_parameters() throws Exception {
        super.testStoredProcedure_with_INOUT_parameters();
    }
}
