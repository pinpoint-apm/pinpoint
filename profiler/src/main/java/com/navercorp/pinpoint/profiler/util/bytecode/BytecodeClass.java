package com.nhn.pinpoint.profiler.util.bytecode;

import java.util.List;

import org.objectweb.asm.Opcodes;

public class BytecodeClass implements Opcodes {
    private final int version;
    private final int access;
    private final String name;
    private final String signature;
    private final String superName;
    private final String[] interfaces;
    private final List<BytecodeAnnotation> annotations;
    private final List<BytecodeMethod> methods;

    public BytecodeClass(int version, int access, String name, String signature, String superName, String[] interfaces, List<BytecodeAnnotation> annotations, List<BytecodeMethod> methods) {
        this.version = version;
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
        this.annotations = annotations;
        this.methods = methods;
    }

    public String getName() {
        return name;
    }

    private boolean isSet(int flag) {
        return (access & flag) != 0;
    }

    public boolean isInterface() {
        return isSet(ACC_INTERFACE);
    }

    public boolean isAbstract() {
        return isSet(ACC_ABSTRACT);
    }

    public boolean isAnnotation() {
        return isSet(ACC_ANNOTATION);
    }

    public boolean isAnnotationPresent(String descriptor) {
        if (annotations == null) {
            return false;
        }

        for (BytecodeAnnotation annotation : annotations) {
            if (annotation.getDescriptor().equals(descriptor)) {
                return true;
            }
        }

        return false;
    }

    public BytecodeMethod getDeclaredMethod(String name, String descriptor) {
        for (BytecodeMethod method : methods) {
            if (method.getName().equals(name) && method.getDescriptor().equals(descriptor)) {
                return method;
            }
        }

        return null;
    }
}
