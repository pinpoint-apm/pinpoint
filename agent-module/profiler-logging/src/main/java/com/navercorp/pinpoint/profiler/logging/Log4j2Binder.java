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

import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.logging.PluginLoggerBinder;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class Log4j2Binder implements PluginLoggerBinder {

    private final ConcurrentMap<String, PluginLogger> loggerCache = new ConcurrentHashMap<>(256, 0.75f, 128);
    private final LoggerContext loggerContext;
    private final Marker marker = MarkerManager.getMarker("PLUGIN");

    public Log4j2Binder(LoggerContext loggerContext) {
        this.loggerContext = Objects.requireNonNull(loggerContext, "loggerContext");
    }

    @Override
    public PluginLogger getLogger(String name) {

        final PluginLogger hitPluginLogger = loggerCache.get(name);
        if (hitPluginLogger != null) {
            return hitPluginLogger;
        }
        ExtendedLogger logger = loggerContext.getLogger(name);
        PluginLogger log4j2Adapter = new Log4J2PluginLoggerAdapter(logger, marker);

        final PluginLogger before = loggerCache.putIfAbsent(name, log4j2Adapter);
        if (before != null) {
            return before;
        }
        return log4j2Adapter;
    }

    @Override
    public void shutdown() {

    }
}
