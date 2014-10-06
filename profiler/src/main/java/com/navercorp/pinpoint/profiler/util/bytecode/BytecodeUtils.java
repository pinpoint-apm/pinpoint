package com.nhn.pinpoint.profiler.util.bytecode;

import org.objectweb.asm.Type;

public class BytecodeUtils {
    public static String toInternalName(String className) {
        return className.replace('.', '/');
    }
    
    public static String toClassName(String internalName) {
        return internalName.replace('/', '.');
    }
    
    public static String descriptorToInternalName(String descriptor) {
        return descriptor.substring(1, descriptor.length() - 1);
    }
}
