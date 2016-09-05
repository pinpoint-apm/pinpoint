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

import java.io.PrintStream;
import java.text.MessageFormat;

/**
 * @author Woonduk Kang(emeroad)
 */
public class StdoutCommonLogger implements CommonLogger {

//    private final String loggerName;
    private final String messagePattern;
    private final PrintStream out;
    private final PrintStream err;


    public StdoutCommonLogger(String loggerName) {
        this(loggerName, System.out, System.err);
    }

    // for test
    StdoutCommonLogger(String loggerName, PrintStream out, PrintStream err) {
        if (loggerName == null) {
            throw new NullPointerException("loggerName must not be null");
        }
        if (out == null) {
            throw new NullPointerException("out must not be null");
        }
        if (err == null) {
            throw new NullPointerException("err must not be null");
        }

//        this.loggerName = loggerName;
        this.messagePattern = "{0,date,yyyy-MM-dd HH:mm:ss SSS} [{1}](" + loggerName + ") {2}";
        this.out = out;
        this.err = err;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String msg) {

    }

    @Override
    public void info(String msg) {
        String message = format("INFO ", msg);
        this.out.println(message);
    }

    private String format(String logLevel, String msg) {

        MessageFormat messageFormat = new MessageFormat(messagePattern);
        
        final long date = System.currentTimeMillis();
        Object[] parameter = {date, logLevel, msg};
        return messageFormat.format(parameter);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(String msg) {
        String message = format("WARN ", msg);
        this.err.println(message);
    }

    @Override
    public void warn(String msg, Throwable th) {
        warn(msg);
        th.printStackTrace(this.err);
    }
}
