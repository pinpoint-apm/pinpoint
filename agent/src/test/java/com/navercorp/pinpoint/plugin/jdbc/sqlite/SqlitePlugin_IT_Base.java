package com.navercorp.pinpoint.plugin.jdbc.sqlite;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class SqlitePlugin_IT_Base {

    String SQLITE = "SQLITE";
    String SQLITE_EXECUTE_QUERY = "SQLITE_EXECUTE_QUERY";

    String JDBC_PREFIX = "jdbc:sqlite:";
    String MEMORY = ":memory:";

    String SQL_CREATE_TABLE = "create table person (id integer, name string)";
    String SQL_SELECT = "select * from person";
    String STMT_INSERT = "insert into person values(0#, '1$')";
    String PSTMT_INSERT = "insert into person values(?, ?)";

    private Connection connection;

    private Statement statement;

    private PreparedStatement preparedSatement;

    private ResultSet rs;

    protected void executeStmt() throws Exception {
        try {
            connection = DriverManager.getConnection(JDBC_PREFIX);
            statement = connection.createStatement();

            statement.execute(SQL_CREATE_TABLE);
            int updateCount = statement.executeUpdate("insert into person values(1, 'leo')");
            assertThat(updateCount, is(1));

            updateCount = statement.executeUpdate("insert into person values(2, 'yui')");
            assertThat(updateCount, is(1));

            rs = statement.executeQuery(SQL_SELECT);
            rs.next();
            assertThat(rs.getInt("id"), is(1));
            assertThat(rs.getString("name"), is("leo"));

            rs.next();
            assertThat(rs.getInt("id"), is(2));
            assertThat(rs.getString("name"), is("yui"));
        } finally {
            closeResultSet();
            closeStatement();
            closeConnection();
        }
    }

    protected void executePstmt() throws Exception {
        try {
            connection = DriverManager.getConnection(JDBC_PREFIX);

            preparedSatement =  connection.prepareStatement(SQL_CREATE_TABLE);
            preparedSatement.execute();
            preparedSatement.close();

            preparedSatement =  connection.prepareStatement(PSTMT_INSERT);
            preparedSatement.setInt(1, 1);
            preparedSatement.setString(2, "leo");
            int updateCount = preparedSatement.executeUpdate();
            assertThat(updateCount, is(1));

            preparedSatement.setInt(1, 2);
            preparedSatement.setString(2, "yui");
            updateCount = preparedSatement.executeUpdate();
            assertThat(updateCount, is(1));
            preparedSatement.close();

            preparedSatement =  connection.prepareStatement(SQL_SELECT);
            rs = preparedSatement.executeQuery();
            rs.next();
            assertThat(rs.getInt("id"), is(1));
            assertThat(rs.getString("name"), is("leo"));

            rs.next();
            assertThat(rs.getInt("id"), is(2));
            assertThat(rs.getString("name"), is("yui"));
        } finally {
            closeResultSet();
            closePreparedSatement();
            closeConnection();
        }
    }

    protected void executeTransaction() throws Exception {
        try {
            connection = DriverManager.getConnection(JDBC_PREFIX);
            connection.close();

            connection = DriverManager.getConnection(JDBC_PREFIX);
            connection.setAutoCommit(false);

            preparedSatement =  connection.prepareStatement(SQL_CREATE_TABLE);
            preparedSatement.execute();
            preparedSatement.close();

            connection.commit();

            preparedSatement =  connection.prepareStatement(PSTMT_INSERT);
            preparedSatement.setInt(1, 1);
            preparedSatement.setString(2, "leo");
            int updateCount = preparedSatement.executeUpdate();
            assertThat(updateCount, is(1));

            connection.rollback();

            preparedSatement.setInt(1, 2);
            preparedSatement.setString(2, "yui");
            updateCount = preparedSatement.executeUpdate();
            assertThat(updateCount, is(1));
            preparedSatement.close();

            connection.commit();

            preparedSatement =  connection.prepareStatement(SQL_SELECT);
            rs = preparedSatement.executeQuery();
            rs.next();
            assertThat(rs.getInt("id"), is(2));
            assertThat(rs.getString("name"), is("yui"));
        } finally {
            closeResultSet();
            closePreparedSatement();
            closeConnection();
        }
    }

    private void closeResultSet() throws SQLException {
        if(rs != null) {
            rs.close();
        }
    }

    private void closePreparedSatement() throws SQLException {
        if(preparedSatement != null) {
            preparedSatement.close();
        }
    }

    private void closeStatement() throws SQLException {
        if(statement != null) {
            statement.close();
        }
    }

    private void closeConnection() throws SQLException {
        if(connection != null) {
            connection.close();
        }
    }
}