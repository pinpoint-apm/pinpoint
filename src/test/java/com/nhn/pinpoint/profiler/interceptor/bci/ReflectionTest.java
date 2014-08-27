package com.nhn.pinpoint.profiler.interceptor.bci;

import javassist.*;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author emeroad
 */
public class ReflectionTest {
    private ClassPool pool = new ClassPool();
    @Before
    public void setUp() throws Exception {
        pool.appendSystemPath();
    }

    @Test
    public void test() throws NotFoundException {
        Constructor<?>[] constructors = String.class.getConstructors();
        for(Constructor c: constructors) {
            System.out.println(c.getName());
        }
        CtClass ctClass = pool.get("java.lang.String");
        CtConstructor[] constructors1 = ctClass.getConstructors();
        for(CtConstructor cc : constructors1) {
            System.out.println(cc.getName());
            System.out.println(cc.getLongName());
            System.out.println(cc.getSignature());
        }


    }
    @Test
    public void methodName() throws NotFoundException, ClassNotFoundException, NoSuchMethodException {
        CtClass ctClass = pool.get("java.lang.String");

        CtMethod subString = ctClass.getDeclaredMethod("substring", new CtClass[]{pool.get("int")});
        System.out.println("getLongName:" + subString.getLongName());
        System.out.println("getName:"+ subString.getName());
        System.out.println("getDescriptor:"+ subString.getMethodInfo().getDescriptor());
        System.out.println("getDescriptor2:"+ subString.getMethodInfo2().getDescriptor());
        System.out.println("getSignature:"+ subString.getSignature());


        Method substring = String.class.getMethod("substring", int.class);
        System.out.println(substring.toString());
        System.out.println(Arrays.toString(substring.getParameterTypes()));

//       M
    }
}
