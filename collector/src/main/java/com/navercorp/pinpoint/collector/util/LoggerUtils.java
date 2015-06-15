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

package com.navercorp.pinpoint.collector.util;

import org.slf4j.Logger;
import org.slf4j.spi.LocationAwareLogger;

/**
 * @author emeroad
 */
public final class LoggerUtils {
    // level : 00
    public static final int TRACE_LEVEL = LocationAwareLogger.TRACE_INT;
    // level : 10
    public static final int DEBUG_LEVEL  = LocationAwareLogger.DEBUG_INT;
    // level : 20
    public static final int INFO_LEVEL  = LocationAwareLogger.INFO_INT;
    // level : 30
    public static final int WARN_LEVEL  = LocationAwareLogger.WARN_INT;
    // level : 40
    public static final int ERROR_LEVEL  = LocationAwareLogger.ERROR_INT;
    // level : 50
    public static final int DISABLE_LEVEL  = LocationAwareLogger.ERROR_INT + 10;

    private LoggerUtils() {
    }

    public static int getLoggerLevel(Logger logger) {
        if (logger == null) {
            throw new NullPointerException("logger must not be null");
        }
        if (logger.isTraceEnabled()) {
            return TRACE_LEVEL;
        }
        if (logger.isDebugEnabled()) {
            return DEBUG_LEVEL;
        }
        if (logger.isInfoEnabled()) {
            return INFO_LEVEL;
        }
        if (logger.isWarnEnabled()) {
            return WARN_LEVEL;
        }
        if (logger.isErrorEnabled()) {
            return ERROR_LEVEL;
        }
        return DISABLE_LEVEL;
    }
}
