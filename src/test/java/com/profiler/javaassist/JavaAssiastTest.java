package com.profiler.javaassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.ClassFile;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class JavaAssiastTest {
    @Test
    public void newClass() {


    }
    @Test
    public void testAssist() throws NotFoundException, NoSuchMethodException {
        ClassPool getDefault = ClassPool.getDefault();
        CtClass ctClass = getDefault.get(String.class.getName());
//        System.out.println(ctClass)  ;
        String s = "";
//        ctClass.getMethod("valueOf", "(D)");

        CtMethod[] methods = ctClass.getMethods();
//        for(CtMethod method :  methods) {
//            System.out.println(method.getMethodInfo() +" " + method.getSignature());
//        }

        CtMethod endsWith = ctClass.getMethod("endsWith", "(Ljava/lang/String;)Z");
        System.out.println(endsWith.getMethodInfo());
        System.out.println(endsWith.getSignature());
        System.out.println(endsWith.getLongName());
        System.out.println(endsWith);
        System.out.println(endsWith.getName());
        System.out.println(endsWith.getMethodInfo().getName());
        System.out.println(endsWith.getMethodInfo().getDescriptor());

        Method endsWith1 = String.class.getMethod("endsWith", new Class[]{String.class});
        System.out.println(endsWith1);

    }

    @Test
    public void test() {
        sout("java/lang/String");
        sout("java.lang.String");

    }

    private void sout(String str) {
        URL resource = this.getClass().getClassLoader().getResource(str);
        System.out.println(resource);

//        new URLClassLoader()
    }
}
