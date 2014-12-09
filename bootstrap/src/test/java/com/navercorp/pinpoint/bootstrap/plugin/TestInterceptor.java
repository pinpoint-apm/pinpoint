package com.navercorp.pinpoint.bootstrap.plugin;

import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;

public class TestInterceptor implements SimpleAroundInterceptor {
    private final String field;
    
    public TestInterceptor(String field) {
        this.field = field;
    }

    @Override
    public void before(Object target, Object[] args) {
        // TODO Auto-generated method stub

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        // TODO Auto-generated method stub

    }

}
