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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class ClassLoadChecker {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final Object EXIST = new Object();

    private final ConcurrentMap<ClassLoader, ConcurrentMap<String, Object>> classLoaderMap = Maps.newWeakConcurrentMap();


    public boolean exist(ClassLoader classLoader, String className) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader must not be null");
        }
        final ConcurrentMap<String, Object> classMap = findClassMap(classLoader);

        final Object hit = classMap.get(className);
        if (hit != null) {
            if (isDebug) {
                logger.debug("{} already exist from {}", className, classLoader);
            }
            return true;
        }

        final Object old = classMap.putIfAbsent(className, EXIST);
        if (old == null) {
            if (isDebug) {
                logger.debug("{} not exist from {}", className, classLoader);
            }
            return false;
        }
        if (isDebug) {
            logger.debug("{} already exist from {}", className, classLoader);
        }
        return true;
    }

    private ConcurrentMap<String, Object> findClassMap(ClassLoader classLoader) {
        ConcurrentMap<String, Object> hit = this.classLoaderMap.get(classLoader);
        if (hit != null) {
            return hit;
        }
        ConcurrentMap<String, Object> newClassMap = new ConcurrentHashMap<String, Object>();
        ConcurrentMap<String, Object> exist = this.classLoaderMap.putIfAbsent(classLoader, newClassMap);
        if (exist != null) {
            return exist;
        }
        return newClassMap;
    }

}
