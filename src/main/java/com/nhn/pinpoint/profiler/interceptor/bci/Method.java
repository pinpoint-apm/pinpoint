package com.nhn.pinpoint.profiler.interceptor.bci;

import java.util.List;

/**
 *
 */
public class Method {
    private String methodName;
    private String[] methodParams;

    public Method() {
    }

    public Method(String methodName, String[] methodParams) {
        this.methodName = methodName;
        this.methodParams = methodParams;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(String[] methodParams) {
        this.methodParams = methodParams;
    }
}
