package com.navercorp.pinpoint.it.plugin.elasticsearch;

import com.navercorp.pinpoint.it.plugin.utils.LogOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class ESServerContainerFactory {

    public static final int DEFAULT_PORT = 9200;

    public static ElasticsearchContainer newESServerContainerFactory(String loggerName) {
        ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer("elasticsearch:7.17.19");

        Logger logger = LogManager.getLogger(loggerName);
        elasticsearchContainer.withLogConsumer(new LogOutputStream(logger::info));
        return elasticsearchContainer;
    }
}
