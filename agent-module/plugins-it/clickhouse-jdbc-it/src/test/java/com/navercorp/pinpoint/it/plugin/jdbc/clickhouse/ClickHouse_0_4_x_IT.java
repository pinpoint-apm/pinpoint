/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.jdbc.clickhouse;

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCApi;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.plugin.jdbc.clickhouse.ClickHouseJdbcUrlParser;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Properties;


/**
 * @author intr3p1d
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-clickhouse-jdbc-plugin"})
@Dependency({
        "com.clickhouse:clickhouse-jdbc:[0.4.0,]",
        "com.clickhouse:clickhouse-http-client:0.4.1",
        "net.jpountz.lz4:lz4:1.3.0",
        "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5",
})
@PinpointConfig("pinpoint-clickhouse.config")
@SharedDependency({
        "com.clickhouse:clickhouse-jdbc:0.4.0",
        PluginITConstants.VERSION, JDBCTestConstants.VERSION,
        ClickHouseOption.TEST_CONTAINER,
        ClickHouseOption.CLICKHOUSE,
})
@SharedTestLifeCycleClass(ClickHouseServer.class)
public class ClickHouse_0_4_x_IT extends ClickHouseITBase{

    private final Logger logger = LogManager.getLogger(getClass());
    protected static DriverProperties driverProperties = DatabaseContainers.readSystemProperties();

    private static JDBCDriverClass driverClass;
    private static JDBCApi jdbcApi;

    private static JdbcUrlParserV2 jdbcUrlParser;

    public static DriverProperties getDriverProperties() {
        return driverProperties;
    }

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        DriverProperties driverProperties = getDriverProperties();
        driverClass = new ClickHouseJDBCDriverClass();
        jdbcApi = new ClickHouseJDBCApi(driverClass);

        jdbcUrlParser = new ClickHouseJdbcUrlParser();
    }

    @BeforeEach
    public void before() {
        logger.info("before");
        setup(driverProperties, jdbcUrlParser, driverClass, jdbcApi);
    }

    @Test
    public void testStatement() throws SQLException {
        super.testStatements();
    }

    @Test
    public void testPreparedStatement() throws SQLException {
        super.testPreparedStatements();
    }
}
