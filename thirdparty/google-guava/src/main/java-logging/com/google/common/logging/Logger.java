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


import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;

/**
 * compiler trick class
 *
 * @author Woonduk Kang(emeroad)
 */
public class Logger {

    private static Level DEFAULT_LOG_LEVEL;

    private final String name;
    private final String messagePattern;
    private final Level level;

    private final PrintStream out;
    private final PrintStream err;

    static {
        setup();

    }

    private static void setup() {
        // TODO setup log level
        DEFAULT_LOG_LEVEL = Level.INFO;
    }

    public static Logger getLogger(String name) {
        return new Logger(name, DEFAULT_LOG_LEVEL);
    }

    public Logger(String name, Level level) {
        this(name, level, System.out, System.err);
    }

    Logger(String name, Level level, PrintStream out, PrintStream err) {
        this.name = name;
        this.level = level;
        this.messagePattern = "{0,date,yyyy-MM-dd HH:mm:ss} [{1}](" + name + ") {2}{3}";
        this.out = out;
        this.err = err;
    }

    public String getName() {
        return name;
    }

    public void log(Level level, String msg) {
        if (!isLoggable(level)) {
            return;
        }
        doLog(level, msg, null, null);
    }

    public void log(Level level, String msg, Throwable throwable) {
        if (!isLoggable(level)) {
            return;
        }
        doLog(level, msg, null, throwable);
    }

    public void log(Level level, String msg, Object params[]) {
        if (!isLoggable(level)) {
            return;
        }
        doLog(level, msg, params, null);
    }



    public void log(Level level, String msg, Object param1) {
        if (!isLoggable(level)) {
            return;
        }
        doLog(level, msg, new Object[]{param1}, null);
    }

    private void doLog(Level level, String msg, Object[] params, Throwable throwable) {

        String format = messageFormat(msg, params);

        String exceptionMessage = toString(throwable);
        String logMessage = logFormat(level, format, exceptionMessage);

        PrintStream printStream = getPrintStream(level);
        printStream.println(logMessage);

    }

    private PrintStream getPrintStream(Level level) {
        if (level.intValue() >= Level.WARNING.intValue()) {
            return this.err;
        } else {
            return this.out;
        }
    }

    public boolean isLoggable(Level level) {
        if (level.intValue() < this.level.intValue()) {
            return false;
        }
        return true;
    }

    public void info(String msg) {
        Level logLevel = Level.INFO;
        if (!isLoggable(logLevel)) {
            return;
        }
        doLog(Level.INFO, msg, null, null);
    }

    public void warning(String msg) {
        if (!isLoggable(Level.WARNING)) {
            return;
        }
        doLog(Level.WARNING, msg, null, null);
    }

    private String messageFormat(String format, Object[] parameter) {
        if (parameter == null || parameter.length == 0) {
            return format;
        }
        // emulation java util.logging
        if (format.indexOf("{0") >= 0 || format.indexOf("{1") >= 0 || format.indexOf("{2") >= 0 || format.indexOf("{3") >= 0) {
            return MessageFormat.format(format, parameter);
        }

        return format;
    }
    private String logFormat(Level logLevel, String msg, String exceptionMessage) {
        exceptionMessage = defaultString(exceptionMessage, "");

        final long date = System.currentTimeMillis();
        return MessageFormat.format(messagePattern, date, logLevel.getName(), msg, exceptionMessage);
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
