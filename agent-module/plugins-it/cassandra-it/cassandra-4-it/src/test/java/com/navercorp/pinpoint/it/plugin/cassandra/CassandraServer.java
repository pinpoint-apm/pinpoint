package com.navercorp.pinpoint.it.plugin.cassandra;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.CassandraContainer;

import java.util.Objects;
import java.util.Properties;

public class CassandraServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    private final String dockerImageVersion;

    public CassandraContainer<?> cassandra;

    public CassandraServer(String dockerImageVersion) {
        this.dockerImageVersion = Objects.requireNonNull(dockerImageVersion, "dockerImageVersion");
    }

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        cassandra = new CassandraContainer<>(dockerImageVersion);
        cassandra.withInitScript("cassandra-init.sql");
        cassandra.start();

        final Integer port = cassandra.getMappedPort(CassandraContainer.CQL_PORT);
        Properties properties = new Properties();
        properties.setProperty("PORT", port.toString());
        System.setProperty("PORT", port.toString());

        return properties;
    }

    @Override
    public void afterAll() {
        if (cassandra != null) {
            cassandra.stop();
        }
    }
}
