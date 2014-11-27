package com.nhn.pinpoint.test;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public abstract class TestModifier extends AbstractModifier {

    private String targetClass;

    public final List<Interceptor> interceptorList = new ArrayList<Interceptor>();

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

    public void addInterceptor(Interceptor interceptor) {
        this.interceptorList.add(interceptor);
    }

    public List<Interceptor> getInterceptorList() {
        return interceptorList;
    }

    public Interceptor getInterceptor(int index) {
        return interceptorList.get(index);
    }




    @Override
    public abstract byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer);


}
