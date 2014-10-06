package com.nhn.pinpoint.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;

import com.nhn.pinpoint.profiler.modifier.Modifier;

public class ClassFileRetransformer implements ClassFileTransformer {
    private final Instrumentation instrumentation;
    private final ConcurrentHashMap<Class<?>, Modifier> targets = new ConcurrentHashMap<Class<?>, Modifier>();
    
    public ClassFileRetransformer(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        Modifier modifier = targets.remove(classBeingRedefined);
        
        if (modifier == null) {
            return null;
        }
        
        return modifier.modify(loader, className.replace('/', '.'), protectionDomain, classfileBuffer);
    }
    
    public void retransform(Class<?> target, Modifier modifier) {
        Modifier removed = targets.put(target, modifier);

        if (removed != null) {
            // TODO log
        }
        
        try {
            instrumentation.retransformClasses(target);
        } catch (UnmodifiableClassException e) {
            throw new ProfilerException(e);
        }
    }
}
