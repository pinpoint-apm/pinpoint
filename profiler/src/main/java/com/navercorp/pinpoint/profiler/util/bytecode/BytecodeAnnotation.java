package com.nhn.pinpoint.profiler.util.bytecode;

import java.util.Map;

public class BytecodeAnnotation {
    private final String descriptor;
    private final String name;
    private final Map<String, Object> elements;
    
    public BytecodeAnnotation(String descriptor, Map<String, Object> elements) {
        this.descriptor = descriptor;
        this.elements = elements;
        this.name = BytecodeUtils.descriptorToInternalName(descriptor);
    }

    public String getDescriptor() {
        return descriptor;
    }
    
    public String getTypeInternalName() {
        return name;
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getElement(String name) {
        if (elements == null) {
            return null;
        }
        
        return (T)elements.get(name);
    }
}
