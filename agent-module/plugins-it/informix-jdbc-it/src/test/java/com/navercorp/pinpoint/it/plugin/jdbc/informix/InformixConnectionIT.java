/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.jdbc.informix;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.it.plugin.utils.jdbc.JDBCTestConstants;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import static com.navercorp.pinpoint.it.plugin.utils.jdbc.JdbcUtils.doInTransaction;
import static com.navercorp.pinpoint.it.plugin.utils.jdbc.JdbcUtils.fetchResultSet;

/**
 * @author Taejin Koo
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.apache.ibatis:ibatis-sqlmap:[2.3.4.726]", "com.ibm.informix:jdbc:[4.10.10.0]",
        PluginITConstants.VERSION, JDBCTestConstants.VERSION})
@SharedDependency({"com.ibm.informix:jdbc:4.10.10.0", TestcontainersOption.TEST_CONTAINER,
        PluginITConstants.VERSION, JDBCTestConstants.VERSION})
@SharedTestLifeCycleClass(InformixServer.class)
@PinpointConfig("pinpoint-informix.config")
public class InformixConnectionIT {

    private static final Logger LOGGER = LogManager.getLogger(InformixConnectionIT.class);

    private static JDBCDriverClass driverClass;
    private static int port;

    @BeforeAll
    public static void beforeAll() throws Exception {
        driverClass = new InformixJDBCDriverClass();
        port = Integer.parseInt(System.getProperty("PORT"));
    }

    @BeforeEach
    public void before() throws Exception {
        Driver driver = driverClass.newDriver();
        DriverManager.registerDriver(driver);
    }

    @AfterEach
    public void deregisterDriver() {
        DriverManagerUtils.deregisterDriver();
    }

    private static Connection createConnection(String url, String username, String password) throws SQLException {
        String connectionUrl = createConnectionUrl(url, username, password);
        return DriverManager.getConnection(connectionUrl);
    }

    private static String createConnectionUrl(String url, String username, String password) {
        return "jdbc:informix-sqli:" +
                url +
                ":" +
                "user=" + username + ";" +
                "password=" + password;
    }

    private static List<String> createTableQuery() {
        String create1 = "CREATE TABLE member \n" +
                "   (\n" +
                "   id  INT PRIMARY KEY,\n" +
                "   name    CHAR(20)\n" +
                "   );";

        return Collections.singletonList(create1);
    }

    @Test
    public void execute() throws SQLException {
        try (Connection connection = createConnection("localhost:" + port + "/sysmaster", "informix", "in4mix")) {

            statement(connection);

            preparedStatement2(connection);

            preparedStatement3(connection);

            preparedStatement4(connection);

            preparedStatement5(connection);

            preparedStatement6(connection);

            preparedStatement7(connection);

            preparedStatement8(connection);

//            List<SpanEvent> currentSpanEvents = getCurrentSpanEvents();
//            LOGGER.info("{}", currentSpanEvents);
        }
    }

    @Test()
    public void executeFail() throws SQLException {
        Assertions.assertThrows(SQLException.class, () -> {
            try (Connection connection = createConnection("localhost:" + port + "/sysmaster", "informix", "in4mix")) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("select * from members");
                }
            }
        });

    }

    private void statement(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            try (ResultSet resultSet = statement.executeQuery("select 1")) {
                fetchResultSet(resultSet);
            }
        }
    }

    private void preparedStatement(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1")) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                LOGGER.debug("PreparedStatement className:" + preparedStatement.getClass().getName());
                fetchResultSet(resultSet);
            }
        }
    }


    private void preparedStatement2(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select * from member where id = ?")) {
            preparedStatement.setInt(1, 1);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

    private void preparedStatement3(Connection connection) throws SQLException {
        doInTransaction(connection, () -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("select * from member where id = ? or id = ?  or id = ?")) {
                preparedStatement.setInt(1, 1);
                preparedStatement.setInt(2, 2);
                preparedStatement.setString(3, "3");
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    fetchResultSet(resultSet);
                }
            }
        });
    }

    private void preparedStatement4(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1", Statement.RETURN_GENERATED_KEYS)) {
            LOGGER.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

    private void preparedStatement5(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1", new String[]{"test"})) {
            LOGGER.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

    private void preparedStatement6(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        int[] columnIndex = {1};
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1", columnIndex)) {
            LOGGER.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

    private void preparedStatement7(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            LOGGER.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

    private void preparedStatement8(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
//        ResultSet.HOLD_CURSORS_OVER_COMMIT or ResultSet.CLOSE_CURSORS_AT_COMMIT
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            LOGGER.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

}
