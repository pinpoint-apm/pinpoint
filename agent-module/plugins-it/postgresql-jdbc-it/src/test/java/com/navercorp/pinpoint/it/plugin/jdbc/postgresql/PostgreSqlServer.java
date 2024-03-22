package com.navercorp.pinpoint.it.plugin.jdbc.postgresql;

import com.navercorp.pinpoint.it.plugin.utils.jdbc.testcontainers.DatabaseContainers;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assume;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Properties;

public class PostgreSqlServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    private PostgreSQLContainer<?> postgreSql;

    @Override
    public Properties beforeAll() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        postgreSql = PostgreSQLContainerFactory.newContainer(logger.getName());

        postgreSql.start();

        return DatabaseContainers.toProperties(postgreSql);
    }

    @Override
    public void afterAll() {
        if (postgreSql != null) {
            postgreSql.stop();
        }
    }
}
