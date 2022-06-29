package com.pinpoint.test.plugin;

import com.navercorp.pinpoint.pluginit.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.function.Consumer;


@Component
public class ElasticSearchServer {
    private final Logger logger = LogManager.getLogger(this.getClass());
    public ElasticsearchContainer elasticSearchContainer;

    private final String address = "127.0.0.1";

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return elasticSearchContainer.getMappedPort(9200);
    }

    @PostConstruct
    public void init() {
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
