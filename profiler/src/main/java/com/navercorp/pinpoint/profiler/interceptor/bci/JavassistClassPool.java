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

package com.navercorp.pinpoint.profiler.interceptor.bci;

import java.net.URL;
import java.net.URLClassLoader;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClassPool;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginInstrumentContext;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.interceptor.InterceptorRegistryBinder;

/**
 * @author emeroad
 */
public class JavassistClassPool implements InstrumentClassPool {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isInfo = logger.isInfoEnabled();
    private final boolean isDebug = logger.isDebugEnabled();

    private final MultipleClassPool childClassPool;
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    private final IsolateMultipleClassPool.EventListener classPoolEventListener =  new IsolateMultipleClassPool.EventListener() {
        @Override
        public void onCreateClassPool(ClassLoader classLoader, NamedClassPool classPool) {
            dumpClassLoaderLibList(classLoader, classPool);
        }

        private void dumpClassLoaderLibList(ClassLoader classLoader, NamedClassPool classPool) {
            if (isInfo) {
                if (classLoader instanceof URLClassLoader) {
                    final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                    final URL[] urlList = urlClassLoader.getURLs();
                    if (urlList != null) {
                        final String classLoaderName = classLoader.getClass().getName();
                        final String classPoolName = classPool.getName();
                        logger.info("classLoader lib cl:{} classPool:{}", classLoaderName, classPoolName);
                        for (URL tempURL : urlList) {
                            String filePath = tempURL.getFile();
                            logger.info("lib:{} ", filePath);
                        }
                    }
                }
            }
        }
    };

    public JavassistClassPool(InterceptorRegistryBinder interceptorRegistryBinder, final String bootStrapJar) {
        if (interceptorRegistryBinder == null) {
            throw new NullPointerException("interceptorRegistryBinder must not be null");
        }

        this.childClassPool = new IsolateMultipleClassPool(classPoolEventListener, new IsolateMultipleClassPool.ClassPoolHandler() {
            @Override
            public void handleClassPool(NamedClassPool systemClassPool) {
                try {
                    if (bootStrapJar != null) {
                        // append bootstarp-core
                        systemClassPool.appendClassPath(bootStrapJar);
                    }
                } catch (NotFoundException ex) {
                    throw new PinpointException("bootStrapJar not found. Caused by:" + ex.getMessage(), ex);
                }
                // append pinpoint classLoader
                systemClassPool.appendClassPath(new ClassClassPath(this.getClass()));
            }
        });
        
        this.interceptorRegistryBinder = interceptorRegistryBinder;
    }

    public InstrumentClass getClass(ClassLoader classLoader, String jvmInternalClassName, byte[] classFileBuffer) throws NotFoundInstrumentException {
        CtClass cc = getClass(classLoader, jvmInternalClassName);
        return new JavassistClass(null, interceptorRegistryBinder, classLoader, cc);
    }
    
    @Override
    public InstrumentClass getClass(ProfilerPluginInstrumentContext pluginContext, ClassLoader classLoader, String jvmInternalClassName, byte[] classFileBuffer) throws NotFoundInstrumentException {
        CtClass cc = getClass(classLoader, jvmInternalClassName);
        return new JavassistClass(pluginContext, interceptorRegistryBinder, classLoader, cc);
    }
    
    public CtClass getClass(ClassLoader classLoader, String className) throws NotFoundInstrumentException {
        final NamedClassPool classPool = getClassPool(classLoader);
        try {
            return classPool.get(className);
        } catch (NotFoundException e) {
            throw new NotFoundInstrumentException(className + " class not found. Cause:" + e.getMessage(), e);
        }
    }

    public NamedClassPool getClassPool(ClassLoader classLoader) {
        return childClassPool.getClassPool(classLoader);
    }

    public boolean hasClass(String javassistClassName, ClassPool classPool) {
        URL url = classPool.find(javassistClassName);
        if (url == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasClass(ClassLoader classLoader, String classBinaryName) {
        ClassPool classPool = getClassPool(classLoader);
        return hasClass(classBinaryName, classPool);
    }

    @Override
    public void appendToBootstrapClassPath(String jar) {
        try {
            getClassPool(null).appendClassPath(jar);
        } catch (NotFoundException e) {
            throw new PinpointException(e);
        }
    }
}