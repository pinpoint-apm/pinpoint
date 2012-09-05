package com.profiler.interceptor.bci;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import org.junit.Test;

import java.lang.reflect.Constructor;

public class ReflectionTest {
    @Test
    public void test() throws NotFoundException {
        Constructor<?>[] constructors = String.class.getConstructors();
        for(Constructor c: constructors) {
            System.out.println(c.getName());
        }

        ClassPool pool = new ClassPool();
        pool.appendSystemPath();
        CtClass ctClass = pool.get("java.lang.String");
        CtConstructor[] constructors1 = ctClass.getConstructors();
        for(CtConstructor cc : constructors1) {
            System.out.println(cc.getName());
            System.out.println(cc.getLongName());
            System.out.println(cc.getSignature());
        }
    }
}
