/*
 * Copyright 2016 NAVER Corp.
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

import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.plugin.ClassLoadingChecker;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Collection;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
public class PlainClassLoaderHandler implements ClassInjector {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final Method DEFINE_CLASS;

    static {
        try {
            DEFINE_CLASS = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            DEFINE_CLASS.setAccessible(true);
        } catch (Exception e) {
            throw new PinpointException("Cannot access ClassLoader.defineClass(String, byte[], int, int)", e);
        }
    }

    private final PluginConfig pluginConfig;

    public PlainClassLoaderHandler(PluginConfig pluginConfig) {
        if (pluginConfig == null) {
            throw new NullPointerException("pluginConfig must not be null");
        }
        this.pluginConfig = pluginConfig;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> injectClass(ClassLoader classLoader, String className) {
        try {
            return (Class<T>) injectClass0(classLoader, className);
        } catch (Exception e) {
            logger.warn("Failed to load plugin class {} with classLoader {}", className, classLoader, e);
            throw new PinpointException("Failed to load plugin class " + className + " with classLoader " + classLoader, e);
        }
    }

    private Class<?> injectClass0(ClassLoader classLoader, String className) throws NotFoundException, IllegalArgumentException, IOException, CannotCompileException, IllegalAccessException, InvocationTargetException {
        if (isDebug) {
            logger.debug("injectClass0 className:{} cl:{}", className, classLoader);
        }
        logger.info("bootstrapCoreJarPath:{}", pluginConfig.getBootstrapCoreJarPath());

        final ClassPool pool = createClassPool(classLoader);

        // TODO ClassLoader + ClassName key?
        // TODO concurrent class loading
        final ClassLoadingChecker classLoadingChecker = new ClassLoadingChecker();
        classLoadingChecker.isFirstLoad(className);
        return injectClass0(pool, classLoader, className, classLoadingChecker);
    }

    private ClassPool createClassPool(ClassLoader classLoader) throws NotFoundException {
        final ClassPool pool = new ClassPool();
        final String bootstrapCoreJarPath = pluginConfig.getBootstrapCoreJarPath();
        pool.appendClassPath(bootstrapCoreJarPath);

        final LoaderClassPath loaderClassPath = new LoaderClassPath(classLoader);
        pool.appendClassPath(loaderClassPath);

        final String pluginJarFileName = pluginConfig.getPluginJarFile().getName();
        pool.appendClassPath(pluginJarFileName);
        return pool;
    }

    private Class<?> injectClass0(ClassPool pool, ClassLoader classLoader, String className, ClassLoadingChecker classLoadingChecker) throws NotFoundException, IOException, CannotCompileException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
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
            if (!"java.lang.Object".equals(superClass.getName())) {
                if (!isSkipClass(superClass.getName(), classLoadingChecker)) {
                    injectClass0(pool, classLoader, superClass.getName(), classLoadingChecker);
                }
            }
        }

        final CtClass[] interfaces = ct.getInterfaces();
        for (CtClass ctInterface : interfaces) {
            if (!isSkipClass(ctInterface.getName(), classLoadingChecker)) {
                if(isDebug) {
                    logger.debug("interface : {}", ctInterface.getName());
                }
                injectClass0(pool, classLoader, ctInterface.getName(), classLoadingChecker);
            }
        }
        @SuppressWarnings("unchecked")
        final Collection<String> referenceClassList = ct.getRefClasses();
        if (isDebug) {
            logger.debug("target:{} referenceClassList:{}", className, referenceClassList);
        }
        for (String referenceClass : referenceClassList) {
            try {
                if (!isSkipClass(referenceClass, classLoadingChecker)) {
                    if(isDebug) {
                        logger.debug("reference : {}", referenceClass);
                    }
                    injectClass0(pool, classLoader, referenceClass, classLoadingChecker);
                }
            } catch (NotFoundException e) {
                logger.warn("Skip a referenced class because of NotFoundException : {}", e.getMessage(), e);
            }
        }
        if (logger.isInfoEnabled()) {
            logger.info("defineClass pluginClass:{} cl:{}", className, classLoader);
        }
        final byte[] bytes = ct.toBytecode();
        return (Class<?>) DEFINE_CLASS.invoke(classLoader, ct.getName(), bytes, 0, bytes.length);
    }

    private boolean isSkipClass(final String className, final ClassLoadingChecker classLoadingChecker) {
        if (!pluginConfig.getPluginPackageFilter().accept(className)) {
            if (isDebug) {
                logger.debug("PluginFilter skip class:{}", className);
            }
            return true;
        }
        if (!classLoadingChecker.isFirstLoad(className)) {
            if (isDebug) {
                logger.debug("skip already loaded class:{}", className);
            }
            return true;
        }

        return false;
    }
}