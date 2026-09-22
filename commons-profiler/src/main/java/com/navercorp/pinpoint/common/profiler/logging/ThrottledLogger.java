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

package com.navercorp.pinpoint.common.profiler.logging;

import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ThrottledLogger {

    private final Logger logger;
    private final LogThrottle throttle;

    /**
     * Logs once per {@code ratio} calls.
     *
     * @deprecated call-count throttling emits in bursts under load spikes;
     * use time-based {@link #getIntervalLogger(Logger, Duration)} instead
     */
    @Deprecated
    public static ThrottledLogger getLogger(Logger logger, long ratio) {
        Objects.requireNonNull(logger, "logger");
        return new ThrottledLogger(logger, new CountLogThrottle(ratio));
    }

    public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(3);

    /**
     * Logs at most once per {@link #DEFAULT_INTERVAL}; suppressed calls are still counted.
     */
    public static ThrottledLogger getIntervalLogger(Logger logger) {
        return getIntervalLogger(logger, DEFAULT_INTERVAL);
    }

    /**
     * Logs at most once per {@code interval}; suppressed calls are still counted.
     */
    public static ThrottledLogger getIntervalLogger(Logger logger, Duration interval) {
        Objects.requireNonNull(logger, "logger");
        Objects.requireNonNull(interval, "interval");
        return new ThrottledLogger(logger, new CountingTimeLogThrottle(interval.toMillis()));
    }

    /**
     * Logs at most once per {@link #DEFAULT_INTERVAL} without counting suppressed calls;
     * {@link #getCounter()} returns {@link LogThrottle#DISABLED_COUNTER}.
     */
    public static ThrottledLogger getUncountedIntervalLogger(Logger logger) {
        return getUncountedIntervalLogger(logger, DEFAULT_INTERVAL);
    }

    /**
     * Logs at most once per {@code interval} without counting suppressed calls;
     * {@link #getCounter()} returns {@link LogThrottle#DISABLED_COUNTER}.
     */
    public static ThrottledLogger getUncountedIntervalLogger(Logger logger, Duration interval) {
        Objects.requireNonNull(logger, "logger");
        Objects.requireNonNull(interval, "interval");
        return new ThrottledLogger(logger, new TimeLogThrottle(interval.toMillis()));
    }

    private ThrottledLogger(Logger logger, LogThrottle throttle) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.throttle = Objects.requireNonNull(throttle, "throttle");
    }

    private boolean checkLogCounter() {
        return throttle.tryAcquire();
    }

    public long getCounter() {
        return throttle.getCounter();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public void info(String msg) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.info(msg);
    }

    public void info(String msg, Object arg) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.info(msg, arg);
    }

    public void info(String msg, Object arg1, Object arg2) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.info(msg, arg1, arg2);
    }

    public void info(String msg, Throwable t) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.info(msg, t);
    }

    public void info(String msg, Object... args) {
        if (!logger.isInfoEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.info(msg, args);
    }

    // debug -------------

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void debug(String msg) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.debug(msg);
    }

    public void debug(String msg, Object arg) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.debug(msg, arg);
    }

    public void debug(String msg, Object arg1, Object arg2) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.debug(msg, arg1, arg2);
    }

    public void debug(String msg, Throwable t) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.debug(msg, t);
    }

    public void debug(String msg, Object... args) {
        if (!logger.isDebugEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.debug(msg, args);
    }

    // warn -------------

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public void warn(String msg) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.warn(msg);
    }

    public void warn(String msg, Object arg) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.warn(msg, arg);
    }

    public void warn(String msg, Object arg1, Object arg2) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.warn(msg, arg1, arg2);
    }

    public void warn(String msg, Throwable t) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.warn(msg, t);
    }

    public void warn(String msg, Object... args) {
        if (!logger.isWarnEnabled()) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.warn(msg, args);
    }
}
