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

package com.navercorp.pinpoint.plugin.jdbc.informix;

import com.navercorp.pinpoint.pluginit.jdbc.DriverManagerUtils;
import com.navercorp.pinpoint.pluginit.jdbc.JDBCDriverClass;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.test.junit4.BasePinpointTest;
import com.navercorp.pinpoint.test.junit4.JunitAgentConfigPath;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerMachineClient;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * @author Taejin Koo
 */
@JunitAgentConfigPath("pinpoint-informix.config")
public class InformixConnectionIT extends BasePinpointTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InformixConnectionIT.class);

    private static GenericContainer<?> container = new GenericContainer<>("ibmcom/informix-developer-database:latest");

    private static JDBCDriverClass driverClass;

// port 9088 : to allow remote connections from TCP clients
// port 9089 : to allow remote connections from DRDA clients
// port 27017 : to allow remote connections from mongo clients
// port 27018 : to allow remote connections from REST clients
// port 27883 : to allow remote connections from MQTT clients

    @BeforeClass
    public static void beforeClass() throws Exception {
        Assume.assumeTrue("Docker not enabled", DockerMachineClient.instance().isInstalled());

        container.withPrivilegedMode(true);
        container.withExposedPorts(9088, 9089, 27017, 27018, 27883);
        container.withEnv("LICENSE", "accept");
        container.withEnv("DB_INIT", "1");
        container.withLogConsumer(new Slf4jLogConsumer(LOGGER));
        container.start();

        driverClass = new InformixJDBCDriverClass();

        Driver driver = driverClass.getDriver().newInstance();
        DriverManager.registerDriver(driver);

        Connection connection = null;
        try {
            connection = createConnection("localhost:" + container.getFirstMappedPort() + "/sysmaster", "informix", "in4mix");
            Statement statement = connection.createStatement();

            List<String> tableQuery = createTableQuery();

            for (String query : tableQuery) {
                statement.execute(query);
            }

        } finally {
            if (connection != null) {
                connection.close();
            }
        }
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
        String connectionUrl = createConnectionUrl(url, username, password);
        return DriverManager.getConnection(connectionUrl);
    }

    private static String createConnectionUrl(String url, String username, String password) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("jdbc:informix-sqli:");
        urlBuilder.append(url);
        urlBuilder.append(":");
        urlBuilder.append("user=").append(username).append(";");
        urlBuilder.append("password=").append(password);
        return urlBuilder.toString();
    }

    private static List<String> createTableQuery() {
        String create1 = "CREATE TABLE member \n" +
                "   (\n" +
                "   id  INT PRIMARY KEY,\n" +
                "   name    CHAR(20)\n" +
                "   );";

        return Arrays.asList(create1);
    }

    @Test
    public void execute() throws SQLException {
        Connection connection = null;
        try {
            connection = createConnection("localhost:" + container.getFirstMappedPort() + "/sysmaster", "informix", "in4mix");

            statement(connection);

            preparedStatement2(connection);

            preparedStatement3(connection);

            preparedStatement4(connection);

            preparedStatement5(connection);

            preparedStatement6(connection);

            preparedStatement7(connection);

            preparedStatement8(connection);

            List<SpanEvent> currentSpanEvents = getCurrentSpanEvents();
            LOGGER.info("{}", currentSpanEvents);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    @Test(expected = SQLException.class)
    public void executeFail() throws SQLException {
        Connection connection = null;
        try {
            connection = createConnection("localhost:" + container.getFirstMappedPort() + "/sysmaster", "informix", "in4mix");

            Statement statement = connection.createStatement();

            statement.execute("select * from members");
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void statement(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeQuery("select 1");
        statement.close();
    }

    private void preparedStatement(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select 1");
        LOGGER.debug("PreparedStatement className:" + preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }


    private void preparedStatement2(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select * from member where id = ?");
        preparedStatement.setInt(1, 1);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement3(Connection connection) throws SQLException {
        connection.setAutoCommit(false);

        PreparedStatement preparedStatement = connection.prepareStatement("select * from member where id = ? or id = ?  or id = ?");
        preparedStatement.setInt(1, 1);
        preparedStatement.setInt(2, 2);
        preparedStatement.setString(3, "3");
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();

        connection.commit();

        connection.setAutoCommit(true);
    }

    private void preparedStatement4(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", Statement.RETURN_GENERATED_KEYS);
        LOGGER.debug("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement5(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", new String[]{"test"});
        LOGGER.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement6(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        int[] columnIndex = {1};
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", columnIndex);
        LOGGER.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement7(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        LOGGER.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement8(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
//        ResultSet.HOLD_CURSORS_OVER_COMMIT or ResultSet.CLOSE_CURSORS_AT_COMMIT
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        LOGGER.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

}
