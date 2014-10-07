package com.nhn.pinpoint.profiler.util;

import com.nhn.pinpoint.profiler.DefaultAgent;

/**
 * @author hyungil.jeong
 */
public class TestClassLoaderForClover extends TestClassLoader {
    
    private final String cloverRuntimePackage;

    public TestClassLoaderForClover(DefaultAgent agent, String cloverRuntimePackage) {
        super(agent);
        this.cloverRuntimePackage = cloverRuntimePackage;
    }

    @Override
    protected Class<?> loadClassByDelegation(String name) throws ClassNotFoundException {
        if (name.startsWith(this.cloverRuntimePackage)) {
            return super.delegateToParent(name);
        }
        return super.loadClassByDelegation(name);
    }
    
}
