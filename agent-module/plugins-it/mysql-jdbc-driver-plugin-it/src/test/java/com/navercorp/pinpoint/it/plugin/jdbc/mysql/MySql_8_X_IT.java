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
package com.navercorp.pinpoint.it.plugin.jdbc.mysql;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCApi;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 * 
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"mysql:mysql-connector-java:[8.0.11,)", "log4j:log4j:1.2.16",
        "org.slf4j:slf4j-log4j12:1.7.5", JDBCTestConstants.VERSION})
@SharedDependency({"mysql:mysql-connector-java:8.0.28", JDBCTestConstants.VERSION, TestcontainersOption.MYSQLDB})
@SharedTestLifeCycleClass(MySqlServer8.class)
public class MySql_8_X_IT extends MySql_IT_Base {

    private static MySqlItHelper HELPER;

    private static JDBCDriverClass driverClass;
    private static JDBCApi jdbcApi;

    @BeforeAll
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
