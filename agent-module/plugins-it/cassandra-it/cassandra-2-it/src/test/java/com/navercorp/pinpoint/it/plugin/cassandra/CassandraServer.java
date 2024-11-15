package com.navercorp.pinpoint.it.plugin.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.cassandra.CassandraContainer;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Properties;

public class CassandraServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    private final String dockerImageVersion;

    public CassandraContainer cassandra;

    public CassandraServer(String dockerImageVersion) {
        this.dockerImageVersion = Objects.requireNonNull(dockerImageVersion, "dockerImageVersion");
    }

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        cassandra = new CassandraContainer(dockerImageVersion);
        cassandra.start();

        InetSocketAddress contactPoint = cassandra.getContactPoint();
        try (Cluster cluster = newCluster("127.0.0.1", contactPoint.getPort())) {
            init(cluster);
        }

        Properties properties = new Properties();
        properties.setProperty("PORT", String.valueOf(contactPoint.getPort()));
        System.getProperties().putAll(properties);
        return properties;
    }

    public void init(Cluster cluster) {
        try (Session systemSession = cluster.connect()) {
            String createKeyspace = String.format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = " +
                    "{'class':'SimpleStrategy','replication_factor':'1'};", CassandraITConstants.TEST_KEYSPACE);
            systemSession.execute(createKeyspace);
            String createTable = String.format("CREATE TABLE %s.%s (id text, value text, PRIMARY KEY(id))",
                    CassandraITConstants.TEST_KEYSPACE, CassandraITConstants.TEST_TABLE);
            systemSession.execute(createTable);
        }
    }

    public Cluster newCluster(String host, int port) {
        Cluster.Builder builder = Cluster.builder();
        builder.addContactPoint(host);
        builder.withPort(port);
        builder.withoutMetrics();
        return builder.build();
    }

    @Override
    public void afterAll() {
        if (cassandra != null) {
            cassandra.stop();
        }
    }
}
