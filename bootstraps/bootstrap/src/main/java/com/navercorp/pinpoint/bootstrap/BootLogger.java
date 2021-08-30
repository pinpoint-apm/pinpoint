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
import java.io.Writer;
import java.util.Formatter;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 * @author yjqg6666
 */
public final class BootLogger {

    private static final String FORMAT = "%tm-%<td %<tT.%<tL %-5s %-35.35s : %s";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private static final BootLogLevel LOG_LEVEL;
    private final String loggerName;
    private final PrintStream out;
    private final PrintStream err;

    static {
        String logLevel = System.getProperty("pinpoint.agent.bootlogger.loglevel");
        final BootLogLevel level = BootLogLevel.of(logLevel);
        LOG_LEVEL = level != null ? level : BootLogLevel.INFO;
    }

    public BootLogger(String loggerName) {
        this(loggerName, System.out, System.err);
    }

    // for test
    BootLogger(String loggerName, PrintStream out, PrintStream err) {
        this.loggerName = Objects.requireNonNull(loggerName, "loggerName");
        this.out = out;
        this.err = err;
    }

    @SuppressWarnings("rawtypes")
    public static BootLogger getLogger(Class clazz) {
        return new BootLogger(clazz.getSimpleName());
    }

    public static BootLogger getLogger(String loggerName) {
        return new BootLogger(loggerName);
    }

    private String format(String logLevel, String msg, Throwable throwable) {
        final long now = System.currentTimeMillis();

        StringBuilder buffer = new StringBuilder(64);
        Formatter formatter = new Formatter(buffer);
        formatter.format(FORMAT, now, logLevel, loggerName, msg);
        if (throwable != null) {
            String exceptionMessage = toString(throwable);
            buffer.append(exceptionMessage);
        } else {
            buffer.append(LINE_SEPARATOR);
        }
        return formatter.toString();
    }

    public boolean isDebugEnabled() {
        return LOG_LEVEL.logDebug();
    }

    public void debug(String msg) {
        if (isDebugEnabled()) {
            String formatMessage = format("DEBUG", msg, null);
            this.out.print(formatMessage);
        }
    }

    public boolean isInfoEnabled() {
        return LOG_LEVEL.logInfo();
    }

    public void info(String msg) {
        if (isInfoEnabled()) {
            String formatMessage = format("INFO", msg, null);
            this.out.print(formatMessage);
        }
    }

    public boolean isWarnEnabled() {
        return LOG_LEVEL.logWarn();
    }

    public void warn(String msg) {
        warn(msg, null);
    }

    public void warn(String msg, Throwable throwable) {
        if (isWarnEnabled()) {
            String formatMessage = format("WARN", msg, throwable);
            this.err.print(formatMessage);
        }
    }

    public void error(String msg) {
        if (LOG_LEVEL.logError()) {
            String formatMessage = format("ERROR", msg, null);
            this.err.print(formatMessage);
        }
    }

    private static String toString(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        Writer sw = new StringWriter(512);
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        throwable.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

}
