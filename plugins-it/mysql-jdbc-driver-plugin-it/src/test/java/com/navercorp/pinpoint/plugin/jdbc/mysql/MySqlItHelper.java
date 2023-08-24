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

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCApi;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Objects;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.args;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.cachedArgs;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.sql;

/**
 * @author Jongho Moon
 * @author HyunGil Jeong
 */
public class MySqlItHelper {

    private static final String MYSQL = "MYSQL";
    private static final String MYSQL_EXECUTE_QUERY = "MYSQL_EXECUTE_QUERY";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final String jdbcUrl;
    private final String databaseId;
    private final String databasePassword;
    private String databaseAddress;
    private String databaseName;

    MySqlItHelper(DriverProperties driverProperties) {
        Objects.requireNonNull(driverProperties, "driverProperties");

        jdbcUrl = driverProperties.getUrl();

        JdbcUrlParserV2 jdbcUrlParser = new MySqlJdbcUrlParser();
        DatabaseInfo databaseInfo = jdbcUrlParser.parse(jdbcUrl);

        databaseAddress = databaseInfo.getHost().get(0);
        databaseName = databaseInfo.getDatabaseId();

        databaseId = driverProperties.getUser();
        databasePassword = driverProperties.getPassword();
    }


    void testStatements(JDBCApi jdbcApi) throws Exception {

        Class<Driver> driverClass = jdbcApi.getJDBCDriverClass().getDriver();
        final Connection conn = connect(driverClass);

        conn.setAutoCommit(false);

        String insertQuery = "INSERT INTO test (name, age) VALUES (?, ?)";
        String selectQuery = "SELECT * FROM test";
        String deleteQuery = "DELETE FROM test";

        PreparedStatement insert = conn.prepareStatement(insertQuery);
        insert.setString(1, "maru");
        insert.setInt(2, 5);
        insert.execute();

        Statement select = conn.createStatement();
        ResultSet rs = select.executeQuery(selectQuery);

        while (rs.next()) {
            final int id = rs.getInt("id");
            final String name = rs.getString("name");
            final int age = rs.getInt("age");
            logger.debug("id: {}, name: {}, age: {}", id, name, age);
        }

        Statement delete = conn.createStatement();
        delete.executeUpdate(deleteQuery);

        conn.commit();
        conn.close();

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();

        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(MYSQL, connect, null, databaseAddress, databaseName, cachedArgs(jdbcUrl)));

        final JDBCApi.ConnectionClass connectionClass = jdbcApi.getConnection();
        Method setAutoCommit = connectionClass.getSetAutoCommit();
        verifier.verifyTrace(event(MYSQL, setAutoCommit, null, databaseAddress, databaseName, args(false)));

        Method prepareStatement = connectionClass.getPrepareStatement();
        verifier.verifyTrace(event(MYSQL, prepareStatement, null, databaseAddress, databaseName, sql(insertQuery, null)));

        final JDBCApi.PreparedStatementClass preparedStatementClass = jdbcApi.getPreparedStatement();
        Method execute = preparedStatementClass.getExecute();
        verifier.verifyTrace(event(MYSQL_EXECUTE_QUERY, execute, null, databaseAddress, databaseName, Expectations.sql(insertQuery, null, "maru, 5")));

        final JDBCApi.StatementClass statementClass = jdbcApi.getStatement();
        Method executeQuery = statementClass.getExecuteQuery();
        verifier.verifyTrace(event(MYSQL_EXECUTE_QUERY, executeQuery, null, databaseAddress, databaseName, Expectations.sql(selectQuery, null)));

        Method executeUpdate = statementClass.getExecuteUpdate();
        verifier.verifyTrace(event(MYSQL_EXECUTE_QUERY, executeUpdate, null, databaseAddress, databaseName, Expectations.sql(deleteQuery, null)));

