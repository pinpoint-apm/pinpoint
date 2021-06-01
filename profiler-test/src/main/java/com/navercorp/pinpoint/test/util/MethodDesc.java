package com.navercorp.pinpoint.test.util;

public class MethodDesc {
    private final String methodDesc;
    private final int lineNumber;

    public MethodDesc(String methodDesc, int lineNumber) {
        this.methodDesc = methodDesc;
        this.lineNumber = lineNumber;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodDesc that = (MethodDesc) o;

        if (lineNumber != that.lineNumber) return false;
        return methodDesc != null ? methodDesc.equals(that.methodDesc) : that.methodDesc == null;
    }

    @Override
    public int hashCode() {
        int result = methodDesc != null ? methodDesc.hashCode() : 0;
        result = 31 * result + lineNumber;
        return result;
    }
}
