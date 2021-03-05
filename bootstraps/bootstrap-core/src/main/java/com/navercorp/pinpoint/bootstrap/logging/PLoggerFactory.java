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

/**
 * @author emeroad
 */
public final class PLoggerFactory {

    private static PLoggerBinder loggerBinder;

    public static void initialize(PLoggerBinder loggerBinder) {
        if (PLoggerFactory.loggerBinder == null) {
            PLoggerFactory.loggerBinder = loggerBinder;
        } else {
            final CommonLogger logger = StdoutCommonLoggerFactory.INSTANCE.getLogger(PLoggerFactory.class.getName());
            logger.warn("loggerBinder is not null");
        }
    }

    public static void unregister(PLoggerBinder loggerBinder) {
        // Limited to remove only the ones already registered
        // when writing a test case, logger register/unregister logic must be located in beforeClass and afterClass
        if (loggerBinder == PLoggerFactory.loggerBinder) {
            PLoggerFactory.loggerBinder = null;
        }
    }

    public static PLogger getLogger(String name) {
        if (loggerBinder == null) {
            // this prevents null exception: need to return Dummy until a Binder is assigned
            return DummyPLogger.INSTANCE;
        }
        return loggerBinder.getLogger(name);
    }

    public static PLogger getLogger(Class clazz) {
        if (clazz == null) {
            throw new NullPointerException("class");
        }
        return getLogger(clazz.getName());
    }
}
