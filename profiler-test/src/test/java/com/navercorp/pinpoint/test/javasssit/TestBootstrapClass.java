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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import com.navercorp.pinpoint.test.util.BytecodeUtils;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author emeroad
 */
@Deprecated
public class TestBootstrapClass {

    private static final String TEST_CLASS_NAME = "com.navercorp.pinpoint.profiler.javaassist.DynamicCreateTest";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void test() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, CannotCompileException {

        URLClassLoader classLoader = new URLClassLoader(new URL[]{});
        LoaderClassPath loaderClassPath = new LoaderClassPath(classLoader);

        ClassPool cp = new ClassPool();
        cp.appendClassPath(loaderClassPath);

        CtClass ctClass = cp.makeClass(TEST_CLASS_NAME);
        byte[] bytes = ctClass.toBytecode();

        logger.debug(classLoader.getClass().getName());
        Class<?> aClass = BytecodeUtils.defineClass(classLoader, TEST_CLASS_NAME, bytes);

        logger.debug("{}", aClass.getName());

    }


    @Test
    public void testJdkClassClassLoader() throws IOException {

        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        ClassLoader parent = systemClassLoader.getParent();
        logger.debug("parent:{}", parent);
        logger.debug("pparent:{}", parent.getParent());

        logger.debug("{}", String.class.getClassLoader());
        logger.debug("{}", TestBootstrapClass.class.getClassLoader());

    }

    @Test
    public void testReflection() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        ClassLoader contextClassLoader = java.lang.Thread.currentThread().getContextClassLoader();
        Class<?> interceptorRegistry = contextClassLoader.loadClass("com.navercorp.pinpoint.bootstrap.interceptor.registry.GlobalInterceptorRegistry");
        Method getInterceptorMethod = interceptorRegistry.getMethod("getInterceptor", int.class);
        Object interceptor = getInterceptorMethod.invoke(interceptorRegistry, Integer.valueOf(1));

        Method beforeMethod = interceptor.getClass().getMethod("before", java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class);
        beforeMethod.invoke(interceptor, null, null, null, null, null);
    }

}

