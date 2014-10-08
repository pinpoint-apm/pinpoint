package com.nhn.pinpoint.profiler.util.bytecode;

import java.util.Map;

public class BytecodeAnnotation {
    private final String descriptor;
    private final Map<String, Object> elements;
    
    public BytecodeAnnotation(String descriptor, Map<String, Object> elements) {
        this.descriptor = descriptor;
        this.elements = elements;
    }

    public String getDescriptor() {
        return descriptor;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getElement(String name) {
        if (elements == null) {
            return null;
        }
        
        return (T)elements.get(name);
    }
}
