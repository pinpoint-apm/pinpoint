package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.bootstrap.plugin.BytecodeUtils;
import com.navercorp.pinpoint.profiler.modifier.Modifier;

public class ClassTransformHelper {


    public static Class<?> transformClass(ClassLoader classLoader, String className, Modifier modifier) {
        final byte[] original = BytecodeUtils.getClassFile(classLoader, className);
        final byte[] transformed = modifier.modify(classLoader, className, null, original);
        
        return BytecodeUtils.defineClass(classLoader, className, transformed);
    }

}
