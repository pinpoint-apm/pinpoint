package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.nhn.pinpoint.common.util.PropertyUtils;
import com.nhn.pinpoint.test.junit4.BasePinpointTest;

import cubrid.jdbc.driver.CUBRIDDriver;

/**
 * @author emeroad
 */
@Ignore
public class CubridConnectionIT extends BasePinpointTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static Properties db;

    @BeforeClass
    public static void beforeClass() throws Exception {
        db = PropertyUtils.loadPropertyFromClassPath("database.properties");
    }

    @Test
    public void executeQueryAndExecuteUpdate() throws SQLException {
        Connection connection = connectDB();

        PreparedStatement preparedStatement = connection.prepareStatement("select 1 from db_root where 1=?");
        preparedStatement.setInt(1, 1);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            logger.debug("---{}", resultSet.getObject(1));
        }
        connection.close();
    }

    private Connection connectDB() throws SQLException {
        String url = db.getProperty("cubrid.url");
        String user = db.getProperty("cubrid.user");
        String password = db.getProperty("cubrid.password");

        Driver driver = new CUBRIDDriver();
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        return driver.connect(url, properties);
    }


    @Test
    public void testModify() throws Exception {

        Connection connection = connectDB();

        logger.info("Connection class name:{}", connection.getClass().getName());
        logger.info("Connection class cl:{}", connection.getClass().getClassLoader());

        DatabaseInfo url = ((DatabaseInfoTraceValue) connection).__getTraceDatabaseInfo();
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
        DatabaseInfo clearUrl = ((DatabaseInfoTraceValue) connection).__getTraceDatabaseInfo();
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
