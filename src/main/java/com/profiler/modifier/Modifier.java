package com.profiler.modifier;


import javassist.ClassPool;

public interface Modifier {
    byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer);
}
