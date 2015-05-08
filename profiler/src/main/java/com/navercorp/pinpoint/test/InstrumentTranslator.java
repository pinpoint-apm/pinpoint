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

import com.navercorp.pinpoint.bootstrap.instrument.matcher.ClassNameMatcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.MultiClassNameMatcher;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import javassist.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class InstrumentTranslator implements Translator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final ConcurrentMap<String, AbstractModifier> modifierMap = new ConcurrentHashMap<String, AbstractModifier>();

    private final ClassLoader loader;
    private final ClassFileTransformer classFileTransformer;

    public InstrumentTranslator(ClassLoader loader, ClassFileTransformer classFileTransformer) {
        if (classFileTransformer == null) {
            throw new NullPointerException("classFileTransformer must not be null");
        }
        this.loader = loader;
        this.classFileTransformer = classFileTransformer;
    }

    public void addModifier(AbstractModifier modifier) {
        // TODO extract matcher process
        final Matcher matcher = modifier.getMatcher();
        if (matcher instanceof ClassNameMatcher) {
            ClassNameMatcher classNameMatcher = (ClassNameMatcher) matcher;
            String className = classNameMatcher.getClassName();
            addModifier0(modifier, className);
        } else if(matcher instanceof MultiClassNameMatcher) {
            final MultiClassNameMatcher classNameMatcher = (MultiClassNameMatcher)matcher;
            List<String> classNameList = classNameMatcher.getClassNames();
            for (String className : classNameList) {
                addModifier0(modifier, className);
            }
        } else {
            throw new IllegalArgumentException("unsupported Matcher " + matcher);
        }

    }

    private void addModifier0(AbstractModifier modifier, String className) {
        final String checkJvmClassName = JavaAssistUtils.javaNameToJvmName(className);
        AbstractModifier old = modifierMap.put(checkJvmClassName, modifier);
        if (old != null) {
            throw new IllegalStateException("Modifier already exist new:" + modifier.getClass() + " old:" + old.getMatcher());
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
            byte[] transform = classFileTransformer.transform(this.loader, jvmClassName, null, null, null);
            if (transform != null) {
                makeClass(pool, transform, jvmClassName);
                return;
            }
        } catch (IllegalClassFormatException ex) {
            throw new RuntimeException(jvmClassName + " not found. Caused:" + ex.getMessage(), ex);
        }
        
         // find from modifierMap
        onLoadTestModifier(pool, jvmClassName);

    }

    private void onLoadTestModifier(ClassPool pool, String jvmClassName) throws NotFoundException, CannotCompileException {
        logger.info("Modify find classname:{}, loader:{}", jvmClassName, loader);
        AbstractModifier modifier = modifierMap.get(jvmClassName);
        if (modifier == null) {
            return;
        }
        logger.info("Modify jvmClassName:{},  modifier{}, loader:{}", jvmClassName, modifier, loader);


        final Thread thread = Thread.currentThread();
        final ClassLoader beforeClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        try {
            String javaClassName = JavaAssistUtils.jvmNameToJavaName(jvmClassName);
            byte[] modify = modifier.modify(this.loader, javaClassName, null, null);
            makeClass(pool, modify, jvmClassName);
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
