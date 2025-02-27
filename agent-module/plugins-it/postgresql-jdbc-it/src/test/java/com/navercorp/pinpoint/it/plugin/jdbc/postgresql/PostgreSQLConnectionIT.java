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

package com.navercorp.pinpoint.it.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static com.navercorp.pinpoint.it.plugin.utils.jdbc.JdbcUtils.doInTransaction;
import static com.navercorp.pinpoint.it.plugin.utils.jdbc.JdbcUtils.fetchResultSet;

/**
 * @author Taejin Koo
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.postgresql:postgresql:[9.4.1212]", PluginITConstants.VERSION, JDBCTestConstants.VERSION, TestcontainersOption.TEST_CONTAINER, TestcontainersOption.POSTGRESQL})
@PinpointConfig("pinpoint-postgresql.config")
public class PostgreSQLConnectionIT {

    private static final Logger logger = LogManager.getLogger(PostgreSQLConnectionIT.class);

    @AutoClose("stop")
    public static JdbcDatabaseContainer<?> container = PostgreSQLContainerFactory.newContainer(logger.getName());

    private static final JDBCDriverClass driverClass = new PostgreSql_9_4_1207_JDBCDriverClass();

    @BeforeAll
    public static void beforeClass() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");
        container.start();
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
        Properties properties = new Properties();
        properties.put("user", username);
        properties.put("password", password);
        return DriverManager.getConnection(url, properties);
    }

    @Test
    public void execute() throws SQLException {
        try (Connection connection = createConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword())) {

            statement(connection);

            preparedStatement2(connection);

            preparedStatement3(connection);

            preparedStatement4(connection);

            preparedStatement5(connection);

//            preparedStatement6(connection);

            preparedStatement7(connection);

            preparedStatement8(connection);

            PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
            verifier.printCache();
        }
    }

    @Test
    public void loadbalance() throws SQLException {
        Integer port = container.getFirstMappedPort();
        String hosts = String.format("localhost:%s,localhost:%s", port, port + 1);
        String jdbcUrl = String.format("jdbc:postgresql://%s/test?loggerLevel=OFF", hosts);
        try (Connection connection = createConnection(jdbcUrl, container.getUsername(), container.getPassword())) {
            if (connection instanceof DatabaseInfoAccessor) {
                DatabaseInfoAccessor accessor = (DatabaseInfoAccessor) connection;
                DatabaseInfo databaseInfo = accessor._$PINPOINT$_getDatabaseInfo();
                String multipleHost = databaseInfo.getMultipleHost();
                Assertions.assertEquals(hosts, multipleHost);
            } else {
                Assertions.fail("Not DatabaseInfoAccessor");
            }
        }
    }

    @Test()
    public void executeFail() throws SQLException {
        Assertions.assertThrows(SQLException.class, () -> {
            try (Connection connection = createConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword())) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("select * from members");
                }
            }
        });
    }

    private void statement(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeQuery("select 1");
        }
    }

    private void preparedStatement(Connection connection) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1")) {
            logger.debug("PreparedStatement className:" + preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
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
                preparedStatement.setInt(3, 3);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    fetchResultSet(resultSet);
                }
            };
        });
    }

    private void preparedStatement4(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1", Statement.RETURN_GENERATED_KEYS)) {
            logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

    private void preparedStatement5(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1", new String[]{"test"})) {
            logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

    private void preparedStatement6(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        int[] columnIndex = {1};
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1", columnIndex)) {
            logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

    private void preparedStatement7(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

    private void preparedStatement8(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
//        ResultSet.HOLD_CURSORS_OVER_COMMIT or ResultSet.CLOSE_CURSORS_AT_COMMIT
        try (PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT)) {
            logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                fetchResultSet(resultSet);
            }
        }
    }

}
