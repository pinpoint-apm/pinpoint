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

package com.navercorp.pinpoint.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.jdbc.DefaultJDBCApi;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCApi;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.args;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.cachedArgs;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

/**
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(7)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-mariadb-jdbc-driver-plugin")
@Dependency({ "org.mariadb.jdbc:mariadb-java-client:[1.4.min,1.6.min)", "ch.vorburger.mariaDB4j:mariaDB4j:2.2.2",
        JDBCTestConstants.VERSION})
public class MariaDB_1_4_x_to_1_6_0_IT extends MariaDB_IT_Base {

    // see CallableParameterMetaData#queryMetaInfos
    private static final String CALLABLE_QUERY_META_INFOS_QUERY = "select param_list, returns, db, type from mysql.proc where db=DATABASE() and name=?";

    private static final JDBCDriverClass driverClass = new MariaDB_1_4_x_DriverClass();
    private static final JDBCApi jdbcApi = new DefaultJDBCApi(driverClass);

    @Override
    public JDBCDriverClass getJDBCDriverClass() {
        return driverClass;
    }
    
    @Test
    public void testStatement() throws Exception {
        super.executeStatement();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyTraceCount(2);

        // Driver#connect(String, Properties)
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(DB_TYPE, connect, null, URL, DATABASE_NAME, cachedArgs(JDBC_URL)));

        // MariaDbStatement#executeQuery(String)
        Method executeQuery = jdbcApi.getStatement().getExecuteQuery();
        verifier.verifyTrace(event(DB_EXECUTE_QUERY, executeQuery, null, URL, DATABASE_NAME, sql(STATEMENT_NORMALIZED_QUERY, "1")));
    }

    @Test
    public void testPreparedStatement() throws Exception {
        super.executePreparedStatement();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyTraceCount(3);

        // Driver#connect(String, Properties)
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(DB_TYPE, connect, null, URL, DATABASE_NAME, cachedArgs(JDBC_URL)));

        // MariaDbConnection#prepareStatement(String)
        Method prepareStatement = jdbcApi.getConnection().getPrepareStatement();
        verifier.verifyTrace(event(DB_TYPE, prepareStatement, null, URL, DATABASE_NAME, sql(PREPARED_STATEMENT_QUERY, null)));

        // MariaDbServerPreparedStatement#executeQuery
        Method executeQuery = jdbcApi.getPreparedStatement().getExecuteQuery();
        verifier.verifyTrace(event(DB_EXECUTE_QUERY, executeQuery, null, URL, DATABASE_NAME, sql(PREPARED_STATEMENT_QUERY, null, "3")));
    }

    @Test
    public void testCallableStatement() throws Exception {
        super.executeCallableStatement();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyTraceCount(6);

        // Driver#connect(String, Properties)
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(DB_TYPE, connect, null, URL, DATABASE_NAME, cachedArgs(JDBC_URL)));

        // MariaDbConnection#prepareCall(String)
        Method prepareCall = jdbcApi.getConnection().getPrepareCall();
        verifier.verifyTrace(event(DB_TYPE, prepareCall, null, URL, DATABASE_NAME, sql(CALLABLE_STATEMENT_QUERY, null)));

        // AbstractCallableProcedureStatement#registerOutParameter
        final JDBCApi.CallableStatementClass callableStatement = jdbcApi.getCallableStatement();
        Method registerOutParameter = callableStatement.getRegisterOutParameter();
        verifier.verifyTrace(event(DB_TYPE, registerOutParameter, null, URL, DATABASE_NAME, args(2, CALLABLE_STATMENT_OUTPUT_PARAM_TYPE)));

        // MariaDbServerPreparedStatement#executeQuery
        Method executeQuery = jdbcApi.getPreparedStatement().getExecuteQuery();
        verifier.verifyTrace(event(DB_EXECUTE_QUERY, executeQuery, null, URL, DATABASE_NAME, sql(CALLABLE_STATEMENT_QUERY, null, CALLABLE_STATEMENT_INPUT_PARAM)));

        // MariaDbConnection#prepareStatement(String)
        Method prepareStatement = jdbcApi.getConnection().getPrepareStatement();
        verifier.verifyTrace(event(DB_TYPE, prepareStatement, null, URL, DATABASE_NAME, sql(CALLABLE_QUERY_META_INFOS_QUERY, null)));

        // MariaDbServerPreparedStatement#executeQuery
        Method executeQueryClient = jdbcApi.getPreparedStatement().getExecuteQuery();
        verifier.verifyTrace(event(DB_EXECUTE_QUERY, executeQueryClient, null, URL, DATABASE_NAME, sql(CALLABLE_QUERY_META_INFOS_QUERY, null, PROCEDURE_NAME)));
    }
}
