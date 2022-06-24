package com.navercorp.pinpoint.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assume;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Properties;

public class PostgreSqlServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    private PostgreSQLContainer postgreSql;

    @Override
    public Properties beforeAll() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        postgreSql = PostgreSQLContainerFactory.newContainer(logger.getName());

        postgreSql.start();

        Properties properties = new Properties();
        properties.setProperty("JDBC_URL", postgreSql.getJdbcUrl());
        properties.setProperty("USERNAME", postgreSql.getUsername());
        properties.setProperty("PASSWORD", postgreSql.getPassword());
        return properties;
    }

    @Override
    public void afterAll() {
        if (postgreSql != null) {
            postgreSql.stop();
        }
    }
}
