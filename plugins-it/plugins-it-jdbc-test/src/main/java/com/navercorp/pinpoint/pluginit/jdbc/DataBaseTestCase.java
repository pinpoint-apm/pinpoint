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

package com.navercorp.pinpoint.pluginit.jdbc;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserV2;
import com.navercorp.pinpoint.bootstrap.plugin.test.Expectations;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

/**
 *
 * @author Woonduk Kang(emeroad)
 */
public abstract class DataBaseTestCase {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String DB_TYPE;
    protected String DB_EXECUTE_QUERY;

    protected String jdbcUrl;
    protected String databaseId;
    protected String databaseIdPassword;
    protected String databaseAddress;
    protected String databaseName;
    protected JDBCApi jdbcApi;

    public void setup(String dbType, String executeQuery, DriverProperties driverProperties, JdbcUrlParserV2 jdbcUrlParser, JDBCApi jdbcApi) {
        this.DB_TYPE = dbType;
        this.DB_EXECUTE_QUERY = executeQuery;

        this.jdbcUrl = driverProperties.getUrl();

        DatabaseInfo databaseInfo = jdbcUrlParser.parse(jdbcUrl);

        this.databaseAddress = databaseInfo.getHost().get(0);
        this.databaseName = databaseInfo.getDatabaseId();

        this.databaseId = driverProperties.getUser();
        this.databaseIdPassword = driverProperties.getPassword();

        this.jdbcApi = jdbcApi;

        try {
            JDBCDriverClass jdbcDriverClass = getJDBCDriverClass();
            Driver driver = jdbcDriverClass.getDriver().newInstance();
            DriverManager.registerDriver(driver);
        } catch (Exception e) {
            throw new RuntimeException("driver register error", e);
        }
    }

    protected abstract JDBCDriverClass getJDBCDriverClass();


    @After
    public void deregisterDriver() {
        DriverManagerUtils.deregisterDriver();
    }

