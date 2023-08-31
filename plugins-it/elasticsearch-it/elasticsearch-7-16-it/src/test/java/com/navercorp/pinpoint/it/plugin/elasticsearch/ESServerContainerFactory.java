package com.navercorp.pinpoint.it.plugin.elasticsearch;

import com.navercorp.pinpoint.it.plugin.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.util.function.Consumer;

public class ESServerContainerFactory {

    public static final int DEFAULT_PORT = 9200;

    public static ElasticsearchContainer newESServerContainerFactory(String loggerName) {
        ElasticsearchContainer elasticsearchContainer = new ElasticsearchContainer();
        elasticsearchContainer.withLogConsumer(new Consumer<OutputFrame>() {
            private final Logger logger = LogManager.getLogger(loggerName);
            @Override
            public void accept(OutputFrame outputFrame) {
                logger.info(LogUtils.removeLineBreak(outputFrame.getUtf8String()));
            }
        });
        return elasticsearchContainer;
    }
}
