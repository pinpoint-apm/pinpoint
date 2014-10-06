package com.nhn.pinpoint.profiler.util.bytecode;

import java.util.List;

import org.objectweb.asm.Opcodes;

public class BytecodeMethod implements Opcodes {
    private static final String CONSTURCTOR_NAME = "<init>";
    private static final int NON_TRANSFORMABLE_MODIFIER = ACC_ABSTRACT | ACC_NATIVE;
    
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
    
    public boolean isTransformable() {
        return (NON_TRANSFORMABLE_MODIFIER & access) == 0;
    }
    
    public boolean isStatic() {
        return (ACC_STATIC & access) != 0;
    }
    
    public boolean isAbstract() {
        return (ACC_ABSTRACT & access) != 0;
    }
    
    public boolean isPublic() {
        return (ACC_PUBLIC & access) != 0;
    }
    
    public boolean isSynthetic() {
        return (ACC_SYNTHETIC & access) != 0;
    }
    
    public boolean isNative() {
        return (ACC_NATIVE & access) != 0;
    }
    
    public boolean isConstructor() {
        return name.equals(CONSTURCTOR_NAME);
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
