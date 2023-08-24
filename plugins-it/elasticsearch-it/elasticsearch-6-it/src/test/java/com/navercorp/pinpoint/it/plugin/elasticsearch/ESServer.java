package com.navercorp.pinpoint.it.plugin.elasticsearch;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assumptions;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.Properties;

public class ESServer implements SharedTestLifeCycle {
    private final Logger logger = LogManager.getLogger(getClass());

    public ElasticsearchContainer elasticsearchContainer;

    public ESServer() {
    }

    @Override
    public Properties beforeAll() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker not enabled");
        logger.info("ElasticsearchContainer start");

        elasticsearchContainer = ESServerContainerFactory.newESServerContainerFactory(logger.getName());
        elasticsearchContainer.start();

        Properties properties = new Properties();

        Integer port = elasticsearchContainer.getMappedPort(ESServerContainerFactory.DEFAULT_PORT);
        properties.setProperty("PORT", port.toString());
        System.setProperty("PORT", port.toString());
        return properties;
    }


    @Override
    public void afterAll() {
        logger.info("ElasticsearchContainer stop");
        if (elasticsearchContainer != null) {
            elasticsearchContainer.stop();
        }
    }
}
