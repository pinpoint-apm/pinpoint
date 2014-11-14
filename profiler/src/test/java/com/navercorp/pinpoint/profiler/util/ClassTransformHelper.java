package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.bootstrap.plugin.BytecodeUtils;
import com.nhn.pinpoint.profiler.modifier.Modifier;

public class ClassTransformHelper {


    public static Class<?> transformClass(ClassLoader classLoader, String className, Modifier modifier) {
        final byte[] original = BytecodeUtils.getClassFile(classLoader, className);
        final byte[] transformed = modifier.modify(classLoader, className, null, original);
        
        return BytecodeUtils.defineClass(classLoader, className, transformed);
    }

}
