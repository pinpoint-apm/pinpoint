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

package com.google.common.logging;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LoggerTest {
    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private PrintStream out = new PrintStream(outputStream);

    private ByteArrayOutputStream errOutputStream = new ByteArrayOutputStream();
    private PrintStream err = new PrintStream(errOutputStream);

     public Logger newLogger(String loggerName, Level level) {
        this.outputStream = new ByteArrayOutputStream();
        this.out = new PrintStream(outputStream);

        this.errOutputStream = new ByteArrayOutputStream();
        this.err= new PrintStream(errOutputStream);

        return new Logger(loggerName, level, out, err);
    }

    @Test
    public void testLoggerLevel_WARNING() {
        Logger logger = new Logger("1", Level.WARNING);

        Assert.assertTrue(logger.isLoggable(Level.WARNING));
        Assert.assertTrue(logger.isLoggable(Level.SEVERE));

        Assert.assertFalse(logger.isLoggable(Level.INFO));
        Assert.assertFalse(logger.isLoggable(Level.FINE));

    }

    @Test
    public void testLoggerLevel_FINE() {
        Logger logger = new Logger("1", Level.INFO);

        Assert.assertTrue(logger.isLoggable(Level.INFO));
        Assert.assertTrue(logger.isLoggable(Level.WARNING));

        Assert.assertFalse(logger.isLoggable(Level.FINE));
        Assert.assertFalse(logger.isLoggable(Level.FINER));
    }

    @Test
    public void testLogger_1() {

        LoggerHolder loggerHolder = new LoggerHolder("test", Level.WARNING);

        Logger test = loggerHolder.getLogger();

        test.log(Level.WARNING, "warnMessage");
        String out = loggerHolder.getError();
        Assert.assertThat(out, containsString("[WARNING](test) warnMessage"));

        test.info("infoTest");
        Assert.assertEquals(loggerHolder.getOut(), "");

        test.warning("warningTest");
        Assert.assertThat(loggerHolder.getError(), containsString("warningTest"));
        Assert.assertThat(loggerHolder.getOut(), is(""));
    }

    @Test
    public void testLogger_exception() {

        LoggerHolder loggerHolder = new LoggerHolder("test", Level.WARNING);

        Logger test = loggerHolder.getLogger();

        test.log(Level.WARNING, "warnMessage", new Exception("testException"));
        String out = loggerHolder.getError();
        Assert.assertThat(out, allOf(containsString("[WARNING](test) warnMessage"), containsString("testException")));
    }


    private static class LoggerHolder {
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private final PrintStream out = new PrintStream(outputStream);

        private final ByteArrayOutputStream errOutputStream = new ByteArrayOutputStream();
        private final PrintStream err = new PrintStream(errOutputStream);

        private final Logger logger;

        public LoggerHolder(String loggerName, Level level) {
            logger = new Logger(loggerName, level, out, err);
        }

        public Logger getLogger() {
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