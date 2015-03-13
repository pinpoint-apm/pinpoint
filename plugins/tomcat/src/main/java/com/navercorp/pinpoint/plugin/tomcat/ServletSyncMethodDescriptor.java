package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;

public class ServletSyncMethodDescriptor implements MethodDescriptor {
    private static final String FULL_NAME = "Servlet Synchronous Process";
    private static final String DESCRIPTOR = "";
    private int apiId = 0;

    @Override
    public String getMethodName() {
        return "";
    }

    @Override
    public String getClassName() {
        return "";
    }

    @Override
    public String[] getParameterTypes() {
        return null;
    }

    @Override
    public String[] getParameterVariableName() {
        return null;
    }

    @Override
    public String getParameterDescriptor() {
        return "()";
    }

    @Override
    public int getLineNumber() {
        return 0;
    }

    @Override
    public String getFullName() {
        return FULL_NAME;
    }

    @Override
    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    @Override
    public int getApiId() {
        return apiId;
    }

    @Override
    public String getApiDescriptor() {
        return "";
    }
}
