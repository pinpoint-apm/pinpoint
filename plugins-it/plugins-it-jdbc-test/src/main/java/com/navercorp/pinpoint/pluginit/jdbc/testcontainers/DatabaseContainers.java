package com.navercorp.pinpoint.pluginit.jdbc.testcontainers;

import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.Objects;
import java.util.Properties;

public final class DatabaseContainers {

    public static final String JDBC_URL = "JDBC_URL";
    public static final String USERNAME = "USERNAME";
    public static final String PASSWORD = "PASSWORD";

    private DatabaseContainers() {
    }

    public static Properties toProperties(JdbcDatabaseContainer container) {
        Objects.requireNonNull(container, "container");

        Properties properties = new Properties();
        properties.setProperty(JDBC_URL, container.getJdbcUrl());
        properties.setProperty(USERNAME, container.getUsername());
        properties.setProperty(PASSWORD, container.getPassword());
        return properties;
    }

    public static String getJdbcUrl(Properties p) {
        return p.getProperty(JDBC_URL);
    }

    public static String getUsername(Properties p) {
        return p.getProperty(USERNAME);
    }

    public static String getPassword(Properties p) {
        return p.getProperty(PASSWORD);
    }

}
