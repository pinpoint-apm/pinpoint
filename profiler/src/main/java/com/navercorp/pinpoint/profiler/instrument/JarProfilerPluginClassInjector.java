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
package com.navercorp.pinpoint.profiler.instrument;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import com.navercorp.pinpoint.profiler.plugin.ClassLoadingChecker;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
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
public class JarProfilerPluginClassInjector implements ClassInjector {
    private final Logger logger = LoggerFactory.getLogger(JarProfilerPluginClassInjector.class);
    private final boolean isDebug = logger.isDebugEnabled();

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
    
    private final PluginConfig pluginConfig;

    private final Object lock = new Object();
    private boolean injectedToRoot = false;

    public JarProfilerPluginClassInjector(PluginConfig pluginConfig) {
        if (pluginConfig == null) {
            throw new NullPointerException("pluginConfig must not be null");
        }
        this.pluginConfig = pluginConfig;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
            if (classLoader == null) {
                return (Class<T>)injectToBootstrapClassLoader(className);
            } else if (classLoader instanceof URLClassLoader) {
                final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                return (Class<T>)injectToURLClassLoader(urlClassLoader, className);
            } else {
                return (Class<T>)injectToPlainClassLoader(classLoader, className);
            }
        } catch (Exception e) {
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }

    private Class<?> injectToBootstrapClassLoader(String className) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
        synchronized (lock) {
            if (this.injectedToRoot == false) {
                this.injectedToRoot = true;
                pluginConfig.getInstrumentation().appendToBootstrapClassLoaderSearch(pluginConfig.getPluginJarFile());
                pluginConfig.getClassPool().appendToBootstrapClassPath(pluginConfig.getPluginJarFile().getName());
            }
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
                if (pluginConfig.getPluginJarURLExternalForm().equals(externalForm)) {
                    hasPluginJar = true;
                    break;
                }
            }

            if (!hasPluginJar) {
                ADD_URL.invoke(classLoader, pluginConfig.getPluginJar());
            }
        }

        return classLoader.loadClass(className);
    }
    
    private Class<?> injectToPlainClassLoader(ClassLoader classLoader, String className) throws NotFoundException, IllegalArgumentException, IOException, CannotCompileException, IllegalAccessException, InvocationTargetException {
        if (isDebug) {
            logger.debug("injectToPlainClassLoader className:{} cl:{}", className, classLoader);
        }
        logger.info("bootstrapCoreJarPath:{}", pluginConfig.getBootstrapCoreJarPath());

        final ClassPool pool = createClassPool(classLoader);

        // TODO ClassLoader + ClassName key?
        // TODO concurrent class loading
        final ClassLoadingChecker classLoadingChecker = new ClassLoadingChecker();
        return injectToPlainClassLoader(pool, classLoader, className, classLoadingChecker);
    }

    private ClassPool createClassPool(ClassLoader classLoader) throws NotFoundException {
        final ClassPool pool = new ClassPool();
        pool.appendClassPath(pluginConfig.getBootstrapCoreJarPath());

        pool.appendClassPath(new LoaderClassPath(classLoader));
        pool.appendClassPath(pluginConfig.getPluginJarFile().getName());
        return pool;
    }

    private Class<?> injectToPlainClassLoader(ClassPool pool, ClassLoader classLoader, String className, ClassLoadingChecker classLoadingChecker) throws NotFoundException, IOException, CannotCompileException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (pluginConfig.getProfilerPackageFilter().accept(className)) {
            if (isDebug) {
                logger.debug("ProfilerFilter skip class {}", className);
            }
            return null;
        }
        if (!pluginConfig.getPluginPackageFilter().accept(className)) {
            if (isDebug) {
                logger.debug("PluginFilter skip class:{}", className);
            }
            return null;
        }
        if (!classLoadingChecker.isFirstLoad(className)) {
            if (isDebug) {
                logger.debug("skip already loaded class:{}", className);
            }
            return null;
        }

        Class<?> c = null;
        try {
            c = classLoader.loadClass(className);
            if (isDebug) {
                logger.debug("loadClass:{}", className);
            }
        } catch (ClassNotFoundException ex) {
            if (isDebug) {
                logger.debug("ClassNotFound {}", ex.getMessage());
            }
        }
        if (c != null) {
            return c;
        }

        final CtClass ct = pool.getOrNull(className);
        if (ct == null) {
            throw new NotFoundException(className);
        }


        final CtClass superClass = ct.getSuperclass();
        if (superClass != null) {
            if ("java.lang.Object".equals(superClass.getName())) {
                return null;
            }
            injectToPlainClassLoader(pool, classLoader, superClass.getName(), classLoadingChecker);
        }

        final CtClass[] interfaces = ct.getInterfaces();
        for (CtClass ctInterface : interfaces) {
            injectToPlainClassLoader(pool, classLoader, ctInterface.getName(), classLoadingChecker);
        }
        @SuppressWarnings("unchecked")
        final Collection<String> referenceClassList = ct.getRefClasses();
        if (isDebug) {
            logger.debug("target:{} referenceClassList:{}", className, referenceClassList);
        }
        for (String referenceClass : referenceClassList) {
            try {
                injectToPlainClassLoader(pool, classLoader, referenceClass, classLoadingChecker);
            } catch (NotFoundException e) {
                logger.warn("Skip a referenced class because of NotFoundException : {}", e.getMessage(), e);
            }
        }
        if (logger.isInfoEnabled()) {
            logger.debug("defineClass pluginClass:{} cl:{}", className, classLoader);
        }
        final byte[] bytes = ct.toBytecode();
        return (Class<?>)DEFINE_CLASS.invoke(classLoader, ct.getName(), bytes, 0, bytes.length);
    }

}
