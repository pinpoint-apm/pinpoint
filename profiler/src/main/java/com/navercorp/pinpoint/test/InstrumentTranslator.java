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

package com.navercorp.pinpoint.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import javassist.Translator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.ClassNameMatcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.MultiClassNameMatcher;
import com.navercorp.pinpoint.profiler.ClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.plugin.xml.transformer.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author emeroad
 */
public class InstrumentTranslator implements Translator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final ConcurrentMap<String, MatchableClassFileTransformer> transformerMap = new ConcurrentHashMap<String, MatchableClassFileTransformer>();

    private final ClassLoader loader;
    private final ClassFileTransformerDispatcher dispatcher;

    public InstrumentTranslator(ClassLoader loader, ClassFileTransformerDispatcher defaultTransformer) {
        if (defaultTransformer == null) {
            throw new NullPointerException("dispatcher must not be null");
        }
        this.loader = loader;
        this.dispatcher = defaultTransformer;
    }

    public void addTransformer(MatchableClassFileTransformer transformer) {
        // TODO extract matcher process
        final Matcher matcher = transformer.getMatcher();
        if (matcher instanceof ClassNameMatcher) {
            ClassNameMatcher classNameMatcher = (ClassNameMatcher) matcher;
            String className = classNameMatcher.getClassName();
            addTransformer0(transformer, className);
        } else if(matcher instanceof MultiClassNameMatcher) {
            final MultiClassNameMatcher classNameMatcher = (MultiClassNameMatcher)matcher;
            List<String> classNameList = classNameMatcher.getClassNames();
            for (String className : classNameList) {
                addTransformer0(transformer, className);
            }
        } else {
            throw new IllegalArgumentException("unsupported Matcher " + matcher);
        }

    }

    private void addTransformer0(MatchableClassFileTransformer transformer, String className) {
        final String checkJvmClassName = JavaAssistUtils.javaNameToJvmName(className);
        MatchableClassFileTransformer old = transformerMap.put(checkJvmClassName, transformer);
        if (old != null) {
            throw new IllegalStateException("Modifier already exist new:" + transformer.getClass() + " old:" + old.getMatcher());
        }
    }

    @Override
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
//        this.pool = pool;
    }

    @Override
    public void onLoad(ClassPool pool, String javaClassName) throws NotFoundException, CannotCompileException {
        logger.debug("loading className:{}", javaClassName);

        final String jvmClassName = JavaAssistUtils.javaNameToJvmName(javaClassName);
        try {
            // Find Modifier from agent and try transforming
            byte[] transform = dispatcher.transform(this.loader, jvmClassName, null, null, null);
            if (transform != null) {
                makeClass(pool, transform, jvmClassName);
                return;
            }
        } catch (IllegalClassFormatException ex) {
            throw new RuntimeException(jvmClassName + " not found. Caused:" + ex.getMessage(), ex);
        }
        
         // find from transformerMap
        onLoadTestModifier(pool, jvmClassName);

    }

    private void onLoadTestModifier(ClassPool pool, String jvmClassName) throws NotFoundException, CannotCompileException {
        logger.info("Modify find classname:{}, loader:{}", jvmClassName, loader);
        MatchableClassFileTransformer transformer = transformerMap.get(jvmClassName);
        if (transformer == null) {
            return;
        }
        logger.info("Modify jvmClassName:{},  modifier{}, loader:{}", jvmClassName, transformer, loader);


        final Thread thread = Thread.currentThread();
        final ClassLoader beforeClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        try {
            String javaClassName = JavaAssistUtils.jvmNameToJavaName(jvmClassName);
            byte[] modify = transformer.transform(loader, javaClassName, null, null, null);
            makeClass(pool, modify, jvmClassName);
        } catch (IllegalClassFormatException e) {
            throw new CannotCompileException(e);
        } finally {
            thread.setContextClassLoader(beforeClassLoader);
        }
    }

    private void makeClass(ClassPool pool, byte[] transform, String jvmClassName) {
        try {
            pool.makeClass(new ByteArrayInputStream(transform));
        } catch (IOException ex) {
            throw new RuntimeException("Class make fail. jvmClass:" + jvmClassName + " Caused by:" + ex.getMessage(), ex);
        }
    }
}
