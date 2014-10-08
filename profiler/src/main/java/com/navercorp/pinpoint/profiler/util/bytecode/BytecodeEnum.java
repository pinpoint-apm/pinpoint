package com.nhn.pinpoint.profiler.util.bytecode;

public class BytecodeEnum {
    private final String descriptor;
    private final String value;
    
    public BytecodeEnum(String descriptor, String value) {
        this.descriptor = descriptor;
        this.value = value;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getValue() {
        return value;
    }
}
