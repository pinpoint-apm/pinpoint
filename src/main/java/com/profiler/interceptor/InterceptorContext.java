package com.profiler.interceptor;

import java.util.Arrays;

public class InterceptorContext {
    private Object target;
    private String className;
    private String methodName;
    private Object[] parameter;

    private Throwable exception;
    private Object returnValue;

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getParameter() {
        return parameter;
    }

    public void setParameter(Object[] parameter) {
        this.parameter = parameter;
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        return "InterceptorContext{" +
                "target=" + target +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", parameter=" + (parameter == null ? null : Arrays.asList(parameter)) +
                ", exception=" + exception +
                ", returnValue=" + returnValue +
                '}';
    }
}
