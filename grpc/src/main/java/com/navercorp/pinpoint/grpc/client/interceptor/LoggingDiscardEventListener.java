/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc.client.interceptor;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.logging.ThrottledLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LoggingDiscardEventListener<ReqT> implements DiscardEventListener<ReqT> {
    private final ThrottledLogger logger;


    public LoggingDiscardEventListener(String loggerName, long rateLimitCount) {
        Assert.requireNonNull(loggerName, "loggerName");
        Logger log = LoggerFactory.getLogger(loggerName);
        this.logger = ThrottledLogger.getLogger(log, rateLimitCount);
    }

    @Override
    public void onDiscard(ReqT message, String cause) {
        logDiscardMessage(message, cause);
    }

    private void logDiscardMessage(ReqT message, String cause) {
        if (logger.isInfoEnabled()) {
            logger.info("Discard {} message, {}. discardCount:{}", getMessageType(message), cause, logger.getCounter() + 1);
        }
    }

    @Override
    public void onCancel(String message, Throwable throwable) {
        logger.info("Cancel message. message={}, cause={}", message, throwable.getMessage(), throwable);
    }

    private String getMessageType(ReqT message) {
        if (message == null) {
            return "null";
        }
        return message.getClass().getSimpleName();
    }

    public long getDiscardCount() {
        return logger.getCounter();
    }
}
