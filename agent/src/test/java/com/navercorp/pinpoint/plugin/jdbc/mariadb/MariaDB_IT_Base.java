/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdbc.mariadb;

import ch.vorburger.mariadb4j.DB;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.sql.CallableStatement;
import java.sql.Connection;
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
public class MariaDB_IT_Base {

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

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TEST_DATABASE = DB.newEmbeddedDB(PORT);
        TEST_DATABASE.start();
        TEST_DATABASE.createDB("test");
        TEST_DATABASE.source("jdbc/mariadb/init.sql");
    }

    protected final void executeStatement() throws Exception {
        final int expectedResultSize = 1;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(JDBC_URL, "root", null);

            Statement statement = null;
            try {
                statement = connection.createStatement();

                ResultSet rs = null;
                try {
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
                }
            } finally {
                closeStatement(statement);
            }
        } finally {
            closeConnection(connection);
        }
    }

    protected final void executePreparedStatement() throws Exception {
        final int expectedResultSize = 1;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(JDBC_URL, "root", null);

            PreparedStatement ps = null;
            try {
                ps = connection.prepareStatement(PREPARED_STATEMENT_QUERY);
                ps.setInt(1, 3);

                ResultSet rs = null;
                try {
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
                }
            } finally {
                closeStatement(ps);
            }
        } finally {
            closeConnection(connection);
        }
    }

    protected final void executeCallableStatement() throws Exception {

        final int expectedResultSize = 1;
        final int expectedTotalCount = 3;
        final int expectedMatchingId = 2;
        final String outputParamCountName = "outputParamCount";

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(JDBC_URL, "root", null);

            CallableStatement cs = null;
            try {
                cs = conn.prepareCall(CALLABLE_STATEMENT_QUERY);
                cs.setString(1, CALLABLE_STATEMENT_INPUT_PARAM);
                cs.registerOutParameter(2, CALLABLE_STATMENT_OUTPUT_PARAM_TYPE);

                ResultSet rs = null;
                try {
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
                } finally {
                    closeResultSet(rs);
                }
                final int totalCount = cs.getInt(outputParamCountName);
                assertEquals(expectedTotalCount, totalCount);
            } finally {
                closeStatement(cs);
            }
        } finally {
            closeConnection(conn);
        }
    }

    private void closeConnection(Connection conn) throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    private void closeResultSet(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }

    private void closeStatement(Statement statement) throws SQLException {
        if (statement != null) {
            statement.close();
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        TEST_DATABASE.stop();
    }
}
