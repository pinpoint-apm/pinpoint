package com.navercorp.pinpoint.pluginit.jdbc.testcontainers;

import com.navercorp.pinpoint.pluginit.jdbc.DriverProperties;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.util.Objects;
import java.util.Properties;

public final class DatabaseContainers {

    private DatabaseContainers() {
    }

    public static Properties toProperties(JdbcDatabaseContainer container) {
        Objects.requireNonNull(container, "container");

        Properties properties = new Properties();
        properties.setProperty(DriverProperties.URL, container.getJdbcUrl());
        properties.setProperty(DriverProperties.USER, container.getUsername());
        properties.setProperty(DriverProperties.PASSWARD, container.getPassword());
        properties.setProperty(DriverProperties.HOST, container.getHost());
        properties.setProperty(DriverProperties.PORT, container.getFirstMappedPort().toString());

        try {
            properties.setProperty(DriverProperties.DATABASE, container.getDatabaseName());
        } catch (UnsupportedOperationException ignored) {
        }

        return properties;
    }

    public static Properties toProperties(String url, String user, String password) {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(user, "user");
        Objects.requireNonNull(password, "password");

        Properties properties = new Properties();
        properties.setProperty(DriverProperties.URL, url);
        properties.setProperty(DriverProperties.USER, user);
        properties.setProperty(DriverProperties.PASSWARD, password);
        return properties;
    }

    public static DriverProperties readDriverProperties(Properties properties) {
        Objects.requireNonNull(properties, "properties");

        String url = properties.getProperty(DriverProperties.URL);
        String user = properties.getProperty(DriverProperties.USER);
        String password = properties.getProperty(DriverProperties.PASSWARD);
        return new DriverProperties(url, user, password, properties);
    }

}