    protected Connection connectDB() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, databaseId, databaseIdPassword);
    }

    @Test
    public void testStatement() throws Exception {
        Connection conn = connectDB();

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

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();

        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(Expectations.event(DB_TYPE, connect, null, databaseAddress, databaseName, Expectations.cachedArgs(jdbcUrl)));

        JDBCApi.ConnectionClass connectionClass = jdbcApi.getConnection();
        Method setAutoCommit = connectionClass.getSetAutoCommit();
        verifier.verifyTrace(Expectations.event(DB_TYPE, setAutoCommit, null, databaseAddress, databaseName, Expectations.args(false)));


        Method prepareStatement = connectionClass.getPrepareStatement();
        verifier.verifyTrace(Expectations.event(DB_TYPE, prepareStatement, null, databaseAddress, databaseName, Expectations.sql(insertQuery, null)));

        Method execute = jdbcApi.getPreparedStatement().getExecute();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, execute, null, databaseAddress, databaseName, Expectations.sql(insertQuery, null, "maru, 5")));

        JDBCApi.StatementClass statementClass = jdbcApi.getStatement();
        Method executeQuery = statementClass.getExecuteQuery();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, executeQuery, null, databaseAddress, databaseName, Expectations.sql(selectQuery, null)));

        Method executeUpdate = statementClass.getExecuteUpdate();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, executeUpdate, null, databaseAddress, databaseName, Expectations.sql(deleteQuery, null)));

        Method commit = connectionClass.getCommit();
        verifier.verifyTrace(Expectations.event(DB_TYPE, commit, null, databaseAddress, databaseName));

        close(conn);
    }



    /*
        CREATE PROCEDURE concatCharacters
            @a CHAR(1),
            @b CHAR(1),
            @c CHAR(2) OUTPUT
        AS
            SET @c = @a + @b;
     */
    @Test
    public void testStoredProcedure_with_IN_OUT_parameters() throws Exception {
        final String param1 = "a";
        final String param2 = "b";
        final String storedProcedureQuery = "{ call concatCharacters(?, ?, ?) }";

        Connection conn = connectDB();

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setString(1, param1);
        cs.setString(2, param2);
        cs.registerOutParameter(3, Types.VARCHAR);
        cs.execute();

        Assert.assertEquals(param1.concat(param2), cs.getString(3));

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(4);

        // Driver#connect(String, Properties)
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(Expectations.event(DB_TYPE, connect, null, databaseAddress, databaseName, Expectations.cachedArgs(jdbcUrl)));

        // ConnectionJDBC2#prepareCall(String)
        JDBCApi.ConnectionClass connectionClass = jdbcApi.getConnection();
        Method prepareCall = connectionClass.getPrepareCall();
        verifier.verifyTrace(Expectations.event(DB_TYPE, prepareCall, null, databaseAddress, databaseName, Expectations.sql(storedProcedureQuery, null)));

        // JtdsCallableStatement#registerOutParameter(int, int)
        Method registerOutParameter = jdbcApi.getCallableStatement().getRegisterOutParameter();
        verifier.verifyTrace(Expectations.event(DB_TYPE, registerOutParameter, null, databaseAddress, databaseName, Expectations.args(3, Types.VARCHAR)));

        // JtdsPreparedStatement#execute
        Method execute = jdbcApi.getPreparedStatement().getExecute();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, execute, null, databaseAddress, databaseName, Expectations.sql(storedProcedureQuery, null, param1 + ", " + param2)));

        close(conn);
    }

    /*
        CREATE PROCEDURE swapAndGetSum
            @a INT OUTPUT,
            @b INT OUTPUT
        AS
            DECLARE @temp INT;
            SET @temp = @a;
            SET @a = @b;
            SET @b = @temp;
            SELECT @temp + @a;
     */
    @Test
    public void testStoredProcedure_with_INOUT_parameters() throws Exception {
        final int param1 = 1;
        final int param2 = 2;
        final String storedProcedureQuery = "{ call swapAndGetSum(?, ?) }";

        Connection conn = connectDB();

        CallableStatement cs = conn.prepareCall(storedProcedureQuery);
        cs.setInt(1, param1);
        cs.setInt(2, param2);
        cs.registerOutParameter(1, Types.INTEGER);
        cs.registerOutParameter(2, Types.INTEGER);
        ResultSet rs = cs.executeQuery();

        Assert.assertTrue(rs.next());
        Assert.assertEquals(param1 + param2, rs.getInt(1));
        Assert.assertFalse(cs.getMoreResults());
        Assert.assertEquals(param2, cs.getInt(1));
        Assert.assertEquals(param1, cs.getInt(2));

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();

        verifier.printCache();
        verifier.verifyTraceCount(5);

        // Driver#connect(String, Properties)
        Method connect = jdbcApi.getDriver().getConnect();
        verifier.verifyTrace(Expectations.event(DB_TYPE, connect, null, databaseAddress, databaseName, Expectations.cachedArgs(jdbcUrl)));

        // ConnectionJDBC2#prepareCall(String)
        Method prepareCall = jdbcApi.getConnection().getPrepareCall();;
        verifier.verifyTrace(Expectations.event(DB_TYPE, prepareCall, null, databaseAddress, databaseName, Expectations.sql(storedProcedureQuery, null)));

        // JtdsCallableStatement#registerOutParameter(int, int)
        Method registerOutParameter = jdbcApi.getCallableStatement().getRegisterOutParameter();
        // param 1
        verifier.verifyTrace(Expectations.event(DB_TYPE, registerOutParameter, null, databaseAddress, databaseName, Expectations.args(1, Types.INTEGER)));
        // param 2
        verifier.verifyTrace(Expectations.event(DB_TYPE, registerOutParameter, null, databaseAddress, databaseName, Expectations.args(2, Types.INTEGER)));

        // JtdsPreparedStatement#executeQuery
        Method executeQuery = jdbcApi.getPreparedStatement().getExecuteQuery();
        verifier.verifyTrace(Expectations.event(DB_EXECUTE_QUERY, executeQuery, null, databaseAddress, databaseName, Expectations.sql(storedProcedureQuery, null, param1 + ", " + param2)));

        close(conn);
    }

    public void close(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
            }
        }
    }
}
