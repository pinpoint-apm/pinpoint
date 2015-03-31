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

package com.navercorp.pinpoint.profiler.interceptor.bci;

import com.navercorp.pinpoint.profiler.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class ClassLoadChecker {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ConcurrentMap<ClassLoader, String> load = Maps.newWeakConcurrentMap();


    public boolean exist(ClassLoader classLoader, String className) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader must not be null");
        }

        Object old = load.putIfAbsent(classLoader, className);
        if (old == null) {
            if (isDebug) {
                logger.debug("{} not exist from ", classLoader);
            }
            return false;
        }
        if (isDebug) {
            logger.debug("{} already exist from ", classLoader);
        }
        return true;
    }

}
