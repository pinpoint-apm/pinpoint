/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.jdbc;

import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverProperties;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Properties;


/**
 * @author yjqg6666
 */
public abstract class Dameng_IT_Base {

    private static final Logger logger = LogManager.getLogger(Dameng_IT_Base.class);

    // for Statement
    protected static final String STATEMENT_QUERY = "SELECT count(1) FROM citest";
    protected static final String STATEMENT_NORMALIZED_QUERY = "SELECT count(0#) FROM citest";

    // for Prepared Statement
    protected static final String PREPARED_STATEMENT_QUERY = "SELECT * FROM citest where ci_id = ?";

    // for Callable Statement
    protected static final String PROCEDURE_NAME = "ciUpdateValue";
    protected static final String CALLABLE_STATEMENT_QUERY = "{ CALL " + PROCEDURE_NAME + "(?, ?, ?) }";
    protected static final int CALLABLE_STATEMENT_INPUT_ID = 3;
    protected static final String CALLABLE_STATEMENT_INPUT_VAL = "citest";
    protected static final int CALLABLE_STATEMENT_OUTPUT_PARAM_TYPE = Types.INTEGER;

    protected static final String DB_TYPE = "DAMENG";
    protected static final String DB_EXECUTE_QUERY = "DAMENG_EXECUTE_QUERY";

    private DriverProperties driverProperties;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeAll() {
    }

    @BeforeEach
    public void registerDriver() throws Exception {
        driverProperties = DatabaseContainers.readSystemProperties();
        JDBCDriverClass driverClass = getJDBCDriverClass();
        Driver driver = driverClass.getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }

    @AfterEach
    public void deregisterDriver() {
        DriverManagerUtils.deregisterDriver();
    }

    abstract JDBCDriverClass getJDBCDriverClass();

    protected String getJdbcUrl() {
        return driverProperties.getUrl();
    }

    protected String getDestination() {
        return System.getProperty(DriverProperties.HOST) + ":" + System.getProperty(DriverProperties.PORT);
    }

    protected final void executeStatement() throws Exception {
        final int expectedResultSize = 1;
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(STATEMENT_QUERY);
            int resultCount = 0;
            while (rs.next()) {
                ++resultCount;
                if (resultCount > expectedResultSize) {
                    Assertions.fail();
                }
                Assertions.assertEquals(4, rs.getInt(1));
            }
            Assertions.assertEquals(expectedResultSize, resultCount);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            closeQuietly(rs);
            closeQuietly(statement);
            closeQuietly(connection);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(driverProperties.getUrl(), driverProperties.getUser(), driverProperties.getPassword());
    }

    protected final void executePreparedStatement() throws Exception {
        final int expectedResultSize = 1;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement(PREPARED_STATEMENT_QUERY);
            ps.setInt(1, 1);
            rs = ps.executeQuery();
            int resultCount = 0;
            while (rs.next()) {
                ++resultCount;
                if (resultCount > expectedResultSize) {
                    Assertions.fail();
                }
                Assertions.assertEquals("dameng-jdbc", rs.getString("ci_val"));
            }
            Assertions.assertEquals(expectedResultSize, resultCount);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(connection);
        }
    }

    protected final void executeCallableStatement() throws Exception {

        Connection conn = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            conn = getConnection();

            cs = conn.prepareCall(CALLABLE_STATEMENT_QUERY);
            cs.setLong(1, CALLABLE_STATEMENT_INPUT_ID);
            cs.setString(2, CALLABLE_STATEMENT_INPUT_VAL);
            cs.registerOutParameter(3, CALLABLE_STATEMENT_OUTPUT_PARAM_TYPE);

            rs = cs.executeQuery();
            int cnt = cs.getInt(3);
            Assertions.assertEquals(1, cnt, "val count not 1");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            closeQuietly(rs);
            closeQuietly(cs);
            closeQuietly(conn);
        }
    }

    private void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
                // empty
            }
        }
    }

}
