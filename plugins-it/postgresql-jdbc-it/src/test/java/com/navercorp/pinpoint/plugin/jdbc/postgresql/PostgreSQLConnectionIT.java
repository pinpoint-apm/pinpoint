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

package com.navercorp.pinpoint.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.DatabaseInfoAccessor;
import com.navercorp.pinpoint.pluginit.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.test.junit4.BasePinpointTest;
import com.navercorp.pinpoint.test.junit4.JunitAgentConfigPath;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

/**
 * @author Taejin Koo
 */
@JunitAgentConfigPath("pinpoint-postgresql.config")
public class PostgreSQLConnectionIT extends BasePinpointTest {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLConnectionIT.class);

    public static JdbcDatabaseContainer<?> container = PostgreSQLContainerFactory.newContainer(logger);

    private static final JDBCDriverClass driverClass = new PostgreSql_9_4_1207_JDBCDriverClass();

    @BeforeClass
    public static void beforeClass() throws Exception {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());
        container.start();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        container.stop();
    }

    @Before
    public void before() throws Exception {
        Driver driver = driverClass.getDriver().newInstance();
        DriverManager.registerDriver(driver);
    }


    @After
    public void deregisterDriver() throws Exception {
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
        Connection connection = null;
        try {
            connection = createConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());

            statement(connection);

            preparedStatement2(connection);

            preparedStatement3(connection);

            preparedStatement4(connection);

            preparedStatement5(connection);

//            preparedStatement6(connection);

            preparedStatement7(connection);

            preparedStatement8(connection);

            List<SpanEvent> currentSpanEvents = getCurrentSpanEvents();
            logger.debug("{}", currentSpanEvents);
        } finally {
            close(connection);
        }
    }

    @Test
    public void loadbalance() throws SQLException {
        Connection connection = null;
        try {
            Integer port = container.getFirstMappedPort();
            String hosts = String.format("localhost:%s,localhost:%s", port, port + 1);
            String jdbcUrl = String.format("jdbc:postgresql://%s/test?loggerLevel=OFF", hosts);

            connection = createConnection(jdbcUrl, container.getUsername(), container.getPassword());
            if (connection instanceof DatabaseInfoAccessor) {
                DatabaseInfoAccessor accessor = (DatabaseInfoAccessor) connection;
                DatabaseInfo databaseInfo = accessor._$PINPOINT$_getDatabaseInfo();
                String multipleHost = databaseInfo.getMultipleHost();
                Assert.assertEquals(hosts, multipleHost);
            } else {
                Assert.fail("Not DatabaseInfoAccessor");
            }
        } finally {
            close(connection);
        }
    }

    @Test(expected = SQLException.class)
    public void executeFail() throws SQLException {
        Connection connection = null;
        try {
            connection = createConnection(container.getJdbcUrl(), container.getUsername(), container.getPassword());

            Statement statement = connection.createStatement();

            statement.execute("select * from members");
        } finally {
            close(connection);
        }
    }

    private void statement(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeQuery("select 1");
        close(statement);
    }

    private void preparedStatement(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select 1");
        logger.debug("PreparedStatement className:" + preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        close(resultSet);
        close(preparedStatement);
    }


    private void preparedStatement2(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select * from member where id = ?");
        preparedStatement.setInt(1, 1);
        ResultSet resultSet = preparedStatement.executeQuery();
        close(resultSet);
        close(preparedStatement);
    }

    private void preparedStatement3(Connection connection) throws SQLException {
        connection.setAutoCommit(false);

        PreparedStatement preparedStatement = connection.prepareStatement("select * from member where id = ? or id = ?  or id = ?");
        preparedStatement.setInt(1, 1);
        preparedStatement.setInt(2, 2);
        preparedStatement.setInt(3, 3);
        ResultSet resultSet = preparedStatement.executeQuery();
        close(resultSet);
        close(preparedStatement);

        connection.commit();

        connection.setAutoCommit(true);
    }

    private void preparedStatement4(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", Statement.RETURN_GENERATED_KEYS);
        logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        close(resultSet);
        close(preparedStatement);
    }

    private void preparedStatement5(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", new String[]{"test"});
        logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        close(resultSet);
        close(preparedStatement);
    }

    private void preparedStatement6(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        int[] columnIndex = {1};
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", columnIndex);
        logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();

        close(resultSet);
        close(preparedStatement);
    }

    private void preparedStatement7(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        close(resultSet);
        close(preparedStatement);
    }

    private void preparedStatement8(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
//        ResultSet.HOLD_CURSORS_OVER_COMMIT or ResultSet.CLOSE_CURSORS_AT_COMMIT
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        logger.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        close(resultSet);
        close(preparedStatement);
    }

    private static void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                logger.warn("close error", e);
            }
        }
    }

}
