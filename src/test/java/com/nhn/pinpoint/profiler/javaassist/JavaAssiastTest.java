package com.nhn.pinpoint.profiler.javaassist;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.net.URL;


public class JavaAssiastTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ClassPool pool;

    @Before
    public void setUp() throws Exception {
        pool = new ClassPool();
        pool.appendSystemPath();
    }

    @Test
    public void newClass() {

    }

    @Test
    public void testAssist() throws NotFoundException, NoSuchMethodException {

        CtClass ctClass = pool.get(String.class.getName());
        logger.info(ctClass.toString());
        String s = "";
//        ctClass.getMethod("valueOf", "(D)");

        CtMethod[] methods = ctClass.getMethods();
//        for(CtMethod method :  methods) {
//            System.out.println(method.getMethodInfo() +" " + method.getSignature());
//        }

        CtMethod endsWith = ctClass.getMethod("endsWith", "(Ljava/lang/String;)Z");
        logger.info(endsWith.getMethodInfo().toString());
        logger.info(endsWith.getSignature());
        logger.info(endsWith.getLongName());
        logger.info(endsWith.toString());
        logger.info(endsWith.getName());
        logger.info(endsWith.getMethodInfo().getName());
        logger.info(endsWith.getMethodInfo().getDescriptor());

        Method endsWith1 = String.class.getMethod("endsWith", new Class[]{String.class});
        logger.debug(endsWith1.toString());

    }

    @Test
    public void test() {
        sout("/java/lang/String.class");
        sout("java.lang.String.class");

    }

    private void sout(String str) {
        URL resource = this.getClass().getClassLoader().getResource(str);
        logger.info("" + resource);

//        new URLClassLoader()
    }

    @Test
    public void innerClass() throws NotFoundException {
        CtClass testClass = pool.get("com.nhn.pinpoint.profiler.javaassist.TestClass");
        logger.debug(testClass.toString());
        CtClass[] nestedClasses = testClass.getNestedClasses();
        for(CtClass nested : nestedClasses) {
            logger.debug("nestedClass:" + nested);
        }

        CtClass innerClass = pool.get("com.nhn.pinpoint.profiler.javaassist.TestClass$InnerClass");
        logger.debug(""+ innerClass);

        CtClass class1 = pool.get("com.nhn.pinpoint.profiler.javaassist.TestClass$1");
        logger.debug(""+ class1);
    }
}
