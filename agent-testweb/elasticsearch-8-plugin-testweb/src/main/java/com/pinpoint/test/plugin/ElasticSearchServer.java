package com.pinpoint.test.plugin;

import com.navercorp.pinpoint.it.plugin.utils.LogUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.IOException;
import java.util.function.Consumer;

@Component
public class ElasticSearchServer {
    private final Logger logger = LogManager.getLogger(this.getClass());
    public ElasticsearchContainer elasticSearchContainer;

    private static final String ADDRESS = "127.0.0.1";

    public String getAddress() {
        return ADDRESS;
    }

    public int getPort() {
        return elasticSearchContainer.getMappedPort(9200);
    }

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        logger.info("ElasticSearchServer init");
        if (!DockerClientFactory.instance().isDockerAvailable()) {
            throw new IllegalStateException("Docker not enabled");
        }

        elasticSearchContainer = new ElasticsearchContainer();
        elasticSearchContainer.withLogConsumer(new Consumer<OutputFrame>() {
            @Override
            public void accept(OutputFrame outputFrame) {
                logger.info(LogUtils.removeLineBreak(outputFrame.getUtf8String()));
            }
        });
        elasticSearchContainer.start();
        logger.info("host:{} port:{}", elasticSearchContainer.getHttpHostAddress(), getPort());
    }

    @PreDestroy
    private void shutdown() {
        logger.info("ElasticSearchServer destroy");
        if (elasticSearchContainer != null) {
            elasticSearchContainer.stop();
        }
    }
}
