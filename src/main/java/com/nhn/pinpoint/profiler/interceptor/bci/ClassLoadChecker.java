package com.nhn.pinpoint.profiler.interceptor.bci;

import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClassLoadChecker {
    private final Logger logger = LoggerFactory.getLogger(ClassLoadChecker.class.getName());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final Object EXIST = new Object();

    private ConcurrentMap<LoadClass, Object> load = new ConcurrentHashMap<LoadClass, Object>();

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

    static class LoadClass {
        private ClassLoader classLoader;
        private String className;

        LoadClass(ClassLoader classLoader, String className) {
            this.classLoader = classLoader;
            this.className = className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LoadClass loadClass = (LoadClass) o;

            if (classLoader != null ? !classLoader.equals(loadClass.classLoader) : loadClass.classLoader != null) return false;
            if (className != null ? !className.equals(loadClass.className) : loadClass.className != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = classLoader != null ? classLoader.hashCode() : 0;
            result = 31 * result + (className != null ? className.hashCode() : 0);
            return result;
        }
    }
}
