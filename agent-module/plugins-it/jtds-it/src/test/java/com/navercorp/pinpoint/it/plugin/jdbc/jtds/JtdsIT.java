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
package com.navercorp.pinpoint.it.plugin.jdbc.jtds;

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DataBaseTestCase;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCApi;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.plugin.jdbc.jtds.JtdsJdbcUrlParser;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.Repository;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.Properties;

/**
 * @author Jongho Moon
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Repository("http://repo.navercorp.com/maven2")
@Dependency({"net.sourceforge.jtds:jtds:[1.2.8],[1.3.1,)", "com.microsoft.sqlserver:mssql-jdbc:[6.1.0.jre8]",
        PluginITConstants.VERSION, JDBCTestConstants.VERSION})
@SharedDependency({"com.microsoft.sqlserver:mssql-jdbc:[6.1.0.jre8]", PluginITConstants.VERSION, JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.MSSQL})
@SharedTestLifeCycleClass(MsSqlServer.class)
public class JtdsIT extends DataBaseTestCase {
    private static final String MSSQL = "MSSQL";
    private static final String MSSQL_EXECUTE_QUERY = "MSSQL_EXECUTE_QUERY";

    private static final DriverProperties driverProperties = DatabaseContainers.readSystemProperties();
    private static DriverProperties jtdsDriverProperties;
    private static JDBCDriverClass driverClass;
    private static JDBCApi jdbcApi;

    private static JdbcUrlParserV2 jdbcUrlParser;


    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }


    @BeforeAll
    public static void beforeClass() {
        String serverJdbcUrl = driverProperties.getUrl();
        String address = serverJdbcUrl.substring(JtdsITConstants.JDBC_URL_PREFIX.length());
        String jdbcUrl = JtdsITConstants.JTDS_URL_PREFIX + address;

        jtdsDriverProperties = new DriverProperties(jdbcUrl, driverProperties.getUser(), driverProperties.getPassword(), new Properties());

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

    @BeforeEach
    public void before() {
        setup(MSSQL, MSSQL_EXECUTE_QUERY, jtdsDriverProperties, jdbcUrlParser, jdbcApi);
    }

}
