package com.navercorp.pinpoint.common.util;

public class DefaultApiDescription implements ApiDescription {
    private String className;

    private String methodName;

    private String[] simpleParameter;

    private int line = -1;

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public String getSimpleClassName() {
        int classNameStartIndex = className.lastIndexOf('.') + 1;
        return className.substring(classNameStartIndex, className.length());
    }

    public String getPackageNameName() {
        int packageNameIndex = className.lastIndexOf('.');
        if (packageNameIndex == -1) {
            return "";
        }
        return className.substring(0, packageNameIndex);
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public void setSimpleParameter(String[] simpleParameter) {
        this.simpleParameter = simpleParameter;
    }

    public String[] getSimpleParameter() {
        return simpleParameter;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public String getSimpleMethodDescription() {
        String simpleParameterDescription = concateLine(simpleParameter, ", ");
        return methodName + simpleParameterDescription;
    }

    public String concateLine(String[] stringList, String separator) {
        if (ArrayUtils.isEmpty(stringList)) {
            return "()";
        }

        StringBuilder sb = new StringBuilder();
        if (stringList.length > 0) {
            sb.append('(');
            sb.append(stringList[0]);
            for (int i = 1; i < stringList.length; i++) {
                sb.append(separator);
                sb.append(stringList[i]);
            }
            sb.append(')');
        }
        return sb.toString();
    }
}