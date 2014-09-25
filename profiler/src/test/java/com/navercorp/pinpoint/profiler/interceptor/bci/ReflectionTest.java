package com.nhn.pinpoint.profiler.interceptor.bci;

import javassist.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author emeroad
 */
public class ReflectionTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ClassPool pool = new ClassPool();
    @Before
    public void setUp() throws Exception {
        pool.appendSystemPath();
    }

    @Test
    public void test() throws NotFoundException {
        Constructor<?>[] constructors = String.class.getConstructors();
        for(Constructor c: constructors) {
            logger.debug(c.getName());
        }
        CtClass ctClass = pool.get("java.lang.String");
        CtConstructor[] constructors1 = ctClass.getConstructors();
        for(CtConstructor cc : constructors1) {
            logger.debug(cc.getName());
            logger.debug(cc.getLongName());
            logger.debug(cc.getSignature());
        }


    }
    @Test
    public void methodName() throws NotFoundException, ClassNotFoundException, NoSuchMethodException {
        CtClass ctClass = pool.get("java.lang.String");

        CtMethod subString = ctClass.getDeclaredMethod("substring", new CtClass[]{pool.get("int")});
        logger.debug("getLongName:{}", subString.getLongName());
        logger.debug("getName:{}", subString.getName());
        logger.debug("getDescriptor:{}", subString.getMethodInfo().getDescriptor());
        logger.debug("getDescriptor2:{}", subString.getMethodInfo2().getDescriptor());
        logger.debug("getSignature:{}", subString.getSignature());


        Method substring = String.class.getMethod("substring", int.class);
        logger.debug(substring.toString());
        logger.debug(Arrays.toString(substring.getParameterTypes()));
    }
}
