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


import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;

import java.util.Objects;

/**
 * @author emeroad
 */
public class PluginLogManager {

    private static PluginLoggerBinder loggerBinder;

    public static void initialize(PluginLoggerBinder loggerBinder) {
        if (PluginLogManager.loggerBinder == null) {
            PluginLogManager.loggerBinder = loggerBinder;
        } else {
            final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(PluginLogManager.class.getName());
            logger.warn("loggerBinder is not null");
        }
    }

    public static void unregister(PluginLoggerBinder loggerBinder) {
        // Limited to remove only the ones already registered
        // when writing a test case, logger register/unregister logic must be located in beforeClass and afterClass
        if (loggerBinder == PluginLogManager.loggerBinder) {
            PluginLogManager.loggerBinder = null;
        }
    }

    public static PluginLogger getLogger(String name) {
        if (loggerBinder == null) {
            // this prevents null exception: need to return Dummy until a Binder is assigned
            return DummyPluginLogger.INSTANCE;
        }
        return loggerBinder.getLogger(name);
    }

    public static PluginLogger getLogger(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz");

        return getLogger(clazz.getName());
    }
}
