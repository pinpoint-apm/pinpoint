package com.nhn.pinpoint.profiler.util.bytecode;

import java.util.List;

public class BytecodeMethod {
    private final int access;
    private final String name;
    private final String descriptor;
    private final String signature;
    private final String[] exceptions;
    private final List<BytecodeAnnotation> annotations;
    
    public BytecodeMethod(int access, String name, String descriptor, String signature, String[] exceptions, List<BytecodeAnnotation> annotations) {
        this.access = access;
        this.name = name;
        this.descriptor = descriptor;
        this.signature = signature;
        this.exceptions = exceptions;
        this.annotations = annotations;
    }

    public int getAccess() {
        return access;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public String getSignature() {
        return signature;
    }

    public String[] getExceptions() {
        return exceptions;
    }

    public List<BytecodeAnnotation> getAnnotations() {
        return annotations;
    }
}
