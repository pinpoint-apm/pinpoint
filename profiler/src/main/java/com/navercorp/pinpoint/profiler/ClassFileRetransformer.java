package com.nhn.pinpoint.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.modifier.GeneralModifier;
import com.nhn.pinpoint.profiler.util.bytecode.BytecodeAnalyzer;
import com.nhn.pinpoint.profiler.util.bytecode.BytecodeClass;

public class ClassFileRetransformer implements ClassFileTransformer {
    private final ConcurrentHashMap<Target, Boolean> targets = new ConcurrentHashMap<Target, Boolean>();
    
    private final Agent agent;
    private final ByteCodeInstrumentor byteCodeInstrumentor;
    
    private List<GeneralModifier> modifiers;

    
    public ClassFileRetransformer(Agent agent, ByteCodeInstrumentor byteCodeInstrumentor) {
        if (agent == null) {
            throw new NullPointerException("agent must not be null");
        }
        
        if (byteCodeInstrumentor == null) {
            throw new NullPointerException("byteCodeInstrumentor must not be null");
        }
        
        this.agent = agent;
        this.byteCodeInstrumentor = byteCodeInstrumentor;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (targets.remove(new Target(loader, className)) == null) {
            return null;
        }
        
        BytecodeClass target = BytecodeAnalyzer.analyze(classfileBuffer);
        
        byte[] transformed = classfileBuffer;

        for (GeneralModifier modifier : modifiers) {
            if (modifier.canModify(target)) {
                transformed = modifier.modify(loader, protectionDomain, target, transformed);
            }
        }
        
        return transformed;
    }
    
    public void addTarget(ClassLoader loader, String className) {
        targets.put(new Target(loader, className), Boolean.TRUE);
    }
    
    private final static class Target {
        private final ClassLoader loader;
        private final String className;
        
        public Target(ClassLoader loader, String className) {
            this.loader = loader;
            this.className = className;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((className == null) ? 0 : className.hashCode());
            result = prime * result + ((loader == null) ? 0 : loader.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            
            if (obj == null) {
                return false;
            }
            
            Target other = (Target) obj;
            return (this.loader == other.loader) && (this.className.equals(other.className));
        }
    }
}
