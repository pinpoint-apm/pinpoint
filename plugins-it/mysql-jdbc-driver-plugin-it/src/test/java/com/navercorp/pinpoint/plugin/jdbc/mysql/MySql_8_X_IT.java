/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jdbc.mysql;

import com.navercorp.pinpoint.pluginit.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCApi;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.PinpointProfile;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 * 
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@PinpointProfile("test")
@ImportPlugin("com.navercorp.pinpoint:pinpoint-mysql-jdbc-driver-plugin")
@Dependency({"mysql:mysql-connector-java:[8.0.11,)", "log4j:log4j:1.2.16",
        "org.slf4j:slf4j-log4j12:1.7.5", JDBCTestConstants.VERSION, TestcontainersOption.MYSQLDB})
public class MySql_8_X_IT extends MySql_IT_Base {

    private static MySqlItHelper HELPER;

    private static JDBCDriverClass driverClass;
    private static JDBCApi jdbcApi;

    @BeforeClass
    public static void beforeClass() throws Exception {
        driverClass = new MySql8JDBCDriverClass();
        jdbcApi = new DefaultJDBCApi(driverClass);

        driverClass.getDriver();
        HELPER = new MySqlItHelper(getDriverProperties());
    }

    @Override
    protected JDBCDriverClass getJDBCDriverClass() {
        return driverClass;
    }


    @Test
    public void testStatements() throws Exception {
        HELPER.testStatements(jdbcApi);
    }

    @Test
    public void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        HELPER.testStoredProcedure_with_IN_OUT_parameters(jdbcApi);
    }

    @Test
    public void testStoredProcedure_with_INOUT_parameters() throws Exception {
        HELPER.testStoredProcedure_with_INOUT_parameters(jdbcApi);
    }
}
