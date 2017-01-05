/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.test.javasssit;


import com.navercorp.pinpoint.test.util.LoaderUtils;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * @author emeroad
 */
@Deprecated
public class JavaAssistInterceptorTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void afterCatch() throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException {
        ClassPool pool = new ClassPool(true);
        Loader loader = getLoader(pool);

        CtClass ctClass = pool.get("com.navercorp.pinpoint.test.javasssit.mock.TestObject");
        CtClass object = pool.get("java.lang.String");

        logger.debug("target:{}", ctClass);


        CtMethod callA = ctClass.getDeclaredMethod("callA", null);
        logger.debug("callA:{}", callA);
//      callA.addLocalVariable("__test", object);
        final String before = "{ java.lang.Throwable __throwable = null; java.lang.String __test = \"abc\"; System.out.println(\"BEFORE\" + __test);";
//      callA.insertBefore();
//      callA.insertAfter("System.out.println(\"AFTER\" + __test);");
//      final String AFTER =  "finally {System.out.println(\"AFTER\" + __test);}}";
        final String after = "}";
//      callA.addCatch();

//      callA.addCatch("System.out.println(\"AFTER\");", pool.get("java.lang.Throwable"));
        callA.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                logger.debug("edit class{}", m.getClassName());
                try {
                    logger.debug("edit method:{}", m.getMethod().toString());
                } catch (NotFoundException e) {
                    logger.warn("getMethod() fail. Caused:{}", e.getMessage(), e);
                }
                logger.debug(m.getMethodName());
                m.replace(before + " try {$_ = $proceed($$); System.out.println(\"end---\"+ $_);} catch (java.lang.Throwable ex) { __throwable = ex; System.out.println(\"catch\"); } " + after);
            }
        });


        Class aClass = loader.loadClass(ctClass.getName());
        java.lang.reflect.Method callA1 = aClass.getMethod("callA");
        Object target = aClass.newInstance();
        Object result = callA1.invoke(target);
        logger.debug("result:{}", result);
    }

    @Test
    public void afterCatch2() throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException {
        ClassPool pool = new ClassPool(true);
        Loader loader = getLoader(pool);
        CtClass ctClass = pool.get("com.navercorp.pinpoint.test.javasssit.mock.TestObject");
        CtClass object = pool.get("java.lang.String");
        logger.debug("target:{}", ctClass);


        CtMethod callA = ctClass.getDeclaredMethod("callA", null);
        logger.debug("callA:{}", callA);
        callA.addLocalVariable("__test", object);
        callA.insertBefore("{ __test = \"abc\"; System.out.println(\"BEFORE\" + __test); }");
        callA.insertAfter("{ System.out.println(\"AFTER\"); }");
        callA.addCatch("{ System.out.println(\"catch\"); throw $e; }", pool.get("java.lang.Throwable"));

        Class aClass = loader.loadClass(ctClass.getName());
        java.lang.reflect.Method callA1 = aClass.getMethod("callA");
        Object target = aClass.newInstance();
        Object result = callA1.invoke(target);
        logger.debug("result:{}", result);

    }

    @Test
    public void around() throws NotFoundException, CannotCompileException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException {
        ClassPool pool = new ClassPool(true);
        Loader loader = getLoader(pool);


        CtClass ctClass = pool.get("com.navercorp.pinpoint.test.javasssit.mock.TestObject");
        CtClass object = pool.get("java.lang.String");
        logger.debug("target:{}", ctClass);

        CtMethod callA = ctClass.getDeclaredMethod("callA", null);
        logger.debug("callA:{}", callA);
        callA.addLocalVariable("__test", object);
        String inti = "__test = \"abc\";";
//        callA.insertBefore("__test = \"abc\";);
        callA.insertBefore("{com.navercorp.pinpoint.test.javasssit.mock.TestObject.before();}");
        callA.insertAfter("{com.navercorp.pinpoint.test.javasssit.mock.TestObject.after();}");
        callA.addCatch("{ com.navercorp.pinpoint.test.javasssit.mock.TestObject.callCatch(); throw $e; }", pool.get("java.lang.Throwable"));

        Class aClass = loader.loadClass(ctClass.getName());
        java.lang.reflect.Method callA1 = aClass.getMethod("callA");
        Object target = aClass.newInstance();
        Object result = callA1.invoke(target);
        logger.debug("result:{}", result);

    }

    private Loader getLoader(ClassPool pool) {
        return LoaderUtils.createLoader(pool);
    }
}
