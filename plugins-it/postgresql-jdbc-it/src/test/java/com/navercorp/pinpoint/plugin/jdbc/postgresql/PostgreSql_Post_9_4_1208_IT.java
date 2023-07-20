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
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URL;

/**
 * @author HyunGil Jeong
 */
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
//@Dependency({"org.postgresql:postgresql:[42.2.15.jre6]",
//        JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.POSTGRESQL})
@PinpointConfig("pinpoint-postgresql.config")
@Dependency({"org.postgresql:postgresql:[9.4.1208,9.4.1212]",
        JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.POSTGRESQL})
@SharedTestLifeCycleClass(PostgreSqlServer.class)
public class PostgreSql_Post_9_4_1208_IT extends PostgreSqlBase {

    private final Logger logger = LogManager.getLogger(getClass());

    private static PostgreSqlItHelper HELPER;
    private static PostgreSqlJDBCDriverClass driverClass;

    private static PostgreSqlJDBCApi jdbcApi;


    @BeforeAll
    public static void beforeClass() throws Exception {
        invalidJarCheck();

        DriverProperties driverProperties = getDriverProperties();
        driverClass = new PostgreSql_Post_9_4_1208_JDBCDriverClass();
        jdbcApi = new PostgreSqlJDBCApi(driverClass);

        driverClass.getDriver();

        HELPER = new PostgreSqlItHelper(driverProperties);
    }

    private static void invalidJarCheck() {
        ClassLoader classLoader = PostgreSql_Post_9_4_1208_IT.class.getClassLoader();
        // invalid jar : postgresql-42.2.15.jre6
        URL jar = classLoader.getResource("org.postgresql.Driver".replace('.', '/').concat(".class"));
        Assumptions.assumeTrue(jar != null, "test skip : invalid jar ");
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
