package com.navercorp.pinpoint.bootstrap.instrument;

import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;

/**
 * @author emeroad
 */
public interface MethodInfo {
    public String getName();

    public String[] getParameterTypes();

    public int getModifiers();
    
    public boolean isConstructor();
    
    public MethodDescriptor getDescriptor();
}
