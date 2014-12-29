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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class ClassLoadChecker {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final Object EXIST = new Object();

    private final ConcurrentMap<LoadClass, Object> load = new ConcurrentHashMap<LoadClass, Object>();

    public boolean exist(ClassLoader cl, String className) {
        LoadClass key = new LoadClass(cl, className);
        Object old = load.putIfAbsent(key, EXIST);
        if (old == null) {
            if (isDebug) {
                logger.debug("{} not exist from ", cl);
            }
            return false;
        }
        if (isDebug) {
            logger.debug("{} already exist from ", cl);
        }
        return true;
    }

    private static class LoadClass {
        private final WeakReference<ClassLoader> classLoaderReference;
        private final int classLoaderHash;
        private final String className;


        private LoadClass(ClassLoader classLoader, String className) {
            if (className == null) {
                throw new NullPointerException("className must not be null");
            }
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            this.classLoaderReference = new WeakReference<ClassLoader>(classLoader);
            classLoaderHash = classLoader.hashCode();
            this.className = className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LoadClass loadClass = (LoadClass) o;
            ClassLoader thisClassLoader = classLoaderReference.get();
            ClassLoader thatClassLoader = loadClass.classLoaderReference.get();
            if (thisClassLoader == null || thatClassLoader == null) {
                return false;
            }

            if (!thisClassLoader.equals(thatClassLoader)) return false;
            if (!className.equals(loadClass.className)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = classLoaderHash;
            result = 31 * result + className.hashCode();
            return result;
        }
    }
}
