/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.logging.jul;

import org.apache.logging.log4j.LogManager;

public class Logger {
    private org.apache.logging.log4j.Logger logger;

    public static Logger getAnonymousLogger() {
        return new Logger(LogManager.getRootLogger());
    }

    public static Logger getLogger(String name) {
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(name);
        return new Logger(logger);
    }

    Logger(org.apache.logging.log4j.Logger logger) {
        this.logger = logger;
    }

    public void log(Level level, String msg) {
        this.logger.log(JulLogLevel.toLevel(level.intValue()), msg);
    }

    public void log(Level level, String msg, Object param1) {
        this.logger.log(JulLogLevel.toLevel(level.intValue()), msg, param1);
    }

    public void log(Level level, String msg, Object params[]) {
        this.logger.log(JulLogLevel.toLevel(level.intValue()), msg, params);
    }

    public void log(Level level, String msg, Throwable thrown) {
        this.logger.log(JulLogLevel.toLevel(level.intValue()), msg, thrown);
    }

    public void severe(String msg) {
        this.logger.fatal(msg);
    }

    public void warning(String msg) {
        this.logger.warn(msg);
    }

    public void info(String msg) {
        this.logger.info(msg);
    }

    public void config(String msg) {
        this.logger.debug(msg);
    }

    public void fine(String msg) {
        this.logger.debug(msg);
    }

    public void finer(String msg) {
        this.logger.trace(msg);
    }

    public void finest(String msg) {
        this.logger.trace(msg);
    }

    public boolean isLoggable(Level level) {
        return this.logger.isEnabled(JulLogLevel.toLevel(level.intValue()));
    }
}
