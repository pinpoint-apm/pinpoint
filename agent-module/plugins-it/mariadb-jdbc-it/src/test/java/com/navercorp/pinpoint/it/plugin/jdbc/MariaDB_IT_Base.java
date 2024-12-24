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
 * @author HyunGil Jeong
 */
public abstract class MariaDB_IT_Base {
    private static final Logger logger = LogManager.getLogger(MariaDB_IT_Base.class);
    protected static final String DATABASE_NAME = MariaDBServer.DATABASE_NAME;

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

    protected static DriverProperties driverProperties = DatabaseContainers.readSystemProperties();

    protected static final String DB_TYPE = "MARIADB";
    protected static final String DB_EXECUTE_QUERY = "MARIADB_EXECUTE_QUERY";

    protected static String URL;

    public String getJdbcUrl() {
        return driverProperties.getUrl();
    }

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
    }

    @BeforeAll
    public static void beforeAll() {
        URL = System.getProperty("URL");
    }

    abstract JDBCDriverClass getJDBCDriverClass();

    @BeforeEach
    public void registerDriver() throws Exception {
        JDBCDriverClass driverClass = getJDBCDriverClass();
        Driver driver = driverClass.getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }

    @AfterEach
    public void deregisterDriver() {
        DriverManagerUtils.deregisterDriver();
    }

    protected final void executeStatement() throws Exception {
        final int expectedResultSize = 1;
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(STATEMENT_QUERY)) {
            int resultCount = 0;
            while (rs.next()) {
                ++resultCount;
                if (resultCount > expectedResultSize) {
                    Assertions.fail();
                }
                Assertions.assertEquals(3, rs.getInt(1));
            }
            Assertions.assertEquals(expectedResultSize, resultCount);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(driverProperties.getUrl(), driverProperties.getUser(), driverProperties.getPassword());
    }

    protected final void executePreparedStatement() throws Exception {
        final int expectedResultSize = 1;

        try (Connection connection = getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(PREPARED_STATEMENT_QUERY)) {
                ps.setInt(1, 3);
                try (ResultSet rs = ps.executeQuery()) {
                    int resultCount = 0;
                    while (rs.next()) {
                        ++resultCount;
                        if (resultCount > expectedResultSize) {
                            Assertions.fail();
                        }
                        Assertions.assertEquals("THREE", rs.getString(2));
                    }
                    Assertions.assertEquals(expectedResultSize, resultCount);
                }
            }
        }
    }

    protected final void executeCallableStatement() throws Exception {

        final int expectedResultSize = 1;
        final int expectedTotalCount = 3;
        final int expectedMatchingId = 2;
        final String outputParamCountName = "outputParamCount";

        try (Connection conn = getConnection();
             CallableStatement cs = conn.prepareCall(CALLABLE_STATEMENT_QUERY)) {
            cs.setString(1, CALLABLE_STATEMENT_INPUT_PARAM);
            cs.registerOutParameter(2, CALLABLE_STATMENT_OUTPUT_PARAM_TYPE);

            try (ResultSet rs = cs.executeQuery()) {
                int resultCount = 0;
                while (rs.next()) {
                    ++resultCount;
                    if (resultCount > expectedResultSize) {
                        Assertions.fail();
                    }
                    Assertions.assertEquals(expectedMatchingId, rs.getInt(1));
                    Assertions.assertEquals(CALLABLE_STATEMENT_INPUT_PARAM, rs.getString(2));
                }
                Assertions.assertEquals(expectedResultSize, resultCount);

                final int totalCount = cs.getInt(outputParamCountName);
                Assertions.assertEquals(expectedTotalCount, totalCount);

            }
        }
    }

}