        Method commit = connectionClass.getCommit();
        verifier.verifyTrace(event(MYSQL, commit, null, databaseAddress, databaseName));
    }

    private Connection connect(Class<Driver> driverClass) throws Exception {
        return DriverManager.getConnection(jdbcUrl, databaseId, databasePassword);
    }

    /*  CREATE OR REPLACE PROCEDURE concatCharacters(IN  a CHAR(1), IN  b CHAR(1), OUT c CHAR(2))
        BEGIN
            SET c = CONCAT(a, b);
        END                                             */
    void testStoredProcedure_with_IN_OUT_parameters(JDBCApi jdbcApi) throws Exception {
        final String param1 = "a";
        final String param2 = "b";
        final String storedProcedureQuery = "{ call concatCharacters(?, ?, ?) }";

        final Class<Driver> driverClass = jdbcApi.getJDBCDriverClass().getDriver();
        final Connection conn = connect(driverClass);

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setString(1, param1);
        cs.setString(2, param2);
        cs.registerOutParameter(3, Types.VARCHAR);
        cs.execute();

        Assertions.assertEquals(param1.concat(param2), cs.getString(3));

        conn.close();
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(4);

        // NonRegisteringDriver#connect(String, Properties)
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(MYSQL, connect, null, databaseAddress, databaseName, cachedArgs(jdbcUrl)));

        // Connection#prepareCall(String)
        final JDBCApi.ConnectionClass connectionClass = jdbcApi.getConnection();
        Method prepareCall = connectionClass.getPrepareCall();
        verifier.verifyTrace(event(MYSQL, prepareCall, null, databaseAddress, databaseName, sql(storedProcedureQuery, null)));

        // CallableStatement#registerOutParameter(int, int)
        final JDBCApi.CallableStatementClass callableStatementClass = jdbcApi.getCallableStatement();
        Method registerOutParameter = callableStatementClass.getRegisterOutParameter();
        verifier.verifyTrace(event(MYSQL, registerOutParameter, null, databaseAddress, databaseName, args(3, Types.VARCHAR)));

        // CallableStatement#execute
        Method execute = callableStatementClass.getExecute();
        verifier.verifyTrace(event(MYSQL_EXECUTE_QUERY, execute, null, databaseAddress, databaseName, Expectations.sql(storedProcedureQuery, null, param1 + ", " + param2)));
    }

    /*
        CREATE OR REPLACE PROCEDURE swapAndGetSum(INOUT a INT, INOUT b INT)
        BEGIN
            DECLARE temp INT;
            SET temp = a;
            SET a = b;
            SET b = temp;
            SELECT temp + a;
        END
     */
    void testStoredProcedure_with_INOUT_parameters(JDBCApi jdbcApi) throws Exception {
        final int param1 = 1;
        final int param2 = 2;
        final String storedProcedureQuery = "{ call swapAndGetSum(?, ?) }";

        final Class<Driver> driverClass = jdbcApi.getJDBCDriverClass().getDriver();
        final Connection conn = connect(driverClass);

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setInt(1, param1);
        cs.setInt(2, param2);
        cs.registerOutParameter(1, Types.INTEGER);
        cs.registerOutParameter(2, Types.INTEGER);
        ResultSet rs = cs.executeQuery();

        Assertions.assertTrue(rs.next());
        Assertions.assertEquals(param1 + param2, rs.getInt(1));
        Assertions.assertEquals(param2, cs.getInt(1));
        Assertions.assertEquals(param1, cs.getInt(2));

        conn.close();
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(5);

        // NonRegisteringDriver#connect(String, Properties)
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(event(MYSQL, connect, null, databaseAddress, databaseName, cachedArgs(jdbcUrl)));

        // NonRegisteringDriver#connect(String, Properties)
        final JDBCApi.ConnectionClass connectionClass = jdbcApi.getConnection();
        Method prepareCall = connectionClass.getPrepareCall();
        verifier.verifyTrace(event(MYSQL, prepareCall, null, databaseAddress, databaseName, sql(storedProcedureQuery, null)));

        // CallableStatement#registerOutParameter(int, int)
        final JDBCApi.CallableStatementClass callableStatementClass = jdbcApi.getCallableStatement();
        Method registerOutParameter = callableStatementClass.getRegisterOutParameter();
        // param 1
        verifier.verifyTrace(event(MYSQL, registerOutParameter, null, databaseAddress, databaseName, args(1, Types.INTEGER)));
        // param 2
        verifier.verifyTrace(event(MYSQL, registerOutParameter, null, databaseAddress, databaseName, args(2, Types.INTEGER)));

        // CallableStatement#execute
        Method execute = callableStatementClass.getExecuteQuery();
        verifier.verifyTrace(event(MYSQL_EXECUTE_QUERY, execute, null, databaseAddress, databaseName, Expectations.sql(storedProcedureQuery, null, param1 + ", " + param2)));
    }
}
