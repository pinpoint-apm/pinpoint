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
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 * 
 */
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-mysql-jdbc-driver-plugin")
@Dependency({"mysql:mysql-connector-java:[5.min,5.1.9),[5.1.10,5.max]", "log4j:log4j:1.2.16",
        "org.slf4j:slf4j-log4j12:1.7.5", JDBCTestConstants.VERSION, TestcontainersOption.MYSQLDB})
@SharedTestLifeCycleClass(MySqlServer.class)
public class MySql_5_X_IT extends MySql_IT_Base {

    private static MySqlItHelper HELPER;

    private static JDBCDriverClass driverClass;
    private static JDBCApi jdbcApi;

    @BeforeAll
    public static void beforeClass() throws Exception {
        driverClass = new MySql5JDBCDriverClass();
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
