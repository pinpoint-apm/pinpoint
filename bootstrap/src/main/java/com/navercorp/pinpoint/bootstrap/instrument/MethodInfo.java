package com.navercorp.pinpoint.bootstrap.instrument;

import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;

/**
 * @author emeroad
 */
public interface MethodInfo {
    String getName();

    String[] getParameterTypes();

    int getModifiers();
    
    boolean isConstructor();
    
    MethodDescriptor getDescriptor();
}
