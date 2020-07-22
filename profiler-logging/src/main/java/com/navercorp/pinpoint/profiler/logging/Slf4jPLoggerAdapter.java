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

import com.navercorp.pinpoint.bootstrap.plugin.jdbc.SqlModule;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Marker;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author emeroad
 */
public class Slf4jPLoggerAdapter extends AbstractLoggerAdapter implements PLogger  {

    private final org.slf4j.Logger logger;

    public Slf4jPLoggerAdapter(org.slf4j.Logger logger) {
        this.logger = Assert.requireNonNull(logger, "logger");
    }

    public String getName() {
        return logger.getName();
    }

    @Override
    public void beforeInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = logMethod(TYPE.BEFORE, target, className, methodName, parameterDescription, args);
            logger.debug(sb.toString());
        }
    }

    @Override
    public void beforeInterceptor(Object target, Object[] args) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = logMethod(TYPE.BEFORE, target, args);
            logger.debug(sb.toString());
        }

    }

    @Override
    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = logMethod(TYPE.AFTER, target, className, methodName, parameterDescription, args);
            logResult(sb, result, throwable);
            String log = sb.toString();
            if (throwable == null) {
                debug(log);
            } else {
                debug(log, throwable);
            }
        }
    }


    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = logMethod(TYPE.AFTER, target, args);
            logResult(sb, result, throwable);
            String log = sb.toString();
            if (throwable == null) {
                debug(log);
            } else {
                debug(log, throwable);
            }
        }
    }


    @Override
    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = logMethod(TYPE.AFTER, target, className, methodName, parameterDescription, args);
            logger.debug(sb.toString());
        }
    }

    @Override
    public void afterInterceptor(Object target, Object[] args) {
        if (logger.isDebugEnabled()) {
            StringBuilder sb = logMethod(TYPE.AFTER, target, args);
            logger.debug(sb.toString());
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object[] argArray) {
        logger.trace(format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
    }

    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    public void trace(Marker marker, String msg) {
        logger.trace(marker, msg);
    }

    public void trace(Marker marker, String format, Object arg) {
        logger.trace(marker, format, arg);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.trace(marker, format, arg1, arg2);
    }

    public void trace(Marker marker, String format, Object[] argArray) {
        logger.trace(marker, format, argArray);
    }

    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object[] argArray) {
        logger.debug(format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    public void debug(Marker marker, String msg) {
        logger.debug(marker, msg);
    }

    public void debug(Marker marker, String format, Object arg) {
        logger.debug(marker, format, arg);
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(marker, format, arg1, arg2);
    }

    public void debug(Marker marker, String format, Object[] argArray) {
        logger.debug(marker, format, argArray);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object[] argArray) {
        logger.info(format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    public void info(Marker marker, String msg) {
        logger.info(marker, msg);
    }

    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, format, arg);
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, format, arg1, arg2);
    }

    public void info(Marker marker, String format, Object[] argArray) {
        logger.info(marker, format, argArray);
    }

    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object[] argArray) {
        logger.warn(format, argArray);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    public void warn(Marker marker, String msg) {
        logger.warn(marker, msg);
    }

    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, format, arg);
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, format, arg1, arg2);
    }

    public void warn(Marker marker, String format, Object[] argArray) {
        logger.warn(marker, format, argArray);
    }

    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object[] argArray) {
        logger.error(format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    public void error(Marker marker, String msg) {
        logger.error(marker, msg);
    }

    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, format, arg);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, format, arg1, arg2);
    }

    public void error(Marker marker, String format, Object[] argArray) {
        logger.error(marker, format, argArray);
    }

    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, msg, t);
    }
}
