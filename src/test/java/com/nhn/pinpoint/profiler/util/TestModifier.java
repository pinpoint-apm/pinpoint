package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public abstract class TestModifier extends AbstractModifier {

    private String targetClass;
    public Object interceptor;
    public Object interceptor2;

    public TestModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }


    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public String getTargetClass() {
        return targetClass;
    }

    public Object getInterceptor() {
        return interceptor;
    }

    public Object getInterceptor2() {
        return interceptor2;
    }

    @Override
    public abstract byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer);


}
