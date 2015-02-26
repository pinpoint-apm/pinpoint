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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.*;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.profiler.interceptor.GlobalInterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.interceptor.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.util.ScopePool;
import com.navercorp.pinpoint.profiler.util.ThreadLocalScopePool;

import javassist.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class JavaAssistByteCodeInstrumentor implements ByteCodeInstrumentor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isInfo = logger.isInfoEnabled();
    private final boolean isDebug = logger.isDebugEnabled();

    private final NamedClassPool rootClassPool;
    
    // TODO Need to separate childClassPool per class space to prevent collision(ex: multiple web applications on a Tomcat server)
    private final NamedClassPool childClassPool;

    private Agent agent;

    private final ScopePool scopePool = new ThreadLocalScopePool();

    private final ClassLoadChecker classLoadChecker = new ClassLoadChecker();
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    public static JavaAssistByteCodeInstrumentor createTestInstrumentor() {
        return new JavaAssistByteCodeInstrumentor();
    }

    // for test
    private JavaAssistByteCodeInstrumentor() {
        this.rootClassPool = createClassPool("rootClassPool");
        this.childClassPool = new NamedClassPool(rootClassPool, "childClassPool");
        this.interceptorRegistryBinder = new GlobalInterceptorRegistryBinder();
    }

    public JavaAssistByteCodeInstrumentor(Agent agent, InterceptorRegistryBinder interceptorRegistryBinder) {
        if (interceptorRegistryBinder == null) {
            throw new NullPointerException("interceptorRegistryBinder must not be null");
        }

        this.rootClassPool = createClassPool("rootClassPool");
        this.childClassPool = createChildClassPool(rootClassPool, "childClassPool");
        this.agent = agent;
        // Add Pinpoint classes to rootClassPool
        checkLibrary(this.getClass().getClassLoader(), this.rootClassPool, this.getClass().getName());
        this.interceptorRegistryBinder = interceptorRegistryBinder;
    }

    public Agent getAgent() {
        return agent;
    }

//    @Deprecated
    public ClassPool getClassPool() {
        return this.childClassPool;
    }

    @Override
    public Scope getScope(String scopeName) {
        final ScopeDefinition scopeDefinition = new DefaultScopeDefinition(scopeName, ScopeDefinition.Type.SIMPLE);
        return getScope(scopeDefinition);
    }



    public Scope getScope(ScopeDefinition scopeDefinition) {
        if (scopeDefinition == null) {
            throw new NullPointerException("scopeDefinition must not be null");
        }
        return this.scopePool.getScope(scopeDefinition);
    }

    private NamedClassPool createClassPool(String classPoolName) {
        NamedClassPool classPool = new NamedClassPool(null, classPoolName);
        classPool.appendClassPath(new LoaderClassPath(this.getClass().getClassLoader()));
        return classPool;
    }

    private NamedClassPool createChildClassPool(ClassPool rootClassPool, String classPoolName) {
        NamedClassPool childClassPool = new NamedClassPool(rootClassPool, classPoolName);
        childClassPool.childFirstLookup = true;
        return childClassPool;
    }


    private void appendClassPath(ClassPool classPool, String pathName) {
        try {
            classPool.appendClassPath(pathName);
        } catch (NotFoundException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("appendClassPath fail. lib not found. {}", e.getMessage(), e);
            }
        }
    }

    public void checkLibrary(ClassLoader classLoader, NamedClassPool classPool, String javassistClassName) {
        // if it's loaded by boot class loader, classLoader is null.
        if (classLoader == null) {
            return;
        }
        
        // synchronized ??
//        synchronized (classPool) {
            final boolean findClass = findClass(javassistClassName, classPool);
            if (findClass) {
                if (isDebug) {
                    logger.debug("checkLibrary cl:{} clPool:{}, class:{} found.", classLoader, classPool.getName(), javassistClassName);
                }
                return;
            }
            loadClassLoaderLibraries(classLoader, classPool);
//        }
    }


    @Override
    public InstrumentClass getClass(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) throws InstrumentException {
        CtClass cc = getClass(classLoader, javassistClassName);
        return new JavaAssistClass(this, cc, interceptorRegistryBinder);
    }
    
    public CtClass getClass(ClassLoader classLoader, String className) throws InstrumentException {
        final NamedClassPool classPool = findClassPool(classLoader);
        checkLibrary(classLoader, classPool, className);
        try {
            return classPool.get(className);
        } catch (NotFoundException e) {
            throw new InstrumentException(className + " class not found. Cause:" + e.getMessage(), e);
        }
    }

    private NamedClassPool findClassPool(ClassLoader classLoader) {
        if (classLoader == null) {
            // SystemClassLoader
        }
        // TODO fix find classPool
        return childClassPool;
    }

    @Override
    public Class<?> defineClass(ClassLoader classLoader, String defineClass, ProtectionDomain protectedDomain) throws InstrumentException {
        if (isInfo) {
            logger.info("defineClass class:{}, cl:{}", defineClass, classLoader);
        }
        try {
            final NamedClassPool classPool = findClassPool(classLoader);
            
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
        ClassPool classPool = findClassPool(classLoader);
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

    private void loadClassLoaderLibraries(ClassLoader classLoader, NamedClassPool classPool) {
        if (isInfo) {
            if (classLoader instanceof URLClassLoader) {
                URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
                URL[] urlList = urlClassLoader.getURLs();
                if (urlList != null) {
                    final String classLoaderName = classLoader.getClass().getName();
                    final String classPoolName = classPool.getName();
                    for (URL tempURL : urlList) {
                        String filePath = tempURL.getFile();
                        logger.info("classLoader lib cl:{} classPool:{} {} ", classLoaderName, classPoolName, filePath);
                    }
                }
            }
        }
        logger.info("appendClassPath. classPool:{} ClassLoader:{}", classPool.getName(), classLoader);
        classPool.appendClassPath(new LoaderClassPath(classLoader));
    }
}
