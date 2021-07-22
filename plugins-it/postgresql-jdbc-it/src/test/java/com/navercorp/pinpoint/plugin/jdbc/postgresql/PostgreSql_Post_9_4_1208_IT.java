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
import com.navercorp.pinpoint.pluginit.utils.TestcontainersOption;
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


import java.net.URL;
import java.util.Properties;

/**
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
//@Dependency({"org.postgresql:postgresql:[42.2.15.jre6]",
//        JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.POSTGRESQL})
@Dependency({"org.postgresql:postgresql:[9.4.1208,)",
        JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.POSTGRESQL})
public class PostgreSql_Post_9_4_1208_IT extends PostgreSqlBase {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSql_Post_9_4_1208_IT.class);
    
    private static PostgreSqlItHelper HELPER;
    private static PostgreSqlJDBCDriverClass driverClass;

    private static PostgreSqlJDBCApi jdbcApi;

    private static JdbcDatabaseContainer container;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());
        invalidJarCheck();

        container = PostgreSQLContainerFactory.newContainer(logger);
        container.start();

        DriverProperties driverProperties = new DriverProperties(container.getJdbcUrl(), container.getUsername(), container.getPassword(), new Properties());
        driverClass = new PostgreSql_Post_9_4_1208_JDBCDriverClass();
        jdbcApi = new PostgreSqlJDBCApi(driverClass);

        driverClass.getDriver();

        HELPER = new PostgreSqlItHelper(driverProperties);
    }

    private static void invalidJarCheck() {
        ClassLoader classLoader = PostgreSql_Post_9_4_1208_IT.class.getClassLoader();
        // invalid jar : postgresql-42.2.15.jre6
        URL jar = classLoader.getResource("org.postgresql.Driver".replace('.', '/').concat(".class"));
        Assume.assumeTrue("test skip : invalid jar ", jar != null);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if (container != null) {
            container.stop();
        }
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
