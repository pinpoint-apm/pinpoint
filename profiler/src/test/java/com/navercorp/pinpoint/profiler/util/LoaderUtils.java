package com.nhn.pinpoint.profiler.util;

import javassist.ClassPool;
import javassist.Loader;

/**
 * @author emeroad
 */
public final class LoaderUtils {

    public static Loader createLoader(ClassPool classPool) {
        if (classPool == null) {
            throw new NullPointerException("classPool must not be null");
        }
        final Loader loader = new Loader(classPool);
        loader.delegateLoadingOf("org.apache.log4j.");
        return loader;
    }

}
