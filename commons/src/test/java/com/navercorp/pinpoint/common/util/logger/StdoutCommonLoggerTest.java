/*
 * Copyright 2016 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.navercorp.pinpoint.common.util.logger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StdoutCommonLoggerTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());


    @Test
    public void testLogging() {
        LoggerHolder loggerHolder = new LoggerHolder("StdoutCommonLoggerTest");
        CommonLogger commonLogger = loggerHolder.getLogger();

        commonLogger.debug("info test");
//        assertMessage(getOut(), null);

        commonLogger.info("info test 1");
        assertMessage(loggerHolder.getOut(), "info test 1");

        commonLogger.info("info test 2");
        assertMessage(loggerHolder.getOut(), "info test 2");

        commonLogger.warn("warn test");
        assertMessage(loggerHolder.getError(), "warn test");

        commonLogger.warn("warn test error", new Exception("testException"));
        assertMessage(loggerHolder.getError(), "warn test");
    }

    private void assertMessage(String out, String message) {
        logger.debug("log-message {}", out);
        Assert.assertTrue(out.contains(message));

    }


    private static class LoggerHolder {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private final PrintStream out = new PrintStream(outputStream);

        private final ByteArrayOutputStream errOutputStream = new ByteArrayOutputStream();
        private final PrintStream err = new PrintStream(errOutputStream);

        private final StdoutCommonLogger logger;

        public LoggerHolder(String loggerName) {
            logger = new StdoutCommonLogger(loggerName, out, err);
        }

        public CommonLogger getLogger() {
            return logger;
        }

        private String getOut() {
            return getLogMessage(this.outputStream);
        }

        private String getError() {
            return getLogMessage(this.errOutputStream);
        }

        private String getLogMessage(ByteArrayOutputStream byteArrayOutputStream) {
            String message = byteArrayOutputStream.toString();
            byteArrayOutputStream.reset();
            return message;
        }
    }

}