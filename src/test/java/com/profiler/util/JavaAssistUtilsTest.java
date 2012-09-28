package com.profiler.util;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaAssistUtilsTest {
    private final Logger logger = LoggerFactory.getLogger(JavaAssistUtilsTest.class.getName());
    private ClassPool pool;

    @Before
    public void setUp() throws Exception {
        pool = new ClassPool();
        pool.appendSystemPath();
    }

    @Test
    public void testGetParameterDescription() throws Exception {
        CtClass ctClass = pool.get("java.lang.String");
        CtMethod substring = ctClass.getDeclaredMethod("substring", new CtClass[]{CtClass.intType});

        String ctDescription = JavaAssistUtils.getParameterDescription(substring.getParameterTypes());
        logger.info(ctDescription);

        String clsDescription = JavaAssistUtils.getParameterDescription(new Class[]{int.class});
        logger.info(clsDescription);
        Assert.assertEquals(ctDescription, clsDescription);
    }

    @Test
    public void testGetParameterDescription2() throws Exception {
        String clsDescription = JavaAssistUtils.getParameterDescription(new Class[]{String.class, Integer.class});
        logger.info(clsDescription);
        Assert.assertEquals("(java.lang.String, java.lang.Integer)", clsDescription);
    }

}
