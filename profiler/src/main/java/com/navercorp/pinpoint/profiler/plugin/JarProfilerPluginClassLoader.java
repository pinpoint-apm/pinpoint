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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.jar.JarFile;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author Jongho Moon
 *
 */
public class JarProfilerPluginClassLoader implements ProfilerPluginClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(JarProfilerPluginClassLoader.class);

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
    
    public static JarProfilerPluginClassLoader of(URL pluginJar) {
        try {
            JarFile jarFile = new JarFile(new File(pluginJar.toURI()));
            return new JarProfilerPluginClassLoader(pluginJar, jarFile);
        } catch (Exception e) {
            logger.warn("Failed to get JarFile {}", pluginJar, e);
            return null;
        }
    }
    
    private final URL pluginJarURL;
    private final JarFile pluginJar;
    
    
    private JarProfilerPluginClassLoader(URL pluginJarURL, JarFile pluginJar) {
        this.pluginJarURL = pluginJarURL;
        this.pluginJar = pluginJar;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> loadClass(ClassLoader classLoader, String className) {
        ClassLoader targetClassLoader = classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader;

        try {
            if (targetClassLoader instanceof URLClassLoader) {
                return (Class<T>)loadFromURLClassLoader((URLClassLoader)targetClassLoader, className);
            } else {
                return (Class<T>)loadFromOtherClassLoader(targetClassLoader, className);
            }
        } catch (Exception e) {
            logger.warn("Failed to load plugin class {} with classLoader {}", className, targetClassLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + targetClassLoader, e);
        }
    }
    
    private Class<?> loadFromURLClassLoader(URLClassLoader classLoader, String className) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        URL[] urls = classLoader.getURLs();
        
        boolean hasPluginJar = false;
        for (URL url : urls) {
            if (url.equals(pluginJarURL)) {
                hasPluginJar = true;
                break;
            }
        }
        
        if (!hasPluginJar) {
            ADD_URL.invoke(classLoader, pluginJarURL);
        }
        
        return classLoader.loadClass(className);
    }
    
    private Class<?> loadFromOtherClassLoader(ClassLoader classLoader, String className) throws NotFoundException, IllegalArgumentException, IOException, CannotCompileException, IllegalAccessException, InvocationTargetException {
        ClassPool pool = new ClassPool();
        
        pool.appendClassPath(new LoaderClassPath(classLoader));
        pool.appendClassPath(pluginJar.getName());
        
        return loadFromOtherClassLoader(pool, classLoader, className);
    }
    
    private Class<?> loadFromOtherClassLoader(ClassPool pool, ClassLoader classLoader, String className) throws NotFoundException, IOException, CannotCompileException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> c = null;
        
        try {
            c = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            
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
}
