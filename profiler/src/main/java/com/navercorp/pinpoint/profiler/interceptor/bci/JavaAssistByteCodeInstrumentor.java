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

import java.lang.instrument.ClassFileTransformer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.DefaultInterceptorGroupDefinition;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InterceptorGroupDefinition;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.RetransformEventTrigger;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.interceptor.GlobalInterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.interceptor.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.util.ScopePool;
import com.navercorp.pinpoint.profiler.util.ThreadLocalScopePool;

/**
 * @author emeroad
 */
public class JavaAssistByteCodeInstrumentor implements ByteCodeInstrumentor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isInfo = logger.isInfoEnabled();
    private final boolean isDebug = logger.isDebugEnabled();

    private final MultipleClassPool childClassPool;

    private Agent agent;

    private final ScopePool scopePool = new ThreadLocalScopePool();

    private final ClassLoadChecker classLoadChecker = new ClassLoadChecker();
    private final InterceptorRegistryBinder interceptorRegistryBinder;
    private final RetransformEventTrigger retransformEventTrigger;

    private final IsolateMultipleClassPool.EventListener eventListener =  new IsolateMultipleClassPool.EventListener() {
        @Override
        public void onCreateClassPool(ClassLoader classLoader, NamedClassPool classPool) {
            dumpClassLoaderLibList(classLoader, classPool);
        }
    };

    public static JavaAssistByteCodeInstrumentor createTestInstrumentor() {
        return new JavaAssistByteCodeInstrumentor();
    }

    // for test
    private JavaAssistByteCodeInstrumentor() {
        this.childClassPool = new IsolateMultipleClassPool(eventListener, null);
        this.interceptorRegistryBinder = new GlobalInterceptorRegistryBinder();
        this.retransformEventTrigger = null;
    }

    public JavaAssistByteCodeInstrumentor(Agent agent, InterceptorRegistryBinder interceptorRegistryBinder, final String bootStrapJar, RetransformEventTrigger retransformEventTrigger) {
        if (interceptorRegistryBinder == null) {
            throw new NullPointerException("interceptorRegistryBinder must not be null");
        }
        if (retransformEventTrigger == null) {
            throw new NullPointerException("retransformEventTrigger must not be null");
        }

        this.childClassPool = new IsolateMultipleClassPool(eventListener, new IsolateMultipleClassPool.ClassPoolHandler() {
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
        
        this.agent = agent;
        this.interceptorRegistryBinder = interceptorRegistryBinder;
        this.retransformEventTrigger = retransformEventTrigger;
    }

    public Agent getAgent() {
        return agent;
    }

    @Override
    public InterceptorGroupInvocation getInterceptorGroupTransaction(String scopeName) {
        final InterceptorGroupDefinition scopeDefinition = new DefaultInterceptorGroupDefinition(scopeName);
        return getInterceptorGroupTransaction(scopeDefinition);
    }



    public InterceptorGroupInvocation getInterceptorGroupTransaction(InterceptorGroupDefinition scopeDefinition) {
        if (scopeDefinition == null) {
            throw new NullPointerException("scopeDefinition must not be null");
        }
        return this.scopePool.getScope(scopeDefinition);
    }

    @Override
    public InstrumentClass getClass(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) throws NotFoundInstrumentException {
        CtClass cc = getClass(classLoader, javassistClassName);
        return new JavaAssistClass(null, this, interceptorRegistryBinder, classLoader, cc);
    }
    
    public InstrumentClass getClass(DefaultProfilerPluginContext pluginContext, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) throws NotFoundInstrumentException {
        CtClass cc = getClass(classLoader, javassistClassName);
        return new JavaAssistClass(pluginContext, this, interceptorRegistryBinder, classLoader, cc);
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


    public Class<?> defineClass(ClassLoader classLoader, String defineClass, ProtectionDomain protectedDomain) throws InstrumentException {
        if (isInfo) {
            logger.info("defineClass class:{}, cl:{}", defineClass, classLoader);
        }
        try {
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            final NamedClassPool classPool = getClassPool(classLoader);
            
            // It's safe to synchronize on classLoader because current thread already hold lock on classLoader.
            // Without lock, maybe something could go wrong.
            synchronized (classLoader)  {
                if (this.classLoadChecker.exist(classLoader, defineClass)) {
                    return classLoader.loadClass(defineClass);
                } else {
                    final CtClass clazz = classPool.get(defineClass);

                    checkTargetClassInterface(clazz);

                    defineAbstractSuperClass(clazz, classLoader, protectedDomain);
                    defineNestedClass(clazz, classLoader, protectedDomain);
                    return clazz.toClass(classLoader, protectedDomain);
                }
            }
        } catch (NotFoundException e) {
            throw new InstrumentException(defineClass + " class not found. Cause:" + e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new InstrumentException(defineClass + " class define fail. cl:" + classLoader + " Cause:" + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new InstrumentException(defineClass + " class not found. Cause:" + e.getMessage(), e);
        }
    }

    private void checkTargetClassInterface(CtClass clazz) throws NotFoundException, InstrumentException {
        final String name = TargetClassLoader.class.getName();
        final CtClass[] interfaces = clazz.getInterfaces();
        for (CtClass anInterface : interfaces) {
            if (name.equals(anInterface.getName())) {
                return;
            }
        }
        throw new InstrumentException("newInterceptor() not support. " + clazz.getName());
    }

    private void defineAbstractSuperClass(CtClass clazz, ClassLoader classLoader, ProtectionDomain protectedDomain) throws NotFoundException, CannotCompileException {
        final CtClass superClass = clazz.getSuperclass();
        if (superClass == null) {
            // maybe java.lang.Object
            return;
        }
        final int modifiers = superClass.getModifiers();
        if (Modifier.isAbstract(modifiers)) {
            if (this.classLoadChecker.exist(classLoader, superClass.getName())) {
                // We have to check if abstract super classes is already loaded because it could be used by other classes unlike nested classes.
                return;
            }
            
            if (isInfo) {
                logger.info("defineAbstractSuperClass class:{} cl:{}", superClass.getName(), classLoader);
            }
            
            // If it was more strict we had to make a recursive call to check super class of super class.
            // But it seems like too much. We'll check direct super class only.
            superClass.toClass(classLoader, protectedDomain);
        }
    }

    private void defineNestedClass(CtClass clazz, ClassLoader classLoader, ProtectionDomain protectedDomain) throws NotFoundException, CannotCompileException {
        CtClass[] nestedClasses = clazz.getNestedClasses();
        if (nestedClasses.length == 0) {
            return;
        }
        for (CtClass nested : nestedClasses) {
            // load from inner-most to outer.
            defineNestedClass(nested, classLoader, protectedDomain);
            if (isInfo) {
                logger.info("defineNestedClass class:{} cl:{}", nested.getName(), classLoader);
            }
            nested.toClass(classLoader, protectedDomain);
        }
    }

    public boolean findClass(String javassistClassName, ClassPool classPool) {
        URL url = classPool.find(javassistClassName);
        if (url == null) {
            return false;
        }
        return true;
    }

    @Override
    public boolean findClass(ClassLoader classLoader, String javassistClassName) {
        ClassPool classPool = getClassPool(classLoader);
        return findClass(javassistClassName, classPool);
    }

    @Override
    public Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN) throws InstrumentException {
        Class<?> aClass = this.defineClass(classLoader, interceptorFQCN, protectedDomain);
        try {
            return (Interceptor) aClass.newInstance();
        } catch (InstantiationException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        }
    }

    @Override
    public Interceptor newInterceptor(ClassLoader classLoader, ProtectionDomain protectedDomain, String interceptorFQCN, Object[] params, Class[] paramClazz) throws InstrumentException {
        Class<?> aClass = this.defineClass(classLoader, interceptorFQCN, protectedDomain);
        try {
            Constructor<?> constructor = aClass.getConstructor(paramClazz);
            return (Interceptor) constructor.newInstance(params);
        } catch (InstantiationException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new InstrumentException(aClass + " instance create fail Cause:" + e.getMessage(), e);
        }

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

    @Override
    public void retransform(Class<?> target, ClassFileTransformer transformer) {
        this.retransformEventTrigger.retransform(target, transformer);
    }


    @Override
    public RetransformEventTrigger getRetransformEventTrigger() {
        return retransformEventTrigger;
    }
}
