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
import com.navercorp.pinpoint.bootstrap.logging.PLoggerBinder;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.Assert;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class Log4j2Binder implements PLoggerBinder {

    private final ConcurrentMap<String, PLogger> loggerCache = new ConcurrentHashMap<String, PLogger>(256, 0.75f, 128);
    private final LoggerContext loggerContext;

    public Log4j2Binder(LoggerContext loggerContext) {
        this.loggerContext = Assert.requireNonNull(loggerContext, "loggerContext");
    }

    @Override
    public PLogger getLogger(String name) {

        final PLogger hitPLogger = loggerCache.get(name);
        if (hitPLogger != null) {
            return hitPLogger;
        }
        Logger logger = loggerContext.getLogger(name);
        PLogger log4j2Adapter = new Log4j2PLoggerAdapter(logger);

        final PLogger before = loggerCache.putIfAbsent(name, log4j2Adapter);
        if (before != null) {
            return before;
        }
        return log4j2Adapter;
    }

    @Override
    public void shutdown() {

    }
}
