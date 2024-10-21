/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.jdbc;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
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
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.args;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.cachedArgs;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

/**
 * @author yjqg6666
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({ "com.dameng:DmJdbcDriver18:[8.1.1.193,8.max)", JDBCTestConstants.VERSION})
@SharedDependency({"com.dameng:DmJdbcDriver18:8.1.1.193", JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER})
@SharedTestLifeCycleClass(DamengServer.class)
public class Dameng_18_IT extends Dameng_IT_Base {

    private static final JDBCDriverClass driverClass = new Dameng_18_DriverClass();
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

        final String url = getJdbcUrl();
        final String dst = getDestination();

        // DmDriver#connect(String, Properties)
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(DB_TYPE, connect, null, dst, dst, cachedArgs(url)));

        // DmdbStatement#executeQuery(String)
        Method executeQuery = jdbcApi.getStatement().getExecuteQuery();
        verifier.verifyTrace(event(DB_EXECUTE_QUERY, executeQuery, null, dst, dst, sql(STATEMENT_NORMALIZED_QUERY, "1")));
    }

    @Test
    public void testPreparedStatement() throws Exception {
        super.executePreparedStatement();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyTraceCount(3);

        final String url = getJdbcUrl();
        final String dst = getDestination();

        // DmDriver#connect(String, Properties)
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(DB_TYPE, connect, null, dst, dst, cachedArgs(url)));

        // DmdbConnection#prepareStatement(String)
        Method prepareStatement = jdbcApi.getConnection().getPrepareStatement();
        verifier.verifyTrace(event(DB_TYPE, prepareStatement, null, dst, dst, sql(PREPARED_STATEMENT_QUERY, null)));

        // DmdbPreparedStatement#executeQuery
        Method executeQuery = jdbcApi.getPreparedStatement().getExecuteQuery();
        verifier.verifyTrace(event(DB_EXECUTE_QUERY, executeQuery, null, dst, dst, sql(PREPARED_STATEMENT_QUERY, null, "1")));
    }

    @Test
    public void testCallableStatement() throws Exception {
        super.executeCallableStatement();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        verifier.verifyTraceCount(4);

        final String url = getJdbcUrl();
        final String dst = getDestination();

        // DmDriver#connect(String, Properties)
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(DB_TYPE, connect, null, dst, dst, cachedArgs(url)));

        // DmdbConnection#prepareCall(String)
        Method prepareCall = jdbcApi.getConnection().getPrepareCall();
        verifier.verifyTrace(event(DB_TYPE, prepareCall, null, dst, dst, sql(CALLABLE_STATEMENT_QUERY, null)));

        // DmdbCallableStatement#registerOutParameter
        final JDBCApi.CallableStatementClass callableStatement = jdbcApi.getCallableStatement();
        Method registerOutParameter = callableStatement.getRegisterOutParameter();
        verifier.verifyTrace(event(DB_TYPE, registerOutParameter, null, dst, dst, args(3, CALLABLE_STATEMENT_OUTPUT_PARAM_TYPE)));

        // DmdbCallableStatement#executeQuery
        Method executeQuery = jdbcApi.getPreparedStatement().getExecuteQuery();
        verifier.verifyTrace(event(DB_EXECUTE_QUERY, executeQuery, null, dst, dst, sql(CALLABLE_STATEMENT_QUERY, null, CALLABLE_STATEMENT_INPUT_ID + ", " + CALLABLE_STATEMENT_INPUT_VAL)));
    }

}
