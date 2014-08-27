package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.util.MockAgent;
import com.nhn.pinpoint.profiler.util.TestClassLoader;
import cubrid.jdbc.driver.CUBRIDDriver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

/**
 * @author emeroad
 */
public class CubridConnectionTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private TestClassLoader loader;

    @Test
    public void executeQueryAndexecuteUpdate() throws SQLException {
        CUBRIDDriver driver = new CUBRIDDriver();
        Properties properties = new Properties();
        properties.setProperty("user", "dba");
        properties.setProperty("password", "nhn!@#123");
        Connection connect = driver.connect("jdbc:cubrid:10.101.57.233:30102:pinpoint:::", properties);

        PreparedStatement preparedStatement = connect.prepareStatement("select 1 from db_root where 1=?");
        preparedStatement.setInt(1, 1);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            System.out.println("---" + resultSet.getObject(1));
        }
    }

    @Before
    public void setUp() throws Exception {
        PLoggerFactory.initialize(new Slf4jLoggerBinder());

        ProfilerConfig profilerConfig = new ProfilerConfig();
        // profiler config를 setter를 열어두는것도 괜찮을듯 하다.
        String path = MockAgent.class.getClassLoader().getResource("pinpoint.config").getPath();
        profilerConfig.readConfigFile(path);

        profilerConfig.setApplicationServerType(ServiceType.TEST_STAND_ALONE);
        DefaultAgent agent = new MockAgent("", profilerConfig);
        loader = new TestClassLoader(agent);
        // agent가 로드한 모든 Modifier를 자동으로 찾도록 변경함.


        loader.initialize();
    }

    @Test
    public void testModify() throws Exception {

        Driver driver = loadClass();

        Properties properties = new Properties();
        properties.setProperty("user", "dba");
        properties.setProperty("password", "nhn!@#123");
        Connection connection = driver.connect("jdbc:cubrid:10.101.57.233:30102:pinpoint:::", properties);

        logger.info("Connection class name:{}", connection.getClass().getName());
        logger.info("Connection class cl:{}", connection.getClass().getClassLoader());

        DatabaseInfo url = ((DatabaseInfoTraceValue)connection).__getTraceDatabaseInfo();
        Assert.assertNotNull(url);

        statement(connection);

        preparedStatement(connection);

        preparedStatement2(connection);

        preparedStatement3(connection);

        preparedStatement4(connection);

        preparedStatement5(connection);

        preparedStatement6(connection);

        preparedStatement7(connection);

        preparedStatement8(connection);


        connection.close();
        DatabaseInfo clearUrl = ((DatabaseInfoTraceValue)connection).__getTraceDatabaseInfo();
        Assert.assertNull(clearUrl);

    }

    private Driver loadClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<Driver> driverClazz = (Class<Driver>) loader.loadClass("cubrid.jdbc.driver.CUBRIDDriver");

        Driver driver = driverClazz.newInstance();
        logger.info("Driver class name:{}", driverClazz.getName());
        logger.info("Driver class cl:{}", driverClazz.getClassLoader());


        Class<?> version = loader.loadClass("com.nhn.pinpoint.common.Version");
        Assert.assertSame("check classLoader", this.getClass().getClassLoader(), version.getClassLoader());
        logger.debug("common cl:{}", version.getClassLoader());
        return driver;
    }


    private void statement(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.executeQuery("select 1");
        statement.close();
    }

    private void preparedStatement(Connection connection) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("select 1");
        logger.info("PreparedStatement className:" + preparedStatement.getClass().getName());
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
        logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement5(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", new String[]{"test"});
        logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement6(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        int[] columnIndex = {1,2,3};
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", columnIndex);
        logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement7(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

    private void preparedStatement8(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
//        ResultSet.HOLD_CURSORS_OVER_COMMIT or ResultSet.CLOSE_CURSORS_AT_COMMIT
        PreparedStatement preparedStatement = connection.prepareStatement("select 1", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

}
