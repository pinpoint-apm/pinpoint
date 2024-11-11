package com.pinpoint.test.plugin.kafka;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaTest {
    private static KafkaContainer container;

    @BeforeAll
    public static void beforeClass() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        container = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

        container.start();
        final String bootstrapServers = container.getBootstrapServers();
        final int port = container.getFirstMappedPort();
    }

    @AfterAll
    public static void afterClass() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    public void test() throws Exception {
        System.out.println("TEST");
    }
}
