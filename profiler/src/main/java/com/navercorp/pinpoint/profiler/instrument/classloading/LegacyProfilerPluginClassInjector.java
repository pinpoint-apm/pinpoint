/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.instrument.classloading;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import com.navercorp.pinpoint.profiler.instrument.classloading.ClassInjector;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @deprecated Since 1.6.1
 * @author Jongho Moon
 */
@Deprecated
public class LegacyProfilerPluginClassInjector implements ClassInjector {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Method DEFINE_CLASS;
    
    static {
        try {
            DEFINE_CLASS = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            DEFINE_CLASS.setAccessible(true);
        } catch (Exception e) {
            throw new PinpointException("Cannot access ClassLoader.defineClass(String, byte[], int, int)", e);
        }
    }
    
    private final ClassLoader sourceClassLoader;
    
    public LegacyProfilerPluginClassInjector(ClassLoader sourceClassLoader) {
        if (sourceClassLoader == null) {
            throw new NullPointerException("sourceClassLoader must not be null");
        }
        this.sourceClassLoader = sourceClassLoader;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {

        classLoader = getClassLoader(classLoader);

        try {
            return (Class<T>)loadFromOtherClassLoader(classLoader, className);
        } catch (Exception e) {
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }
    
    private Class<?> loadFromOtherClassLoader(ClassLoader classLoader, String className) throws NotFoundException, IllegalArgumentException, IOException, CannotCompileException, IllegalAccessException, InvocationTargetException {
        ClassPool pool = new ClassPool();
        
        pool.appendClassPath(new LoaderClassPath(classLoader));
        pool.appendClassPath(new LoaderClassPath(sourceClassLoader));
        
        return loadFromOtherClassLoader(pool, classLoader, className);
    }
    
    private Class<?> loadFromOtherClassLoader(ClassPool pool, ClassLoader classLoader, String className) throws NotFoundException, IOException, CannotCompileException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> c = null;
        
        try {
            c = classLoader.loadClass(className);
        } catch (ClassNotFoundException ignore) {
            
        }
        
        if (c != null) {
            return c;
        }
        
        CtClass ct = pool.get(className);
        
        if (ct == null) {
            throw new NotFoundException(className);
        }
        
        
        CtClass superClass = ct.getSuperclass();
        
        if (superClass != null) {
            loadFromOtherClassLoader(pool, classLoader, superClass.getName());
        }
        
        CtClass[] interfaces = ct.getInterfaces();
        
        for (CtClass i : interfaces) {
            loadFromOtherClassLoader(pool, classLoader, i.getName());
        }
        
        Collection<String> refs = ct.getRefClasses();
        
        for (String ref : refs) {
            try {
                loadFromOtherClassLoader(pool, classLoader, ref);
            } catch (NotFoundException e) {
                logger.warn("Skip a referenced class because of NotFoundException : ", e);
            }
        }
        
        byte[] bytes = ct.toBytecode();
        return (Class<?>)DEFINE_CLASS.invoke(classLoader, ct.getName(), bytes, 0, bytes.length);
    }

    @Override
    public InputStream getResourceAsStream(ClassLoader classLoader, String classPath) {

        classLoader = getClassLoader(classLoader);

        return classLoader.getResourceAsStream(classPath);
    }

    private static ClassLoader getClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            return ClassLoader.getSystemClassLoader();
        }
        return classLoader;
    }
}
