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

package com.navercorp.pinpoint.profiler.instrument;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarFile;

import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.profiler.plugin.PluginConfig;
import com.navercorp.pinpoint.profiler.plugin.PluginInstrumentContext;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import javassist.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.instrument.classpool.IsolateMultipleClassPool;
import com.navercorp.pinpoint.profiler.instrument.classpool.MultipleClassPool;
import com.navercorp.pinpoint.profiler.instrument.classpool.NamedClassPool;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;

/**
 * @author emeroad
 */
@Deprecated
public class JavassistEngine implements InstrumentEngine {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isInfo = logger.isInfoEnabled();
    private final boolean isDebug = logger.isDebugEnabled();

    private final Instrumentation instrumentation;
    private final ObjectBinderFactory objectBinderFactory;
    private final Provider<ApiMetaDataService> apiMetaDataService;
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



    public JavassistEngine(Instrumentation instrumentation, ObjectBinderFactory objectBinderFactory, InterceptorRegistryBinder interceptorRegistryBinder, Provider<ApiMetaDataService> apiMetaDataService, final List<String> bootStrapJars) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (objectBinderFactory == null) {
            throw new NullPointerException("objectBinderFactory must not be null");
        }
        if (interceptorRegistryBinder == null) {
            throw new NullPointerException("interceptorRegistryBinder must not be null");
        }
        if (apiMetaDataService == null) {
            throw new NullPointerException("apiMetaDataService must not be null");
        }
        this.instrumentation = instrumentation;
        this.objectBinderFactory = objectBinderFactory;
        this.apiMetaDataService = apiMetaDataService;
        this.childClassPool = new IsolateMultipleClassPool(classPoolEventListener, new IsolateMultipleClassPool.ClassPoolHandler() {
            @Override
            public void handleClassPool(NamedClassPool systemClassPool) {
                try {
                    if (bootStrapJars != null) {
                        // append bootstarp jars
                        for (String bootStrapJar : bootStrapJars) {
                            systemClassPool.appendClassPath(bootStrapJar);
                        }
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


    @Override
    public InstrumentClass getClass(InstrumentContext instrumentContext, ClassLoader classLoader, String jvmInternalClassName, byte[] classFileBuffer) throws NotFoundInstrumentException {
        if (jvmInternalClassName == null) {
            throw new NullPointerException("jvmInternalClassName must not be null");
        }

        if (isDebug) {
            logger.debug("Get javassist class {}", jvmInternalClassName);
        }
        final CtClass cc = getCtClass(instrumentContext, classLoader, jvmInternalClassName, classFileBuffer);
        final ApiMetaDataService apiMetaDataService = this.apiMetaDataService.get();
        return new JavassistClass(objectBinderFactory, instrumentContext, interceptorRegistryBinder, apiMetaDataService, classLoader, cc);
    }


    private CtClass getCtClass(InstrumentContext instrumentContext, ClassLoader classLoader, String className, byte[] classfileBuffer) throws NotFoundInstrumentException {
        final NamedClassPool classPool = getClassPool(classLoader);
        try {
            if (classfileBuffer == null) {
                // compatibility code
                logger.info("classFileBuffer is null className:{}", className);
                return classPool.get(className);
            } else {
                final ClassPool contextCassPool = getContextClassPool(instrumentContext, classPool, className, classfileBuffer);
                return contextCassPool.get(className);
            }
        } catch (NotFoundException e) {
            throw new NotFoundInstrumentException(className + " class not found. Cause:" + e.getMessage(), e);
        }
    }

    private ClassPool getContextClassPool(InstrumentContext instrumentContext, NamedClassPool parent, String jvmInternalClassName, byte[] classfileBuffer) throws NotFoundException {
        final ClassPool contextCassPool = new ClassPool(parent);
        contextCassPool.childFirstLookup = true;

        final String javaName = JavaAssistUtils.jvmNameToJavaName(jvmInternalClassName);
        if (isDebug) {
            logger.debug("getContextClassPool() className={}", javaName);
        }
        final ClassPath byteArrayClassPath = new ByteArrayClassPath(javaName, classfileBuffer);
        contextCassPool.insertClassPath(byteArrayClassPath);

        // append plugin jar for jboss
        // plugin class not found in jboss classLoader
        if (instrumentContext instanceof PluginInstrumentContext) {
            final PluginConfig pluginConfig = ((PluginInstrumentContext) instrumentContext).getPluginConfig();
            if (pluginConfig != null) {
                String jarPath = pluginConfig.getPluginJar().getPath();
                contextCassPool.appendClassPath(jarPath);
            }
        }
        return contextCassPool;
    }


    public CtClass getClass(ClassLoader classLoader, String jvmInternalClassName) throws NotFoundInstrumentException {
        final NamedClassPool classPool = getClassPool(classLoader);
        try {
            return classPool.get(jvmInternalClassName);
        } catch (NotFoundException e) {
            throw new NotFoundInstrumentException(jvmInternalClassName + " class not found. Cause:" + e.getMessage(), e);
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
    public void appendToBootstrapClassPath(JarFile jarFile) {
        if (jarFile == null) {
            throw new NullPointerException("jarFile must not be null");
        }

        if (isInfo) {
            logger.info("appendToBootstrapClassPath:{}", jarFile);
        }
        synchronized (this) {
            this.instrumentation.appendToBootstrapClassLoaderSearch(jarFile);
            try {
                getClassPool(null).appendClassPath(jarFile.getName());
            } catch (NotFoundException e) {
                throw new PinpointException(e);
            }
        }
    }
}
