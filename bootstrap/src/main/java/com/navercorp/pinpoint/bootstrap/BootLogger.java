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

package com.navercorp.pinpoint.bootstrap;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class BootLogger {

//    private static final int LOG_LEVEL;
//    private final String loggerName;
    private final String messagePattern;
    private final PrintStream out;
    private final PrintStream err;


    static {
        setup();
    }

    private static void setup() {
        // TODO setup BootLogger LogLevel
        // low priority
//        String logLevel = System.getProperty("pinpoint.agent.bootlogger.loglevel");
//        logLevel = ???
    }


    public BootLogger(String loggerName) {
        this(loggerName, System.out, System.err);
    }

    // for test
    BootLogger(String loggerName, PrintStream out, PrintStream err) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName must not be null");
        }
//        this.loggerName = loggerName;
        this.messagePattern = "{0,date,yyyy-MM-dd HH:mm:ss} [{1}](" + loggerName + ") {2}{3}";
        this.out = out;
        this.err = err;
    }

    static BootLogger getLogger(String loggerName) {
        return new BootLogger(loggerName);
    }

    private String format(String logLevel, String msg, String exceptionMessage) {
        exceptionMessage = defaultString(exceptionMessage, "");

        MessageFormat messageFormat = new MessageFormat(messagePattern);
        final long date = System.currentTimeMillis();
        Object[] parameter = {date, logLevel, msg, exceptionMessage};
        return messageFormat.format(parameter);
    }

    public boolean isInfoEnabled() {
        return true;
    }

    public void info(String msg) {
        String formatMessage = format("INFO ", msg, "");
        this.out.println(formatMessage);
    }


    public boolean isWarnEnabled() {
        return true;
    }

    public void warn(String msg) {
        warn(msg, null);
    }

    public void warn(String msg, Throwable throwable) {
        String exceptionMessage = toString(throwable);
        String formatMessage = format("WARN ", msg, exceptionMessage);
        this.err.println(formatMessage);
    }

    private String toString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        throwable.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

    private String defaultString(String exceptionMessage, String defaultValue) {
        if (exceptionMessage == null) {
            return defaultValue;
        }
        return exceptionMessage;
    }
}
