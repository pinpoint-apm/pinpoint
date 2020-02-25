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

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import com.navercorp.pinpoint.pluginit.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author HyunGil Jeong
 */
public abstract class MariaDB_IT_Base {

    private static final int PORT = 13306;
    protected static final String URL = "127.0.0.1:" + PORT;
    protected static final String DATABASE_NAME = "test";

    protected static final String JDBC_URL = "jdbc:mariadb://" + URL + "/" + DATABASE_NAME;

    // for Statement
    protected static final String STATEMENT_QUERY = "SELECT count(1) FROM playground";
    protected static final String STATEMENT_NORMALIZED_QUERY = "SELECT count(0#) FROM playground";

    // for Prepared Statement
    protected static final String PREPARED_STATEMENT_QUERY = "SELECT * FROM playground where id = ?";

    // for Callable Statement
    protected static final String PROCEDURE_NAME = "getPlaygroundByName";
    protected static final String CALLABLE_STATEMENT_QUERY = "{ CALL " + PROCEDURE_NAME + "(?, ?) }";
    protected static final String CALLABLE_STATEMENT_INPUT_PARAM = "TWO";
    protected static final int CALLABLE_STATMENT_OUTPUT_PARAM_TYPE = Types.INTEGER;

    private static DB TEST_DATABASE;
    protected static DriverProperties driverProperties;


    private static final String EMBEDDED_DB_PORT_KEY = "maria.embedded-db.port";

    protected static final String DB_TYPE = "MARIADB";
    protected static final String DB_EXECUTE_QUERY = "MARIADB_EXECUTE_QUERY";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        driverProperties = DriverProperties.load("database/maria.properties", "maria");
        int embeddedDBPort = Integer.parseInt(driverProperties.getProperty(EMBEDDED_DB_PORT_KEY));
        TEST_DATABASE = DB.newEmbeddedDB(embeddedDBPort);
        TEST_DATABASE.start();
        TEST_DATABASE.createDB("test");
        TEST_DATABASE.source("jdbc/mariadb/init.sql");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TEST_DATABASE = testDBStop();

    }

    abstract JDBCDriverClass getJDBCDriverClass();

    @Before
    public void registerDriver() throws Exception {
        JDBCDriverClass driverClass = getJDBCDriverClass();
        Driver driver = driverClass.getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }

    @After
    public void deregisterDriver() {
        DriverManagerUtils.deregisterDriver();
    }

    private static DB testDBStop() throws ManagedProcessException {
        if (TEST_DATABASE != null) {
            TEST_DATABASE.stop();
        }
        return null;
    }

    protected final void executeStatement() throws Exception {
        final int expectedResultSize = 1;
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = DriverManager.getConnection(JDBC_URL, "root", null);
            statement = connection.createStatement();
            rs = statement.executeQuery(STATEMENT_QUERY);
            int resultCount = 0;
            while (rs.next()) {
                ++resultCount;
                if (resultCount > expectedResultSize) {
                    fail();
                }
                assertEquals(3, rs.getInt(1));
            }
            assertEquals(expectedResultSize, resultCount);
        } finally {
            closeResultSet(rs);
            closeStatement(statement);
            closeConnection(connection);
        }
    }

    protected final void executePreparedStatement() throws Exception {
        final int expectedResultSize = 1;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DriverManager.getConnection(JDBC_URL, "root", null);
            ps = connection.prepareStatement(PREPARED_STATEMENT_QUERY);
            ps.setInt(1, 3);
            rs = ps.executeQuery();
            int resultCount = 0;
            while (rs.next()) {
                ++resultCount;
                if (resultCount > expectedResultSize) {
                    fail();
                }
                assertEquals("THREE", rs.getString(2));
            }
            assertEquals(expectedResultSize, resultCount);
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeConnection(connection);
        }
    }

    protected final void executeCallableStatement() throws Exception {

        final int expectedResultSize = 1;
        final int expectedTotalCount = 3;
        final int expectedMatchingId = 2;
        final String outputParamCountName = "outputParamCount";

        Connection conn = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(JDBC_URL, "root", null);

            cs = conn.prepareCall(CALLABLE_STATEMENT_QUERY);
            cs.setString(1, CALLABLE_STATEMENT_INPUT_PARAM);
            cs.registerOutParameter(2, CALLABLE_STATMENT_OUTPUT_PARAM_TYPE);

            rs = cs.executeQuery();
            int resultCount = 0;
            while (rs.next()) {
                ++resultCount;
                if (resultCount > expectedResultSize) {
                    fail();
                }
                assertEquals(expectedMatchingId, rs.getInt(1));
                assertEquals(CALLABLE_STATEMENT_INPUT_PARAM, rs.getString(2));
            }
            assertEquals(expectedResultSize, resultCount);

            final int totalCount = cs.getInt(outputParamCountName);
            assertEquals(expectedTotalCount, totalCount);

        } finally {
            closeResultSet(rs);
            closeStatement(cs);
            closeConnection(conn);
        }
    }

    private void closeConnection(Connection conn) throws SQLException {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // empty
            }
        }
    }

    private void closeResultSet(ResultSet rs) throws SQLException {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // empty
            }
        }
    }

    private void closeStatement(Statement statement) throws SQLException {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // empty
            }
        }
    }


}
