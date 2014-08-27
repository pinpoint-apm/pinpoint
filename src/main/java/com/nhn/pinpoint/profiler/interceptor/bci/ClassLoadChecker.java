package com.nhn.pinpoint.profiler.interceptor.bci;

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
        private final ClassLoader classLoader;
        private final String className;

        private LoadClass(ClassLoader classLoader, String className) {
            if (className == null) {
                throw new NullPointerException("className must not be null");
            }
            // classLoader는 null일수도 있을거 같음. 몇몇 java reference의 최상위 로더의 경우 null 이 나옴.
            this.classLoader = classLoader;
            this.className = className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LoadClass loadClass = (LoadClass) o;

            if (classLoader != null ? !classLoader.equals(loadClass.classLoader) : loadClass.classLoader != null) return false;
            if (!className.equals(loadClass.className)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = classLoader != null ? classLoader.hashCode() : 0;
            result = 31 * result + className.hashCode();
            return result;
        }
    }
}
