package com.nhn.pinpoint.profiler.modifier.db.mysql;

import com.mysql.jdbc.JDBC4PreparedStatement;
import com.mysql.jdbc.NonRegisteringDriver;
import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.nhn.pinpoint.common.util.PropertyUtils;
import com.nhn.pinpoint.profiler.junit4.BasePinpointTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
public class MySQLConnectionImplModifierTest extends BasePinpointTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static Properties db;

    @BeforeClass
    public static void beforeClass() throws Exception {
        db = PropertyUtils.loadPropertyFromClassPath("database.properties");
    }

    @Test
    public void testModify() throws Exception {

        Connection connection = connectDB(db.getProperty("mysql.url"));

        logger.info("Connection class name:{}", connection.getClass().getName());
        logger.info("Connection class cl:{}", connection.getClass().getClassLoader());

        DatabaseInfo url = ((DatabaseInfoTraceValue) connection).__getTraceDatabaseInfo();
        Assert.assertNotNull(url);

        statement(connection);

        preparedStatement(connection);

        preparedStatement2(connection);

        preparedStatement3(connection);

        connection.close();

        DatabaseInfo clearUrl = ((DatabaseInfoTraceValue) connection).__getTraceDatabaseInfo();
        Assert.assertNull(clearUrl);

    }

    private Connection connectDB(String url) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        String user = db.getProperty("mysql.user");
        String password = db.getProperty("mysql.password");

        Driver driver = new NonRegisteringDriver();
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        return driver.connect(url, properties);
    }

	@Ignore
    @Test
    public void loadBalancedUrlModify() throws Exception {
		// random fail
        Connection connection = connectDB(db.getProperty("mysql.url.loadbalance"));

        logger.info("Connection class name:{}", connection.getClass().getName());
        logger.info("Connection class cl:{}", connection.getClass().getClassLoader());
        // loadbalanced 타입을때 가져올수 있는 reflection을 쓰지 않으면
        // 그리고 LoadBalancingConnectionProxy으로 캐스팅할 경우 classLoader가 달라서 문제가 생김. 일단 그냥둔다.
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(connection);
        Class<? extends InvocationHandler> aClass = invocationHandler.getClass();

        Field current = aClass.getDeclaredField("currentConn");
        current.setAccessible(true);
        Object internalConnection = current.get(invocationHandler);


        DatabaseInfo url = ((DatabaseInfoTraceValue) internalConnection).__getTraceDatabaseInfo();
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
        DatabaseInfo clearUrl = ((DatabaseInfoTraceValue) internalConnection).__getTraceDatabaseInfo();
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
        int[] columnIndex = {1, 2, 3};
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
