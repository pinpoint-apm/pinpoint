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

import org.apache.logging.log4j.Level;
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

    /**
     * Logs at most once per {@code interval}; suppressed calls are still counted.
     */
    public static ThrottledLogger getIntervalLogger(Logger logger, Duration interval) {
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

    // level -------------

    private boolean isEnabled(Level level) {
        return logger.isEnabled(level);
    }

    private void log(Level level, String msg) {
        if (!logger.isEnabled(level)) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.log(level, msg);
    }

    private void log(Level level, String msg, Object arg) {
        if (!logger.isEnabled(level)) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.log(level, msg, arg);
    }

    private void log(Level level, String msg, Object arg1, Object arg2) {
        if (!logger.isEnabled(level)) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.log(level, msg, arg1, arg2);
    }

    private void log(Level level, String msg, Throwable t) {
        if (!logger.isEnabled(level)) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.log(level, msg, t);
    }

    private void log(Level level, String msg, Object... args) {
        if (!logger.isEnabled(level)) {
            return;
        }
        if (!checkLogCounter()) {
            return;
        }
        logger.log(level, msg, args);
    }

    // info -------------

    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
    }

    public void info(String msg) {
        log(Level.INFO, msg);
    }

    public void info(String msg, Object arg) {
        log(Level.INFO, msg, arg);
    }

    public void info(String msg, Object arg1, Object arg2) {
        log(Level.INFO, msg, arg1, arg2);
    }

    public void info(String msg, Throwable t) {
        log(Level.INFO, msg, t);
    }

    public void info(String msg, Object... args) {
        log(Level.INFO, msg, args);
    }

    // debug -------------

    public boolean isDebugEnabled() {
        return isEnabled(Level.DEBUG);
    }

    public void debug(String msg) {
        log(Level.DEBUG, msg);
    }

    public void debug(String msg, Object arg) {
        log(Level.DEBUG, msg, arg);
    }

    public void debug(String msg, Object arg1, Object arg2) {
        log(Level.DEBUG, msg, arg1, arg2);
    }

    public void debug(String msg, Throwable t) {
        log(Level.DEBUG, msg, t);
    }

    public void debug(String msg, Object... args) {
        log(Level.DEBUG, msg, args);
    }

    // warn -------------

    public boolean isWarnEnabled() {
        return isEnabled(Level.WARN);
    }

    public void warn(String msg) {
        log(Level.WARN, msg);
    }

    public void warn(String msg, Object arg) {
        log(Level.WARN, msg, arg);
    }

    public void warn(String msg, Object arg1, Object arg2) {
        log(Level.WARN, msg, arg1, arg2);
    }

    public void warn(String msg, Throwable t) {
        log(Level.WARN, msg, t);
    }

    public void warn(String msg, Object... args) {
        log(Level.WARN, msg, args);
    }
}
