package com.profiler.interceptor;

/**
 *
 */
public class DefaultMethodDescriptor implements MethodDescriptor {
    private String className;
    private String simpleClassName;

    private String methodName;

    private String[] parameterTypes;
    private String[] simpleParameterTypes;

    private String[] parameterVariableName;


    private String parameterDescriptor;


    private String simpleParameterDescriptor;

    private int lineNumber;


    public DefaultMethodDescriptor() {
    }

    public String getParameterDescriptor() {
        return parameterDescriptor;
    }

    public void setParameterDescriptor(String parameterDescriptor) {
        this.parameterDescriptor = parameterDescriptor;
    }

    public String getSimpleParameterDescriptor() {
        return simpleParameterDescriptor;
    }

    public void setSimpleParameterDescriptor(String simpleParameterDescriptor) {
        this.simpleParameterDescriptor = simpleParameterDescriptor;
    }


    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setParameterVariableName(String[] parameterVariableName) {
        this.parameterVariableName = parameterVariableName;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getSimpleClassName() {
        return simpleClassName;
    }

    public void setSimpleClassName(String simpleClassName) {
        this.simpleClassName = simpleClassName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String[] getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public String[] getParameterVariableName() {
        return parameterVariableName;
    }

    public String[] getSimpleParameterTypes() {
        return simpleParameterTypes;
    }

    public void setSimpleParameterTypes(String[] simpleParameterTypes) {
        this.simpleParameterTypes = simpleParameterTypes;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
