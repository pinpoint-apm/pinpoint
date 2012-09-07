package com.profiler.modifier.db.mysql;

import com.profiler.context.Trace;
import com.profiler.modifier.db.ConnectionTrace;
import com.profiler.util.TestClassLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class MySQLConnectionImplModifierTest {

    private final Logger logger = Logger.getLogger(MySQLConnectionImplModifierTest.class.getName());

    private TestClassLoader loader;
    @Before
    public void setUp() throws Exception {
        loader = new TestClassLoader();

        MySQLConnectionImplModifier connectionModifier = new MySQLConnectionImplModifier(loader.getInstrumentor());
        loader.addModifier(connectionModifier);

        MySQLStatementModifier statementModifier = new MySQLStatementModifier(loader.getInstrumentor());
        loader.addModifier(statementModifier);

        MySQLPreparedStatementModifier preparedStatementModifier = new MySQLPreparedStatementModifier(loader.getInstrumentor());
        loader.addModifier(preparedStatementModifier);

//        loader.delegateLoadingOf(ConnectionTrace.class.getName());

        loader.initialize();
    }

    @Test
    public void testModify() throws Exception {

        Class<Driver> driverClazz = (Class<Driver>) loader.loadClass("com.mysql.jdbc.NonRegisteringDriver");
        Driver driver = driverClazz.newInstance();
        logger.info("Driver class name:" + driverClazz.getName());
        logger.info("Driver class cl:" + driverClazz.getClassLoader());

        Properties properties = new Properties();
        properties.setProperty("user", "lucytest");
        properties.setProperty("password", "testlucy");
        Connection connect = driver.connect("jdbc:mysql://10.98.133.22:3306/hippo", properties);

        Trace.getTraceIdOrCreateNew();
        logger.info("Connection class name:" + connect.getClass().getName());
        logger.info("Connection class cl:" + connect.getClass().getClassLoader());

        Set<Connection> connectionList = ConnectionTrace.getConnectionTrace().getConnectionList();
        Assert.assertEquals(connectionList.size(), 1);
        logger.info("connection size:" + connectionList.size());

        statement(connect);

        preparedStatement(connect);

        connect.close();
        Assert.assertEquals(connectionList.size(), 0);
        logger.info("connection size:" + connectionList.size());

        Trace.removeCurrentTraceIdFromStack();
    }

    private void statement(Connection connect) throws SQLException {
        Statement statement = connect.createStatement();
        statement.executeQuery("select 1");
        statement.close();
    }

    private void preparedStatement(Connection connect) throws SQLException {
        PreparedStatement preparedStatement = connect.prepareStatement("select 1");
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.close();
        preparedStatement.close();
    }

}
