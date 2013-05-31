package com.nhn.pinpoint.modifier.db.mysql;

import com.mysql.jdbc.JDBC4PreparedStatement;
import com.nhn.pinpoint.DefaultAgent;
import com.nhn.pinpoint.DummyInstrumentation;
import com.profiler.config.ProfilerConfig;

import com.nhn.pinpoint.logging.Slf4jLoggerBinder;
import com.profiler.modifier.db.DatabaseInfo;

import com.profiler.util.MetaObject;
import com.nhn.pinpoint.util.TestClassLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

public class MySQLConnectionImplModifierTest {

    private final Logger logger = LoggerFactory.getLogger(MySQLConnectionImplModifierTest.class.getName());

    private TestClassLoader loader;


    @Before
    public void setUp() throws Exception {

        com.profiler.logging.LoggerFactory.initialize(new Slf4jLoggerBinder());

        ProfilerConfig profilerConfig = new ProfilerConfig();
        DefaultAgent agent = new DefaultAgent("", new DummyInstrumentation(), profilerConfig);
        loader = new TestClassLoader(agent);


        MySQLNonRegisteringDriverModifier driverModifier = new MySQLNonRegisteringDriverModifier(loader.getInstrumentor(), agent);
        loader.addModifier(driverModifier);

        MySQLConnectionImplModifier connectionModifier = new MySQLConnectionImplModifier(loader.getInstrumentor(), agent);
        loader.addModifier(connectionModifier);

        MySQLStatementModifier statementModifier = new MySQLStatementModifier(loader.getInstrumentor(), agent);
        loader.addModifier(statementModifier);

        MySQLPreparedStatementModifier preparedStatementModifier = new MySQLPreparedStatementModifier(loader.getInstrumentor(), agent);
        loader.addModifier(preparedStatementModifier);

        MySQLPreparedStatementJDBC4Modifier preparedStatementJDBC4Modifier = new MySQLPreparedStatementJDBC4Modifier(loader.getInstrumentor(), agent);
        loader.addModifier(preparedStatementJDBC4Modifier);



        loader.initialize();
    }

    private MetaObject<DatabaseInfo> getUrl = new MetaObject<DatabaseInfo>("__getUrl");

    @Test
    public void testModify() throws Exception {

        Class<Driver> driverClazz = (Class<Driver>) loader.loadClass("com.mysql.jdbc.NonRegisteringDriver");
        Driver driver = driverClazz.newInstance();
        logger.info("Driver class name:" + driverClazz.getName());
        logger.info("Driver class cl:" + driverClazz.getClassLoader());

        Properties properties = new Properties();
        properties.setProperty("user", "lucytest");
        properties.setProperty("password", "testlucy");



        Connection connection = driver.connect("jdbc:mysql://10.98.133.22:3306/hippo", properties);

        logger.info("Connection class name:" + connection.getClass().getName());
        logger.info("Connection class cl:" + connection.getClass().getClassLoader());

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

    @Test
    public void test() throws NoSuchMethodException {
//        setNClob(int parameterIndex, NClob value)
        JDBC4PreparedStatement.class.getDeclaredMethod("setNClob", new Class[]{int.class, NClob.class});
//        JDBC4PreparedStatement.class.getDeclaredMethod("addBatch", null);
        JDBC4PreparedStatement.class.getMethod("addBatch");

    }
}
