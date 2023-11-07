package com.navercorp.pinpoint.pinot.kafka.util;

import org.apache.logging.log4j.Logger;
import org.springframework.kafka.support.SendResult;

import java.util.function.BiConsumer;

public final class KafkaCallbacks {

    public static <T> BiConsumer<SendResult<String, T>, Throwable> loggingCallback(String name, Logger logger) {
        return new BiConsumer<>() {
            @Override
            public void accept(SendResult<String, T> result, Throwable throwable) {
                if (throwable != null) {
                    logger.warn("{} onFailure:{}", name, throwable.getMessage(), throwable);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("{} onSuccess:{}", name, result);
                    }
                }
            }
        };
    }
}
