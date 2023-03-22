package com.navercorp.pinpoint.pinot.kafka.util;

import org.apache.logging.log4j.Logger;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

public final class KafkaCallbacks {

    public static <T> ListenableFutureCallback<SendResult<String, T>> loggingCallback(String name, Logger logger) {
        return new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                logger.warn("{} onFailure:{}", name, ex.getMessage(), ex);
            }

            @Override
            public void onSuccess(SendResult<String, T> result) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} onSuccess:{}", name, result);
                }
            }
        };
    }
}
