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

package com.navercorp.pinpoint.profiler.javaassist;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.test.util.BytecodeUtils;

/**
 * @author emeroad
 */
public class TestBootstrapClass {

    private static final String TEST_CLASS_NAME = "com.navercorp.pinpoint.profiler.javaassist.DynamicTest";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void test() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException, CannotCompileException {

        URLClassLoader classLoader = new URLClassLoader(new URL[]{});
        LoaderClassPath loaderClassPath = new LoaderClassPath(classLoader);

        ClassPool cp = new ClassPool();
        cp.appendClassPath(loaderClassPath);

        CtClass ctClass = cp.makeClass(TEST_CLASS_NAME);
        byte[] bytes = ctClass.toBytecode();

        logger.info(classLoader.getClass().getName());
        Class<?> aClass = BytecodeUtils.defineClass(classLoader, TEST_CLASS_NAME, bytes);

        logger.info("{}", aClass.getName());

    }


    @Test
    public void testJdkClassClassLoader() throws IOException {
        URL url = new URL("http://test");


        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        logger.info(urlConnection.toString());
        logger.info("{}", urlConnection.getClass().getClassLoader());
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        ClassLoader parent = systemClassLoader.getParent();
        logger.info("parent:{}", parent);
        logger.info("pparent:{}", parent.getParent());

        logger.info("{}", String.class.getClassLoader());
        logger.info("{}", TestBootstrapClass.class.getClassLoader());


        urlConnection.disconnect();


    }

    @Test
    public void testReflection() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        java.lang.ClassLoader contextClassLoader = java.lang.Thread.currentThread().getContextClassLoader();
        java.lang.Class<?> interceptorRegistry = contextClassLoader.loadClass("com.navercorp.pinpoint.bootstrap.interceptor.registry.GlobalInterceptorRegistry");
        java.lang.reflect.Method getInterceptorMethod = interceptorRegistry.getMethod("getInterceptor", new java.lang.Class[]{int.class});
        java.lang.Object interceptor = getInterceptorMethod.invoke(interceptorRegistry, Integer.valueOf(1));

        java.lang.reflect.Method beforeMethod = interceptor.getClass().getMethod("before", java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class);
        beforeMethod.invoke(interceptor, null, null, null, null, null);
    }

}

