/*
 * Copyright 2019 NAVER Corp.
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
 * <p>Notable class changes :<br/>
 * <ul>
 *     <li><tt>org.mariadb.jdbc.MariaDbPreparedStatementServer</tt> -> <tt>org.mariadb.jdbc.ServerSidePreparedStatement</tt></li>
 *     <li><tt>org.mariadb.jdbc.MariaDbPreparedStatementClient</tt> -> <tt>org.mariadb.jdbc.ClientSidePreparedStatement</tt></li>
 * </ul>
 * </p>
 *
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8) // 2.x+ requires Java 8
@Dependency({ "org.mariadb.jdbc:mariadb-java-client:[2.4.0,)", "ch.vorburger.mariaDB4j:mariaDB4j:2.2.2",
        JDBCTestConstants.VERSION})
@ImportPlugin("com.navercorp.pinpoint:pinpoint-mariadb-jdbc-driver-plugin")
public class MariaDB_2_4_x_IT extends MariaDB_IT_Base {

    // see CallableParameterMetaData#queryMetaInfos
    private  static final String CALLABLE_QUERY_META_INFOS_QUERY = "select param_list, returns, db, type from mysql.proc where name=? and db=DATABASE()";


    private static final JDBCDriverClass driverClass = newDriverClass(PreparedStatementType.Server);
    private static final JDBCApi jdbcApi = new DefaultJDBCApi(driverClass);

    private static final JDBCDriverClass clientDriverClass = newDriverClass(PreparedStatementType.Client);
    private static final JDBCApi clientJdbcApi = new DefaultJDBCApi(clientDriverClass);

    private static JDBCDriverClass newDriverClass(PreparedStatementType server) {
        return new MariaDB_2_4_x_DriverClass(server);
    }

    @Override
    public JDBCDriverClass getJDBCDriverClass() {
        return driverClass;
    }

    @Test
    public void testStatement() throws Exception {
        super.executeStatement();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

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
        final JDBCApi jdbcMethod = clientJdbcApi;

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyTraceCount(3);

        // Driver#connect(String, Properties)
        Method connect = jdbcMethod.getDriver().getConnect();
        verifier.verifyTrace(event(DB_TYPE, connect, null, URL, DATABASE_NAME, cachedArgs(JDBC_URL)));

        // MariaDbConnection#prepareStatement(String)
        Method prepareStatement = jdbcMethod.getConnection().getPrepareStatement();
        verifier.verifyTrace(event(DB_TYPE, prepareStatement, null, URL, DATABASE_NAME, sql(PREPARED_STATEMENT_QUERY, null)));

        // ClientSidePreparedStatement#executeQuery
        Method executeQuery = jdbcMethod.getPreparedStatement().getExecuteQuery();
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

        // CallableProcedureStatement#registerOutParameter
        final JDBCApi.CallableStatementClass callableStatement = jdbcApi.getCallableStatement();
        Method registerOutParameter = callableStatement.getRegisterOutParameter();
        verifier.verifyTrace(event(DB_TYPE, registerOutParameter, null, URL, DATABASE_NAME, args(2, CALLABLE_STATMENT_OUTPUT_PARAM_TYPE)));

        // ServerSidePreparedStatement#executeQuery
        Method executeQuery = jdbcApi.getPreparedStatement().getExecuteQuery();
        verifier.verifyTrace(event(DB_EXECUTE_QUERY, executeQuery, null, URL, DATABASE_NAME, sql(CALLABLE_STATEMENT_QUERY, null, CALLABLE_STATEMENT_INPUT_PARAM)));

        // MariaDbConnection#prepareStatement(String)
        Method prepareStatement = jdbcApi.getConnection().getPrepareStatement();
        verifier.verifyTrace(event(DB_TYPE, prepareStatement, null, URL, DATABASE_NAME, sql(CALLABLE_QUERY_META_INFOS_QUERY, null)));

        // ClientSidePreparedStatement#executeQuery
        Method executeQueryClient = clientJdbcApi.getPreparedStatement().getExecuteQuery();
        verifier.verifyTrace(event(DB_EXECUTE_QUERY, executeQueryClient, null, URL, DATABASE_NAME, sql(CALLABLE_QUERY_META_INFOS_QUERY, null, PROCEDURE_NAME)));
    }
}
