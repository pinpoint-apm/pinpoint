package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.common.trace.MethodType;

public class OpenwhiskInvokerMethodDescriptor implements MethodDescriptor {
    private int apiId = 0;
    private int type = MethodType.INVOCATION;

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
        return -1;
    }

    @Override
    public String getFullName() {
        return "Openwhisk Invoker";
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
        return "Openwhisk Invoker";
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
