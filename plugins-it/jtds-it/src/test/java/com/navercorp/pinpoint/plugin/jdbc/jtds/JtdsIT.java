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
package com.navercorp.pinpoint.plugin.jdbc.jtds;

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.pluginit.jdbc.DataBaseTestCase;
import com.navercorp.pinpoint.pluginit.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCApi;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import com.navercorp.pinpoint.test.plugin.shared.AfterSharedClass;
import com.navercorp.pinpoint.test.plugin.shared.BeforeSharedClass;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.output.OutputFrame;

import java.io.IOException;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * @author Jongho Moon
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Repository("http://repo.navercorp.com/maven2")
@ImportPlugin("com.navercorp.pinpoint:pinpoint-jtds-plugin")
@Dependency({"net.sourceforge.jtds:jtds:[1.2.8],[1.3.1,)", "com.microsoft.sqlserver:mssql-jdbc:[6.1.0.jre8]",
        "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5",
        JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.MSSQL})
public class JtdsIT extends DataBaseTestCase {
    private static final String MSSQL = "MSSQL";
    private static final String MSSQL_EXECUTE_QUERY = "MSSQL_EXECUTE_QUERY";

    private static final Logger logger = LoggerFactory.getLogger(JtdsIT.class);


    public static final JdbcDatabaseContainer mssqlserver = newMSSQLServerContainer(logger.getName());

    public static JdbcDatabaseContainer newMSSQLServerContainer(String loggerName) {
        final MSSQLServerContainer mssqlServerContainer = new MSSQLServerContainer("mcr.microsoft.com/mssql/server:2019-latest");
        mssqlServerContainer.addEnv("ACCEPT_EULA", "y");
        mssqlServerContainer.withInitScript("sql/init_mssql.sql");
        mssqlServerContainer.withPassword(JtdsITConstants.PASSWORD);


        mssqlServerContainer.withLogConsumer(new Consumer<OutputFrame>() {
            @Override
            public void accept(OutputFrame outputFrame) {
                logger.info(outputFrame.getUtf8String());
            }
        });
        return mssqlServerContainer;
    }

    private static DriverProperties driverProperties;
    private static JDBCDriverClass driverClass;
    private static JDBCApi jdbcApi;

    private static JdbcUrlParserV2 jdbcUrlParser;

    private static String address;

    @BeforeSharedClass
    public static void sharedSetUp() throws Exception {
        mssqlserver.start();
        setAddress(mssqlserver.getJdbcUrl());

    }

    @AfterSharedClass
    public static void sharedTearDown() {
        if (mssqlserver != null) {
            mssqlserver.stop();
        }
    }

    @BeforeClass
    public static void beforeClass() throws IOException, InterruptedException {
        String serverJdbcUrl = getAddress();
        String address = serverJdbcUrl.substring(JtdsITConstants.JDBC_URL_PREFIX.length());
        String jdbcUrl = JtdsITConstants.JTDS_URL_PREFIX + address;

        driverProperties = new DriverProperties(jdbcUrl, JtdsITConstants.USER_NAME, JtdsITConstants.PASSWORD, new Properties());

        driverClass = new JtdsJDBCDriverClass();
        jdbcApi = new DefaultJDBCApi(driverClass);
        // load jdbc driver
        driverClass.getDriver();

        jdbcUrlParser = new JtdsJdbcUrlParser();
    }

    @Override
    protected JDBCDriverClass getJDBCDriverClass() {
        return driverClass;
    }

    @Before
    public void before() {
        setup(MSSQL, MSSQL_EXECUTE_QUERY, driverProperties, jdbcUrlParser, jdbcApi);
    }

    public static String getAddress() {
        return address;
    }

    public static void setAddress(String address) {
        JtdsIT.address = address;
    }

}
