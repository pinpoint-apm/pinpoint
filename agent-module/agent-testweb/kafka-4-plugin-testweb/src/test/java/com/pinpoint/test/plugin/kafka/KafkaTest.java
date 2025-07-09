package com.pinpoint.test.plugin.kafka;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.kafka.KafkaContainer;

@Disabled
public class KafkaTest {
    @AutoClose
    private static KafkaContainer container;

    @BeforeAll
    public static void beforeClass() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");

        container = new KafkaContainer("apache/kafka:3.8.1");
        container.start();
    }

    @Test
    public void test() throws Exception {
        String bootstrapServers = container.getBootstrapServers();
        System.out.println("Kafka bootstrapServers:" + bootstrapServers);
    }
}
