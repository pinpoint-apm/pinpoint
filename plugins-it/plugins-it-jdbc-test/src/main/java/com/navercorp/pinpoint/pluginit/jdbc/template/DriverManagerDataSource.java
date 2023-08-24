package com.navercorp.pinpoint.pluginit.jdbc.template;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

public class DriverManagerDataSource implements DataSource {
    private String jdbcUrl;
    private final Properties properties;


    public DriverManagerDataSource(String jdbcUrl, String user, String password) {
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(password, "password");

        this.jdbcUrl = jdbcUrl;
        Properties properties = new Properties();
        properties.put("user", user);
        properties.put("password", password);
        this.properties = properties;
    }

    public DriverManagerDataSource(String jdbcUrl, Properties properties) {
        this.jdbcUrl = jdbcUrl;
        this.properties = properties;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, properties);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
