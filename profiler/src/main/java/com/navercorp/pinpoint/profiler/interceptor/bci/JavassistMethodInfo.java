package com.navercorp.pinpoint.profiler.interceptor.bci;

import javassist.CtBehavior;
import javassist.CtConstructor;

import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.profiler.interceptor.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

public class JavassistMethodInfo implements MethodInfo {
    private final CtBehavior behavior;
    
    public JavassistMethodInfo(CtBehavior behavior) {
        this.behavior = behavior;
    }

    @Override
    public String getName() {
        return behavior.getName();
    }

    @Override
    public String[] getParameterTypes() {
        return JavaAssistUtils.parseParameterSignature(behavior.getSignature());
    }

    @Override
    public int getModifiers() {
        return behavior.getModifiers();
    }
    
    @Override
    public boolean isConstructor() {
        return behavior instanceof CtConstructor;
    }

    @Override
    public MethodDescriptor getDescriptor() {
        String[] parameterVariableNames = JavaAssistUtils.getParameterVariableName(behavior);
        return new DefaultMethodDescriptor(behavior.getDeclaringClass().getName(), behavior.getName(), getParameterTypes(), parameterVariableNames);
    }

}
