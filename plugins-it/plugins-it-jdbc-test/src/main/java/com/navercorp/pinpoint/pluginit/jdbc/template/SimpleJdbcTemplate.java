package com.navercorp.pinpoint.pluginit.jdbc.template;

import com.navercorp.pinpoint.pluginit.jdbc.JdbcUtils;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class SimpleJdbcTemplate {
    private final DataSource datasource;
    private final ConnectionInterceptor connectionInterceptor;

    public SimpleJdbcTemplate(DataSource dataSource) {
        this.datasource = dataSource;
        this.connectionInterceptor = new AutoCommitConnectionInterceptor();
    }
    public SimpleJdbcTemplate(DataSource dataSource, ConnectionInterceptor connectionInterceptor) {
        this.datasource = dataSource;
        this.connectionInterceptor = connectionInterceptor;
    }

    private Connection getConnection() throws SQLException {
        final Connection connection = datasource.getConnection();
        connectionInterceptor.before(connection);
        return connection;
    }

    private void closeConnection(Connection connection) throws SQLException {
        try {
            connectionInterceptor.after(connection);
        } catch (SQLException e) {
            // throw e??
            throw e;
        } finally {
            JdbcUtils.closeConnection(connection);
        }
    }


    public <T> T executeQuery(String sql, ResultSetExtractor<T> resultExtractor) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            return resultExtractor.extractData(resultSet);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(statement);
            closeConnection(connection);
        }
    }



    public int executeUpdate(String sql) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            return statement.executeUpdate(sql);
        } finally {
            JdbcUtils.closeStatement(ps);
            closeConnection(connection);
        }
    }


    // preparedStatement
    public <T> T executeQuery(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> resultExtractor) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement(sql);
            pss.setValues(ps);
            resultSet = ps.executeQuery();
            return resultExtractor.extractData(resultSet);
        } finally {
            JdbcUtils.closeResultSet(resultSet);
            JdbcUtils.closeStatement(ps);
            closeConnection(connection);
        }
    }


    public boolean execute(String sql, PreparedStatementSetter pss) throws SQLException {
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = getConnection();
            ps = connection.prepareStatement(sql);
            pss.setValues(ps);
            return ps.execute();
        } finally {
            JdbcUtils.closeStatement(ps);
            closeConnection(connection);
        }
    }

    public <T> T execute(String sql, CallableStatementCallback<T> callback) throws SQLException {
        Connection connection = null;
        CallableStatement cs = null;
        try {
            connection = getConnection();
            cs = connection.prepareCall(sql);
            return callback.doInCallableStatement(cs);
        } finally {
            JdbcUtils.closeStatement(cs);
            closeConnection(connection);
        }
    }


    public interface ConnectionInterceptor {
        ConnectionInterceptor EMPTY = new ConnectionInterceptor() {
            @Override
            public void before(Connection connection) throws SQLException {

            }

            @Override
            public void after(Connection connection) throws SQLException {

            }
        };

        void before(Connection connection) throws SQLException;

        void after(Connection connection) throws SQLException;
    }

    public static class AutoCommitConnectionInterceptor implements ConnectionInterceptor {
        public void before(Connection connection) throws SQLException {
            connection.setAutoCommit(false);
        }

        public void after(Connection connection) throws SQLException {
            connection.commit();
        }
    }


}
