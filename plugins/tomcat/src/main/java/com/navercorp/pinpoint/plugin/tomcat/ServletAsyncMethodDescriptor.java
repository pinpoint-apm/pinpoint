package com.navercorp.pinpoint.plugin.tomcat;

import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;

public class ServletAsyncMethodDescriptor implements MethodDescriptor {
    private static final String CLASS_NAME = "";
    private static final String METHOD_NAME = "Tomcat";
    private int apiId = 0;

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public String getClassName() {
        return CLASS_NAME;
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
        return ".Tomcat Servlet Asynchronous Process()";
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
        return ".Tomcat Servlet Asynchronous Process()";
    }
}