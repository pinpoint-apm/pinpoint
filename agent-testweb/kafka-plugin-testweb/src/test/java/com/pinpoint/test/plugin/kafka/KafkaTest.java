package com.pinpoint.test.plugin.kafka;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

public class KafkaTest {
    private static KafkaContainer container;

    @BeforeClass
    public static void beforeClass() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());

        container = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"));

        container.start();
        final String bootstrapServers = container.getBootstrapServers();
        final int port = container.getFirstMappedPort();
    }

    @AfterClass
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
