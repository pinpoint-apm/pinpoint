/*
 * Copyright 2014 NAVER Corp.
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
 */

package com.navercorp.pinpoint.profiler.logging;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

import java.util.Objects;

/**
 * @author emeroad
 */
@SuppressWarnings("deprecation")
public class Log4J2PluginLoggerAdapter extends AbstractLoggerAdapter implements PLogger {

    private final Logger logger;
    private final boolean isDebug;
    private final Marker marker;

    public Log4J2PluginLoggerAdapter(Logger logger, Marker marker) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.marker = Objects.requireNonNull(marker, "marker");
        this.isDebug = logger.isDebugEnabled(marker);
    }

    public String getName() {
        return logger.getName();
    }

    @Override
    public void beforeInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            StringBuilder sb = logMethod(TYPE.BEFORE, target, className, methodName, parameterDescription, args);
            String log = sb.toString();
            logger.debug(marker, log);
        }
    }

    @Override
    public void beforeInterceptor(Object target, Object[] args) {
        if (isDebug) {
            StringBuilder sb = logMethod(TYPE.BEFORE, target, args);
            String log = sb.toString();
            logger.debug(marker, log);
        }
    }

    @Override
    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            StringBuilder sb = logMethod(TYPE.AFTER, target, className, methodName, parameterDescription, args);
            logResult(sb, result, throwable);
            String log = sb.toString();
            if (throwable == null) {
                logger.debug(marker, log);
            } else {
                logger.debug(marker, log, throwable);
            }
        }
    }


    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            StringBuilder sb = logMethod(TYPE.AFTER, target, args);
            logResult(sb, result, throwable);
            String log = sb.toString();
            if (throwable == null) {
                logger.debug(marker, log);
            } else {
                logger.debug(marker, log, throwable);
            }
        }
    }


    @Override
    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (isDebug) {
            StringBuilder sb = logMethod(TYPE.AFTER, target, className, methodName, parameterDescription, args);
            String log = sb.toString();
            logger.debug(marker, log);
        }
    }

    @Override
    public void afterInterceptor(Object target, Object[] args) {
        if (isDebug) {
            StringBuilder sb = logMethod(TYPE.AFTER, target, args);
            String log = sb.toString();
            logger.debug(marker, log);
        }
    }


    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled(marker);
    }

    @Override
    public void trace(String msg) {
        logger.trace(marker, msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.trace(marker, format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object[] argArray) {
        logger.trace(marker, format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled(marker);
    }

    @Override
    public void debug(String msg) {
        logger.debug(marker, msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(marker, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object[] argArray) {
        logger.debug(marker, format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled(marker);
    }

    @Override
    public void info(String msg) {
        logger.info(marker, msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(marker, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object[] argArray) {
        logger.info(marker, format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled(marker);
    }

    @Override
    public void warn(String msg) {
        logger.warn(marker, msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(marker, format, arg);
    }

    @Override
    public void warn(String format, Object[] argArray) {
        logger.warn(marker, format, argArray);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled(marker);
    }

    @Override
    public void error(String msg) {
        logger.error(marker, msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(marker, format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object[] argArray) {
        logger.error(marker, format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(marker, msg, t);
    }

}
