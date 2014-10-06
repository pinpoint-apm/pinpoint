package com.nhn.pinpoint.profiler.util.bytecode;

import java.lang.reflect.Modifier;
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
    
    private final ClassLoader loader;

    
    public BytecodeClass(int version, int access, String name, String signature, String superName, String[] interfaces, List<BytecodeAnnotation> annotations, List<BytecodeMethod> methods, ClassLoader loader) {
        this.version = version;
        this.access = access;
        this.name = name;
        this.signature = signature;
        this.superName = superName;
        this.interfaces = interfaces;
        this.annotations = annotations;
        this.methods = methods;
        this.loader = loader;
    }

    public String getName() {
        return name;
    }
    
    public String getSuperName() {
        return superName;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public boolean isInterface() {
        return (ACC_INTERFACE & access) != 0;
    }

    public boolean isAbstract() {
        return (ACC_ABSTRACT & access) != 0;
    }

    public List<BytecodeAnnotation> getAnnotations() {
        return annotations;
    }

    public boolean isAnnotationPresent(String annotationClassName) {
        return isAnnotationPresent(annotationClassName, false, false);
    }
    
    public boolean isAnnotationPresent(String annotationClassName, boolean checkSuperClass, boolean checkAnnotationHierarchy) {
        String annotationInternalName = BytecodeUtils.toInternalName(annotationClassName);
        return isAnnotationPresent0(annotationInternalName, checkSuperClass, checkAnnotationHierarchy);
    }

    private boolean isAnnotationPresent0(String annotationInternalName, boolean checkSuperClass, boolean checkAnnotationHierarchy) {
        if (annotations != null) {
            for (BytecodeAnnotation annotation : annotations) {
                if (annotation.getTypeInternalName().equals(annotationInternalName)) {
                    return true;
                }
            }
            
            if (checkAnnotationHierarchy) {
                for (BytecodeAnnotation annotation : annotations) {
                    BytecodeClass c = BytecodeClassFactory.get(annotation.getTypeInternalName(), loader);
                    
                    if (c.checkAnnotationHierarchy(annotationInternalName)) {
                        return true;
                    }
                }            
            }
        }
        
        if (checkSuperClass && !superName.equals("java/lang/Object")) {
            BytecodeClass c = BytecodeClassFactory.get(superName, loader);
            
            if (c.isAnnotationPresent0(annotationInternalName, checkSuperClass, checkAnnotationHierarchy)) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean checkAnnotationHierarchy(String annotationInternalName) {
        for (BytecodeAnnotation a : annotations) {
            if (a.getTypeInternalName().equals(annotationInternalName)) {
                return true;
            }
        }
        
        for (BytecodeAnnotation a : annotations) {
            if (a.getTypeInternalName().startsWith("java/lang/annotation")) {
                continue;
            }
            
            BytecodeClass ac = BytecodeClassFactory.get(a.getTypeInternalName(), loader);
            
            if (ac.checkAnnotationHierarchy(annotationInternalName)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isInstanceOf(String className, boolean isInterface) {
        String internalName = BytecodeUtils.toInternalName(className);
        return isInstanceOf0(internalName, isInterface);
    }
    
    public boolean isInstanceOf0(String internalName, boolean isInterface) {
        if (name.equals(internalName) || superName.equals(internalName)) {
            return true;
        }
        
        for (String impl : interfaces) {
            if (impl.equals(internalName)) {
                return true;
            }
        }
        
        if (!superName.equals("java/lang/Object")) {
            BytecodeClass c = BytecodeClassFactory.get(superName, loader);
            
            if (c.isInstanceOf0(internalName, isInterface)) {
                return true;
            }
        }

        if (isInterface) {
            for (String impl : interfaces) {
                BytecodeClass i = BytecodeClassFactory.get(impl, loader);
                
                if (i.isInstanceOf0(internalName, isInterface)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public List<BytecodeMethod> getDeclaredMethods() {
        return methods;
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
