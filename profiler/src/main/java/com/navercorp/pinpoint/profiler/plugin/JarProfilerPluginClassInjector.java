/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarFile;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClassPool;
import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author Jongho Moon
 *
 */
public class JarProfilerPluginClassInjector implements ProfilerPluginClassInjector {
    private static final Logger logger = LoggerFactory.getLogger(JarProfilerPluginClassInjector.class);

    private static final Method ADD_URL;
    private static final Method DEFINE_CLASS;
    
    static {
        try {
            ADD_URL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            ADD_URL.setAccessible(true);
        } catch (Exception e) {
            throw new PinpointException("Cannot access URLClassLoader.addURL(URL)", e);
        }
        
        try {
            DEFINE_CLASS = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            DEFINE_CLASS.setAccessible(true);
        } catch (Exception e) {
            throw new PinpointException("Cannot access ClassLoader.defineClass(String, byte[], int, int)", e);
        }
    }
    
    public static JarProfilerPluginClassInjector of(Instrumentation instrumentation, InstrumentClassPool classPool, URL pluginJar) {
        try {
            JarFile jarFile = new JarFile(new File(pluginJar.toURI()));
            return new JarProfilerPluginClassInjector(instrumentation, classPool, pluginJar, jarFile);
        } catch (Exception e) {
            logger.warn("Failed to get JarFile {}", pluginJar, e);
            return null;
        }
    }
    
    private final Instrumentation instrumentation;
    private final InstrumentClassPool classPool;
    private final AtomicBoolean injectedToRoot = new AtomicBoolean(false);
    private final URL pluginJarURL;
    private final String pluginJarURLExternalForm;
    private final JarFile pluginJar;
    
    
    private JarProfilerPluginClassInjector(Instrumentation instrumentation, InstrumentClassPool classPool, URL pluginJarURL, JarFile pluginJar) {
        this.instrumentation = instrumentation;
        this.classPool = classPool;
        this.pluginJarURL = pluginJarURL;
        this.pluginJarURLExternalForm = pluginJarURL.toExternalForm();
        this.pluginJar = pluginJar;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
            if (classLoader == null) {
                return (Class<T>)injectToBootstrapClassLoader(className);
            } else if (classLoader instanceof URLClassLoader) {
                return (Class<T>)injectToURLClassLoader((URLClassLoader)classLoader, className);
            } else {
                return (Class<T>)injectToPlainClassLoader(classLoader, className);
            }
        } catch (Exception e) {
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }

    private Class<?> injectToBootstrapClassLoader(String className) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        if (injectedToRoot.compareAndSet(false, true)) {
            instrumentation.appendToBootstrapClassLoaderSearch(pluginJar);
            classPool.appendToBootstrapClassPath(pluginJar.getName());
        }
        
        return Class.forName(className, false, null);
    }

    private Class<?> injectToURLClassLoader(URLClassLoader classLoader, String className) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        final URL[] urls = classLoader.getURLs();
        if (urls != null) {

            boolean hasPluginJar = false;
            for (URL url : urls) {
                // if (url.equals(pluginJarURL)) { fix very slow
                // http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html
                final String externalForm = url.toExternalForm();
                if (pluginJarURLExternalForm.equals(externalForm)) {
                    hasPluginJar = true;
                    break;
                }
            }

            if (!hasPluginJar) {
                ADD_URL.invoke(classLoader, pluginJarURL);
            }
        }
        
        return classLoader.loadClass(className);
    }
    
    private Class<?> injectToPlainClassLoader(ClassLoader classLoader, String className) throws NotFoundException, IllegalArgumentException, IOException, CannotCompileException, IllegalAccessException, InvocationTargetException {
        ClassPool pool = new ClassPool();
        
        pool.appendClassPath(new LoaderClassPath(classLoader));
        pool.appendClassPath(pluginJar.getName());
        
        return injectToPlainClassLoader(pool, classLoader, className);
    }
    
    private Class<?> injectToPlainClassLoader(ClassPool pool, ClassLoader classLoader, String className) throws NotFoundException, IOException, CannotCompileException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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
            injectToPlainClassLoader(pool, classLoader, superClass.getName());
        }
        
        CtClass[] interfaces = ct.getInterfaces();
        
        for (CtClass i : interfaces) {
            injectToPlainClassLoader(pool, classLoader, i.getName());
        }
        
        Collection<String> refs = ct.getRefClasses();
        
        for (String ref : refs) {
            try {
                injectToPlainClassLoader(pool, classLoader, ref);
            } catch (NotFoundException e) {
                logger.warn("Skip a referenced class because of NotFoundException : ", e);
            }
        }
        
        byte[] bytes = ct.toBytecode();
        return (Class<?>)DEFINE_CLASS.invoke(classLoader, ct.getName(), bytes, 0, bytes.length);
    }
}
