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

package com.navercorp.pinpoint.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Properties;

/**
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@Dependency({"org.postgresql:postgresql:[9.4.1207,9.4.1208)",
        "log4j:log4j:1.2.16", "org.slf4j:slf4j-log4j12:1.7.5",
        JDBCTestConstants.VERSION})
public class PostgreSql_9_4_1207_IT extends PostgreSqlBase {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSql_9_4_1207_IT.class);
    
    private static PostgreSqlItHelper HELPER;
    private static PostgreSqlJDBCDriverClass driverClass;

    private static PostgreSqlJDBCApi jdbcApi;

    private static final JdbcDatabaseContainer container = PostgreSQLContainerFactory.newContainer(logger);

    @BeforeClass
    public static void beforeClass() throws Exception {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());
        
        container.start();
        
        DriverProperties driverProperties = new DriverProperties(container.getJdbcUrl(), container.getUsername(), container.getPassword(), new Properties());
        driverClass = new PostgreSql_9_4_1207_JDBCDriverClass();
        jdbcApi = new PostgreSqlJDBCApi(driverClass);

        driverClass.getDriver();

        HELPER = new PostgreSqlItHelper(driverProperties);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        container.stop();
    }

    @Override
    protected JDBCDriverClass getJDBCDriverClass() {
        return driverClass;
    }

    @Test
    public void testStatements() throws Exception {
        HELPER.testStatements(jdbcApi);
    }
}
