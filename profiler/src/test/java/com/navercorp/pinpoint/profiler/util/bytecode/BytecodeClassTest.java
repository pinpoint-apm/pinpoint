package com.nhn.pinpoint.profiler.util.bytecode;

import static org.junit.Assert.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

public class BytecodeClassTest {
    private final ClassLoader loader = getClass().getClassLoader();

    @Test
    public void testInstanceOf() {
        
        BytecodeClass arrayList = BytecodeClassFactory.get("java/util/ArrayList", loader);
        
        assertTrue(arrayList.isInstanceOf("java/util/List", true));
        assertTrue(arrayList.isInstanceOf("java/lang/Object", false));
        assertTrue(arrayList.isInstanceOf("java/util/ArrayList", false));
        assertFalse(arrayList.isInstanceOf("java/util/Map", true));
        assertFalse(arrayList.isInstanceOf("java/util/Random", false));
        
        
        BytecodeClass concurrentHashMap = BytecodeClassFactory.get("java/util/concurrent/ConcurrentHashMap", loader);
        
        assertTrue(concurrentHashMap.isInstanceOf("java/util/concurrent/ConcurrentMap", true));
        assertTrue(concurrentHashMap.isInstanceOf("java/util/Map", true));
        assertTrue(concurrentHashMap.isInstanceOf("java/lang/Object", false));
        assertTrue(concurrentHashMap.isInstanceOf("java/util/concurrent/ConcurrentHashMap", false));
        assertFalse(concurrentHashMap.isInstanceOf("java/util/List", true));
        assertFalse(concurrentHashMap.isInstanceOf("java/util/Random", false));
        
        
        BytecodeClass concurrentMap = BytecodeClassFactory.get("java/util/concurrent/ConcurrentMap", loader);
        
        assertTrue(concurrentMap.isInstanceOf("java/util/concurrent/ConcurrentMap", true));
        assertTrue(concurrentMap.isInstanceOf("java/util/Map", true));
        assertTrue(concurrentMap.isInstanceOf("java/lang/Object", false));
        assertFalse(concurrentMap.isInstanceOf("java/util/concurrent/ConcurrentHashMap", false));
        assertFalse(concurrentMap.isInstanceOf("java/util/List", true));
        assertFalse(concurrentMap.isInstanceOf("java/util/Random", false));
        
    }

    @Test
    public void testIsAnnotationPresent() {
        String parnetAnnotationName = ParentAnnotation.class.getName();
        String childAnnotationName = ChildAnnotation.class.getName();

        BytecodeClass parentClass = BytecodeClassFactory.get(BytecodeUtils.toInternalName(ParentClass.class.getName()), loader);
        
        assertTrue(parentClass.isAnnotationPresent(childAnnotationName, true, true));
        assertTrue(parentClass.isAnnotationPresent(childAnnotationName, false, true));
        assertTrue(parentClass.isAnnotationPresent(childAnnotationName, false, false));
        assertTrue(parentClass.isAnnotationPresent(childAnnotationName, true, false));
        
        assertTrue(parentClass.isAnnotationPresent(parnetAnnotationName, true, true));
        assertTrue(parentClass.isAnnotationPresent(parnetAnnotationName, false, true));
        assertFalse(parentClass.isAnnotationPresent(parnetAnnotationName, false, false));
        assertFalse(parentClass.isAnnotationPresent(parnetAnnotationName, true, false));
        
        assertFalse(parentClass.isAnnotationPresent("no/such/Annotation", true, true));
        
        
        BytecodeClass childClass = BytecodeClassFactory.get(BytecodeUtils.toInternalName(ChildClass.class.getName()), loader);
        
        assertTrue(childClass.isAnnotationPresent(childAnnotationName, true, true));
        assertFalse(childClass.isAnnotationPresent(childAnnotationName, false, true));
        assertFalse(childClass.isAnnotationPresent(childAnnotationName, false, false));
        assertTrue(childClass.isAnnotationPresent(childAnnotationName, true, false));
        
        assertTrue(childClass.isAnnotationPresent(parnetAnnotationName, true, true));
        assertFalse(childClass.isAnnotationPresent(parnetAnnotationName, false, true));
        assertFalse(childClass.isAnnotationPresent(parnetAnnotationName, false, false));
        assertFalse(childClass.isAnnotationPresent(parnetAnnotationName, true, false));
        
        assertFalse(childClass.isAnnotationPresent("no/such/Annotation", true, true));
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ParentAnnotation {
        
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @ParentAnnotation
    public @interface ChildAnnotation {
        
    }
    
    @ChildAnnotation
    public static class ParentClass {
        
    }
    
    public static class ChildClass extends ParentClass {

    }
}
