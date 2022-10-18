package com.navercorp.pinpoint.plugin.rabbitmq;

import com.navercorp.pinpoint.plugin.rabbitmq.util.TestBroker;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.junit.Assume;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Properties;

public class TestBrokerServer implements SharedTestLifeCycle {
    private RabbitMQContainer container;

    @Override
    public Properties beforeAll() {
        Assume.assumeTrue("Docker not enabled", DockerClientFactory.instance().isDockerAvailable());
        container = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.7.25-management-alpine"));

        container.start();

        int port = container.getFirstMappedPort();
        Properties properties = new Properties();
        properties.setProperty("PORT", String.valueOf(port));
        return properties;
    }

    @Override
    public void afterAll() {
        if (container != null) {
            container.stop();
        }
    }
}
