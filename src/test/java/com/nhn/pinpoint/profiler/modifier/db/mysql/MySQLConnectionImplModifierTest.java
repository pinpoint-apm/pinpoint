package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.mysql.jdbc.JDBC4PreparedStatement;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;

import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;


import com.nhn.pinpoint.bootstrap.util.MetaObject;
import com.nhn.pinpoint.profiler.util.MockAgent;
import com.nhn.pinpoint.profiler.util.TestClassLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.Properties;

/**
 * @author emeroad
 */
public class MySQLConnectionImplModifierTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private TestClassLoader loader;


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

    private MetaObject<DatabaseInfo> getUrl = new MetaObject<DatabaseInfo>("__getDatabaseInfo");

    @Test
    public void testModify() throws Exception {

        Driver driver = loadClass();

        Properties properties = new Properties();
        properties.setProperty("user", "lucytest");
        properties.setProperty("password", "testlucy");
        Connection connection = driver.connect("jdbc:mysql://10.98.133.22:3306/hippo", properties);

        logger.info("Connection class name:{}", connection.getClass().getName());
        logger.info("Connection class cl:{}", connection.getClass().getClassLoader());

        DatabaseInfo url = getUrl.invoke(connection);
        Assert.assertNotNull(url);

        statement(connection);

        preparedStatement(connection);

        preparedStatement2(connection);

        preparedStatement3(connection);

        connection.close();
        DatabaseInfo clearUrl = getUrl.invoke(connection);
        Assert.assertNull(clearUrl);

    }

    private Driver loadClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<Driver> driverClazz = (Class<Driver>) loader.loadClass("com.mysql.jdbc.NonRegisteringDriver");
//        Driver nonRegisteringDriver = new NonRegisteringDriver();
//        Class<Driver> driverClazz = (Class<Driver>) nonRegisteringDriver.getClass();

        Driver driver = driverClazz.newInstance();
        logger.info("Driver class name:{}", driverClazz.getName());
        logger.info("Driver class cl:{}", driverClazz.getClassLoader());


        Class<?> aClass = loader.loadClass("com.mysql.jdbc.StringUtils");
//      이게 loader와 동일하게 로드 되는게 정확한건지 애매함. 하위에 로드되는게 좋을것 같은데.
//        Assert.assertNotSame("check classLoader", aClass.getClassLoader(), loader);
        logger.debug("mysql cl:{}", aClass.getClassLoader());

        Class<?> version = loader.loadClass("com.nhn.pinpoint.common.Version");
        Assert.assertSame("check classLoader", this.getClass().getClassLoader(), version.getClassLoader());
        logger.debug("common cl:{}", version.getClassLoader());
        return driver;
    }

    @Test
    public void loadBalancedUrlModify() throws Exception {

        Driver driver = loadClass();

        Properties properties = new Properties();
        properties.setProperty("user", "lucytest");
        properties.setProperty("password", "testlucy");
        Connection connection = driver.connect("jdbc:mysql:loadbalance://10.98.133.23:3306,10.98.133.22:3306/hippo", properties);

        logger.info("Connection class name:{}", connection.getClass().getName());
        logger.info("Connection class cl:{}", connection.getClass().getClassLoader());
        // loadbalanced 타입을때 가져올수 있는 reflection을 쓰지 않으면
        // 그리고 LoadBalancingConnectionProxy으로 캐스팅할 경우 classLoader가 달라서 문제가 생김. 일단 그냥둔다.
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(connection);
        Class<? extends InvocationHandler> aClass = invocationHandler.getClass();

        Field current = aClass.getDeclaredField("currentConn");
        current.setAccessible(true);
        Object internalConnection = current.get(invocationHandler);


        DatabaseInfo url = getUrl.invoke(internalConnection);
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
        DatabaseInfo clearUrl = getUrl.invoke(internalConnection);
        Assert.assertNull(clearUrl);

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

    private void preparedStatementError(Connection connection) throws SQLException {
//        Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("select 8 from invalidTable", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            logger.info("PreparedStatement className:{}", preparedStatement.getClass().getName());
            resultSet = preparedStatement.executeQuery();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }

        }
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


    @Test
    public void test() throws NoSuchMethodException {
//        setNClob(int parameterIndex, NClob value)
        JDBC4PreparedStatement.class.getDeclaredMethod("setNClob", new Class[]{int.class, NClob.class});
//        JDBC4PreparedStatement.class.getDeclaredMethod("addBatch", null);
        JDBC4PreparedStatement.class.getMethod("addBatch");

    }
}
