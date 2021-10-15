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
package com.navercorp.pinpoint.plugin.jdbc.oracle;

import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.navercorp.pinpoint.test.plugin.Repository;
import com.navercorp.pinpoint.test.plugin.shared.BeforeSharedClass;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.Wait;

@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({"com.oracle.database.jdbc:ojdbc6:(,)", JDBCTestConstants.VERSION, OracleITConstants.ORACLE_TESTCONTAINER})
@ImportPlugin("com.navercorp.pinpoint:pinpoint-oracle-jdbc-driver-plugin")
public class Oracle11_Ojdbc6_IT extends Oracle_IT_Base {
    private static final Logger logger = LoggerFactory.getLogger(Oracle11_Ojdbc6_IT.class);

    @BeforeSharedClass
    public static void sharedSetup() throws Exception {
        logger.info("Setting up oracle db...");
        startOracleDB(OracleITConstants.ORACLE_11_X_IMAGE, Wait.defaultWaitStrategy());
    }

    @BeforeClass
    public static void setup() throws Exception {
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
