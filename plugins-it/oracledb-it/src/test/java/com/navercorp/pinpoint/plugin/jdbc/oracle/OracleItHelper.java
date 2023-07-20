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
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.CallableStatement;
import java.sql.Types;
import java.sql.SQLException;
import java.util.Objects;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

public class OracleItHelper {
    protected static final String ORACLE = "ORACLE";
    protected static final String ORACLE_EXECUTE_QUERY = "ORACLE_EXECUTE_QUERY";

    public static String DB_ADDRESS;
    public static String DB_NAME;
    private static String DB_ID;
    private static String DB_PASSWORD;
    public static String JDBC_URL;

    private final Logger logger = LogManager.getLogger(this.getClass());

    OracleItHelper(DriverProperties driverProperties) {
        Objects.requireNonNull(driverProperties, "driverProperties");

        JDBC_URL = driverProperties.getUrl();

        JdbcUrlParserV2 jdbcUrlParser = new OracleJdbcUrlParser();
        DatabaseInfo databaseInfo = jdbcUrlParser.parse(JDBC_URL);

        DB_ADDRESS = databaseInfo.getHost().get(0);
        DB_NAME = databaseInfo.getDatabaseId();

        DB_ID = driverProperties.getUser();
        DB_PASSWORD = driverProperties.getPassword();
    }

    private Connection connect(Class<Driver> driverClass) throws SQLException {
        logger.info("Connecting to Oracle Database url: {}, id: {}, pw: {}", JDBC_URL, DB_ID, DB_PASSWORD);
        return DriverManager.getConnection(JDBC_URL, DB_ID, DB_PASSWORD);
    }

    public void create(JDBCApi jdbcApi) throws Exception {
        Class<Driver> driverClass = jdbcApi.getJDBCDriverClass().getDriver();
        final Connection conn = connect(driverClass);

        conn.setAutoCommit(false);

        Statement statement = conn.createStatement();
        statement.execute("CREATE TABLE test (id INTEGER NOT NULL, name VARCHAR(45) NOT NULL, age INTEGER NOT NULL, CONSTRAINT test_pk PRIMARY KEY (id))");
        statement.execute("CREATE SEQUENCE test_seq");
        statement.execute("CREATE OR REPLACE TRIGGER test_trigger BEFORE INSERT ON test FOR EACH ROW BEGIN SELECT test_seq.nextval INTO :new.id FROM dual; END;");
        statement.execute("CREATE OR REPLACE PROCEDURE concatCharacters(a IN VARCHAR2, b IN VARCHAR2, c OUT VARCHAR2) AS BEGIN c := a || b; END concatCharacters;");
        statement.execute("CREATE OR REPLACE PROCEDURE swapAndGetSum(a IN OUT NUMBER, b IN OUT NUMBER, c OUT NUMBER) IS BEGIN c := a; a := b; b := c; SELECT c + a INTO c FROM DUAL; END swapAndGetSum;");
        statement.close();
        conn.commit();
        conn.close();
    }

    public void testStatement(JDBCApi jdbcApi, String insertQuery, String selectQuery, String deleteQuery) throws Exception {
        Class<Driver> driverClass = jdbcApi.getJDBCDriverClass().getDriver();
        final Connection conn = connect(driverClass);

        conn.setAutoCommit(false);

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
    }

