/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.logging.jul;

import com.navercorp.pinpoint.common.util.Assert;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Woonduk Kang(emeroad)
 */
public class JulAdaptorHandler extends Handler {

    private final LoggerContext loggerContext;

    public JulAdaptorHandler(LoggerContext loggerContext) {
        this.loggerContext = Assert.requireNonNull(loggerContext, "loggerContext");
    }

    @Override
    public void publish(LogRecord record) {
        final String loggerName = record.getLoggerName();
        final Logger logger = loggerContext.getLogger(loggerName);

        final Level level = JulLogLevel.toLevel(record.getLevel().intValue());
        if (logger.isEnabled(level)) {

            String message = format(record);
            final Throwable thrown = record.getThrown();
            if (thrown == null) {
                logger.log(level, message);
            } else {
                logger.log(level, message, thrown);
            }
        }
    }

    String format(LogRecord record) {
        final String message = record.getMessage();
        if (message == null) {
            return "";
        }
        final Object[] parameters = record.getParameters();
        if (parameters == null || parameters.length == 0) {
            return message;
        }
        if (checkPattern(message)) {
            return MessageFormat.format(message, parameters);
        }
        return message;
    }

    private boolean checkPattern(String message) {
        return message.indexOf("{0") >= 0 || message.indexOf("{1") >= 0 ||
                message.indexOf("{2") >= 0 || message.indexOf("{3") >= 0;
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}