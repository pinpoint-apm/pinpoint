/*
 * Copyright 2020 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.jdbc.oracle;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"com.oracle.database.jdbc:ojdbc8:[19.9,)", PluginITConstants.VERSION, JDBCTestConstants.VERSION, OracleITConstants.ORACLE_TESTCONTAINER})
@SharedDependency({"com.oracle.database.jdbc:ojdbc8:19.9.0.0", PluginITConstants.VERSION, JDBCTestConstants.VERSION, OracleITConstants.ORACLE_TESTCONTAINER})
@SharedTestLifeCycleClass(OracleServer19x.class)
public class Oracle19_Ojdbc8_IT extends Oracle_IT_Base {
    private final Logger logger = LogManager.getLogger(Oracle19_Ojdbc8_IT.class);

    @BeforeAll
    public static void setup() {
        DriverProperties driverProperties = createDriverProperties();
        helper = new OracleItHelper(driverProperties);
    }

    @Test
    public void test() throws Exception {
        String insertQuery = "INSERT INTO test (name, age) VALUES (?, ?)";
        String selectQuery = "SELECT * FROM test";
        String deleteQuery = "DELETE FROM test";

        helper.testStatement(JDBC_API, insertQuery, selectQuery, deleteQuery);
        helper.verifyTestStatement(JDBC_API, insertQuery, selectQuery, deleteQuery);
    }

    @Test
    public void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        final String param1 = "a";
        final String param2 = "b";
        final String storedProcedureQuery = "{ call concatCharacters(?, ?, ?) }";

        helper.testStoredProcedure_with_IN_OUT_parameters(JDBC_API, param1, param2, storedProcedureQuery);
        helper.verifyTestStoredProcedure_with_IN_OUT_parameters(JDBC_API, param1, param2, storedProcedureQuery);
   }

    @Test
    public void testStoredProcedure_with_INOUT_parameters() throws Exception {
        final int param1 = 1;
        final int param2 = 2;
        final String storedProcedureQuery = "{ call swapAndGetSum(?, ?, ?) }";

        helper.testStoredProcedure_with_INOUT_parameters(JDBC_API, param1, param2, storedProcedureQuery);
        helper.verifyTestStoredProcedure_with_INOUT_parameters(JDBC_API, param1, param2, storedProcedureQuery);
    }

}
