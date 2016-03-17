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

package com.navercorp.pinpoint.bootstrap.logging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * @author minwoo.jung
 */
public class JavaLoggerBinder {
    
    private final static Handler[] handlers = LogManager.getLogManager().getLogger("").getHandlers();

    private ConcurrentMap<String, Logger> loggerCache = new ConcurrentHashMap<String, Logger>(256, 0.75f, 128);

    public Logger getLogger(String name) {

        final Logger hitLogger = loggerCache.get(name);
        if (hitLogger != null) {
            return hitLogger;
        }

        Logger logger = Logger.getLogger(name);
        
        for (Handler handler : handlers) {
            logger.addHandler(handler);
            logger.setUseParentHandlers(false);
        }
        
        final Logger before = loggerCache.putIfAbsent(name, logger);
        if (before != null) {
            return before;
        }
        return logger;
    }

    public void shutdown() {
        loggerCache = null;
    }

}
