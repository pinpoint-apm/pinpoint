/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.test.classloader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.navercorp.pinpoint.profiler.ClassFileTransformerDispatcher;
import javassist.ClassPool;
import javassist.CtClass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.ClassNameMatcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.MultiClassNameMatcher;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author emeroad
 */
public class JavassistTranslator implements Translator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentMap<String, MatchableClassFileTransformer> transformerMap = new ConcurrentHashMap<String, MatchableClassFileTransformer>();

    private final ClassLoader loader;
    private final ClassFileTransformerDispatcher dispatcher;
    private final ClassPool classPool;

    public JavassistTranslator(ClassLoader loader, ClassPool classPool, ClassFileTransformerDispatcher defaultTransformer) {
        if (defaultTransformer == null) {
            throw new NullPointerException("dispatcher must not be null");
        }
        if (classPool == null) {
            throw new NullPointerException("classPool must not be null");
        }

        this.loader = loader;
        this.dispatcher = defaultTransformer;
        this.classPool = classPool;
    }

    public void addTransformer(MatchableClassFileTransformer transformer) {
        // TODO extract matcher process
        final Matcher matcher = transformer.getMatcher();
        if (matcher instanceof ClassNameMatcher) {
            ClassNameMatcher classNameMatcher = (ClassNameMatcher) matcher;
            String className = classNameMatcher.getClassName();
            addTransformer0(transformer, className);
        } else if (matcher instanceof MultiClassNameMatcher) {
            final MultiClassNameMatcher classNameMatcher = (MultiClassNameMatcher) matcher;
            List<String> classNameList = classNameMatcher.getClassNames();
            for (String className : classNameList) {
                addTransformer0(transformer, className);
            }
        } else {
            throw new IllegalArgumentException("unsupported Matcher " + matcher);
        }

    }

    private void addTransformer0(MatchableClassFileTransformer transformer, String className) {
        final String checkClassInternalName = JavaAssistUtils.javaNameToJvmName(className);
        MatchableClassFileTransformer old = transformerMap.put(checkClassInternalName, transformer);
        if (old != null) {
            throw new IllegalStateException("Modifier already exist new:" + transformer.getClass() + " old:" + old.getMatcher());
        }
    }

    @Override
    public void start() {

    }

    @Override
    public byte[] transform(String className) throws ClassNotFoundException {
        logger.debug("loading className:{}", className);

        final String classInternalName = JavaAssistUtils.javaNameToJvmName(className);
        try {
            // Find Modifier from agent and try transforming
            final byte[] transformBytes = dispatcher.transform(this.loader, classInternalName, null, null, null);
            if (transformBytes != null) {
                logger.debug(classInternalName + " find in dispatcher");
                makeClass(classPool, transformBytes, classInternalName);
                return transformBytes;
            }

            final byte[] customTransformBytes = customTransformer(classPool, classInternalName);
            if (customTransformBytes != null) {
                logger.debug(classInternalName + " find in transformerMap");
                return customTransformBytes;
            }

            final CtClass ctClass = this.classPool.get(className);
            return ctClass.toBytecode();
        } catch (Throwable th) {
            throw new RuntimeException(className + " transform fail" , th);
        }

    }

    private byte[] customTransformer(ClassPool pool, String jvmClassName) {
        logger.info("Modify find classname:{}, loader:{}", jvmClassName, loader);
        MatchableClassFileTransformer transformer = transformerMap.get(jvmClassName);
        if (transformer == null) {
            return null;
        }
        logger.info("Modify jvmClassName:{},  modifier{}, loader:{}", jvmClassName, transformer, loader);


        final Thread thread = Thread.currentThread();
        final ClassLoader beforeClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        try {
            String javaClassName = JavaAssistUtils.jvmNameToJavaName(jvmClassName);
            byte[] transformBytes = transformer.transform(loader, javaClassName, null, null, null);
            makeClass(pool, transformBytes, jvmClassName);
            return transformBytes;
        } catch (IllegalClassFormatException e) {
            throw new RuntimeException(jvmClassName + " transform fail" , e);
        } finally {
            thread.setContextClassLoader(beforeClassLoader);
        }
    }

    private CtClass makeClass(ClassPool pool, byte[] transform, String jvmClassName) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("{} makeClass.", jvmClassName);
            }
            return pool.makeClass(new ByteArrayInputStream(transform));
        } catch (IOException ex) {
            throw new RuntimeException("Class make fail. jvmClass:" + jvmClassName + " Caused by:" + ex.getMessage(), ex);
        }
    }

}