    public void verifyTestStatement(JDBCApi JDBC_API, String insertQuery, String selectQuery, String deleteQuery) {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();

        Method connect = JDBC_API.getDriver().getConnect();
        verifier.verifyTrace(event(ORACLE, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        final JDBCApi.ConnectionClass connectionClass = JDBC_API.getConnection();

        Method setAutoCommit = connectionClass.getSetAutoCommit();
        verifier.verifyTrace(event(ORACLE, setAutoCommit, null, DB_ADDRESS, DB_NAME, args(false)));


        Method prepareStatement = connectionClass.getPrepareStatement();
        verifier.verifyTrace(event(ORACLE, prepareStatement, null, DB_ADDRESS, DB_NAME, sql(insertQuery, null)));

        Method execute = JDBC_API.getPreparedStatement().getExecute();
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, execute, null, DB_ADDRESS, DB_NAME, Expectations.sql(insertQuery, null, "maru, 5")));

        Method executeQuery = JDBC_API.getStatement().getExecuteQuery();
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, Expectations.sql(selectQuery, null)));

        Method executeUpdate = JDBC_API.getStatement().getExecuteUpdate();
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeUpdate, null, DB_ADDRESS, DB_NAME, Expectations.sql(deleteQuery, null)));

        Method commit = connectionClass.getCommit();
        verifier.verifyTrace(event(ORACLE, commit, null, DB_ADDRESS, DB_NAME));
    }

    public void verifyTestStatement_connectWithGssCredential(OracleJDBCApi JDBC_API, String insertQuery, String selectQuery, String deleteQuery) {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();

        Method connect = JDBC_API.getDriver().getConnect();
        verifier.verifyTrace(event(ORACLE, connect, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, cachedArgs(OracleItHelper.JDBC_URL)));

        // OracleDriver#connect(String, Properties, GssCredential)
        Method connectGssCredential = JDBC_API.getDriver().getConnectionWithGssCredential();
        verifier.verifyTrace(event(ORACLE, connectGssCredential, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, cachedArgs(OracleItHelper.JDBC_URL)));

        final JDBCApi.ConnectionClass connectionClass = JDBC_API.getConnection();

        Method setAutoCommit = connectionClass.getSetAutoCommit();
        verifier.verifyTrace(event(ORACLE, setAutoCommit, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, args(false)));


        Method prepareStatement = connectionClass.getPrepareStatement();
        verifier.verifyTrace(event(ORACLE, prepareStatement, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, sql(insertQuery, null)));

        Method execute = JDBC_API.getPreparedStatement().getExecute();
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, execute, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, Expectations.sql(insertQuery, null, "maru, 5")));

        Method executeQuery = JDBC_API.getStatement().getExecuteQuery();
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeQuery, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, Expectations.sql(selectQuery, null)));

        Method executeUpdate = JDBC_API.getStatement().getExecuteUpdate();
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeUpdate, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, Expectations.sql(deleteQuery, null)));

        Method commit = connectionClass.getCommit();
        verifier.verifyTrace(event(ORACLE, commit, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME));
    }

    /*
        CREATE OR REPLACE PROCEDURE concatCharacters(a IN VARCHAR2, b IN VARCHAR2, c OUT VARCHAR2)
        AS
        BEGIN
            c := a || b;
        END concatCharacters;
     */
    public void testStoredProcedure_with_IN_OUT_parameters(JDBCApi jdbcApi, String param1, String param2, String storedProcedureQuery) throws Exception {
        Class<Driver> driverClass = jdbcApi.getJDBCDriverClass().getDriver();
        final Connection conn = connect(driverClass);

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setString(1, param1);
        cs.setString(2, param2);
        cs.registerOutParameter(3, Types.NCHAR);
        cs.execute();

        Assertions.assertEquals(param1.concat(param2), cs.getString(3));
        conn.close();
    }

    public void verifyTestStoredProcedure_with_IN_OUT_parameters(JDBCApi JDBC_API, String param1, String param2, String storedProcedureQuery) {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(4);

        // OracleDriver#connect(String, Properties)
        Method connect = JDBC_API.getDriver().getConnect();
        verifier.verifyTrace(event(ORACLE, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // PhysicalConnection#prepareCall(String)
        final JDBCApi.ConnectionClass connectionClass = JDBC_API.getConnection();
        Method prepareCall = connectionClass.getPrepareCall();
        verifier.verifyTrace(event(ORACLE, prepareCall, null, DB_ADDRESS, DB_NAME, Expectations.sql(storedProcedureQuery, null)));

        // OracleCallableStatementWrapper#registerOutParameter(int, int)
        final JDBCApi.CallableStatementClass callableStatementClass = JDBC_API.getCallableStatement();
        Method registerOutParameter = callableStatementClass.getRegisterOutParameter();
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, DB_ADDRESS, DB_NAME, Expectations.args(3, Types.NCHAR)));

        // OraclePreparedStatementWrapper#execute
        final JDBCApi.PreparedStatementClass preparedStatementWrapper = JDBC_API.getPreparedStatement();
        Method executeQuery = preparedStatementWrapper.getExecute();
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null, param1 + ", " + param2)));

    }

    public void verifyTestStoredProcedure_with_IN_OUT_parameters_connectWithGssCredential(OracleJDBCApi JDBC_API, String param1, String param2, String storedProcedureQuery) {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(5);

        // OracleDriver#connect(String, Properties)
        Method connect = JDBC_API.getDriver().getConnect();
        verifier.verifyTrace(event(ORACLE, connect, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, cachedArgs(OracleItHelper.JDBC_URL)));

        // OracleDriver#connect(String, Properties, GssCredential)
        Method connectGssCredential = JDBC_API.getDriver().getConnectionWithGssCredential();
        verifier.verifyTrace(event(ORACLE, connectGssCredential, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, cachedArgs(OracleItHelper.JDBC_URL)));

        // PhysicalConnection#prepareCall(String)
        final JDBCApi.ConnectionClass connectionClass = JDBC_API.getConnection();
        Method prepareCall = connectionClass.getPrepareCall();
        verifier.verifyTrace(event(ORACLE, prepareCall, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, Expectations.sql(storedProcedureQuery, null)));

        // OracleCallableStatementWrapper#registerOutParameter(int, int)
        final JDBCApi.CallableStatementClass callableStatementClass = JDBC_API.getCallableStatement();
        Method registerOutParameter = callableStatementClass.getRegisterOutParameter();
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, Expectations.args(3, Types.NCHAR)));

        // OraclePreparedStatementWrapper#execute
        final JDBCApi.PreparedStatementClass preparedStatementWrapper = JDBC_API.getPreparedStatement();
        Method executeQuery = preparedStatementWrapper.getExecute();
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeQuery, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, sql(storedProcedureQuery, null, param1 + ", " + param2)));

    }

    /*
        CREATE OR REPLACE PROCEDURE swapAndGetSum(a IN OUT NUMBER, b IN OUT NUMBER, c OUT NUMBER)
        AS
        BEGIN
            c := a;
            a := b;
            b := c;
            SELECT c + a INTO c FROM DUAL;
        END swapAndGetSum;
    */
    public void testStoredProcedure_with_INOUT_parameters(JDBCApi jdbcApi, int param1, int param2, String storedProcedureQuery) throws Exception {
        Class<Driver> driverClass = jdbcApi.getJDBCDriverClass().getDriver();
        final Connection conn = connect(driverClass);

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setInt(1, param1);
        cs.setInt(2, param2);
        cs.registerOutParameter(1, Types.INTEGER);
        cs.registerOutParameter(2, Types.INTEGER);
        cs.registerOutParameter(3, Types.INTEGER);
        cs.execute();

        Assertions.assertEquals(param2, cs.getInt(1));
        Assertions.assertEquals(param1, cs.getInt(2));
        Assertions.assertEquals(param1 + param2, cs.getInt(3));

        conn.close();
    }


    public void verifyTestStoredProcedure_with_INOUT_parameters_connectWithGssCredential(OracleJDBCApi JDBC_API, int param1, int param2, String storedProcedureQuery) {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        // including one new connect api: oracle.jdbc.driver.OracleDriver.connect(java.lang.String, java.util.Properties, org.ietf.jgss.GSSCredential)
        verifier.verifyTraceCount(7);

        // OracleDriver#connect(String, Properties)
        Method connect = JDBC_API.getDriver().getConnect();
        verifier.verifyTrace(event(ORACLE, connect, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // OracleDriver#connect(String, Properties, GssCredential)
        Method connectGssCredential = JDBC_API.getDriver().getConnectionWithGssCredential();
        verifier.verifyTrace(event(ORACLE, connectGssCredential, null, DB_ADDRESS, DB_NAME, cachedArgs(JDBC_URL)));

        // PhysicalConnection#prepareCall(String)
        final JDBCApi.ConnectionClass connectionClass = JDBC_API.getConnection();
        Method prepareCall = connectionClass.getPrepareCall();
        verifier.verifyTrace(event(ORACLE, prepareCall, null, DB_ADDRESS, DB_NAME, Expectations.sql(storedProcedureQuery, null)));

        // OracleCallableStatementWrapper#registerOutParameter(int, int)
        final JDBCApi.CallableStatementClass callableStatementClass = JDBC_API.getCallableStatement();
        Method registerOutParameter = callableStatementClass.getRegisterOutParameter();
        // param 1
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, DB_ADDRESS, DB_NAME, Expectations.args(1, Types.INTEGER)));
        // param 2
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, DB_ADDRESS, DB_NAME, Expectations.args(2, Types.INTEGER)));
        // param 3
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, DB_ADDRESS, DB_NAME, Expectations.args(3, Types.INTEGER)));

        // OraclePreparedStatementWrapper#execute
        final JDBCApi.PreparedStatementClass preparedStatement = JDBC_API.getPreparedStatement();
        Method executeQuery = preparedStatement.getExecute();
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeQuery, null, DB_ADDRESS, DB_NAME, sql(storedProcedureQuery, null, param1 + ", " + param2)));

    }

    public void verifyTestStoredProcedure_with_INOUT_parameters(OracleJDBCApi JDBC_API, int param1, int param2, String storedProcedureQuery) {
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(6);

        // OracleDriver#connect(String, Properties)
        Method connect = JDBC_API.getDriver().getConnect();
        verifier.verifyTrace(event(ORACLE, connect, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, cachedArgs(OracleItHelper.JDBC_URL)));

        // PhysicalConnection#prepareCall(String)
        final JDBCApi.ConnectionClass connectionClass = JDBC_API.getConnection();
        Method prepareCall = connectionClass.getPrepareCall();
        verifier.verifyTrace(event(ORACLE, prepareCall, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, Expectations.sql(storedProcedureQuery, null)));

        // OracleCallableStatementWrapper#registerOutParameter(int, int)
        final JDBCApi.CallableStatementClass callableStatementClass = JDBC_API.getCallableStatement();
        Method registerOutParameter = callableStatementClass.getRegisterOutParameter();
        // param 1
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, Expectations.args(1, Types.INTEGER)));
        // param 2
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, Expectations.args(2, Types.INTEGER)));
        // param 3
        verifier.verifyTrace(event(ORACLE, registerOutParameter, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, Expectations.args(3, Types.INTEGER)));

        // OraclePreparedStatementWrapper#execute
        final JDBCApi.PreparedStatementClass preparedStatement = JDBC_API.getPreparedStatement();
        Method executeQuery = preparedStatement.getExecute();
        verifier.verifyTrace(event(ORACLE_EXECUTE_QUERY, executeQuery, null, OracleItHelper.DB_ADDRESS, OracleItHelper.DB_NAME, sql(storedProcedureQuery, null, param1 + ", " + param2)));

    }
}