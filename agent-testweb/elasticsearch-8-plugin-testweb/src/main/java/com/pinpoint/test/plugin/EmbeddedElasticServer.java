package com.pinpoint.test.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.io.IOException;

import static java.util.concurrent.TimeUnit.MINUTES;

@Component
public class EmbeddedElasticServer {
    private final Logger logger = LogManager.getLogger(this.getClass());
    public EmbeddedElastic embeddedElastic;

    private final String address = "127.0.0.1";
    private final int port = 9200;

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @PostConstruct
    public void init() throws IOException, InterruptedException {
        logger.info("EmbeddedElasticServer init");
        embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("6.8.0")
                .withSetting(PopularProperties.HTTP_PORT, port)
                .withEsJavaOpts("-Xms128m -Xmx512m")
                .withStartTimeout(2, MINUTES)
                .build()
                .start();
    }

    @PreDestroy
    private void shutdown() {
        logger.info("EmbeddedElasticServer destroy");
        if (embeddedElastic != null) {
            embeddedElastic.stop();
        }
    }
}
