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
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import javassist.*;

import javassist.bytecode.stackmap.TypeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class InstrumentTranslator implements Translator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private ConcurrentMap<String, AbstractModifier> modifierMap = new ConcurrentHashMap<String, AbstractModifier>();

    private ClassLoader loader;
    private final ClassFileTransformer classFileTransformer;

    public InstrumentTranslator(ClassLoader loader, ClassFileTransformer classFileTransformer) {
        if (classFileTransformer == null) {
            throw new NullPointerException("classFileTransformer must not be null");
        }
        this.loader = loader;
        this.classFileTransformer = classFileTransformer;
    }

    public AbstractModifier addModifier(AbstractModifier modifier) {
        final Matcher matcher = modifier.getMatcher();
        if (matcher instanceof ClassNameMatcher) {
            ClassNameMatcher classNameMatcher = (ClassNameMatcher) matcher;
            final String javaName = JavaAssistUtils.jvmNameToJavaName(classNameMatcher.getClassName());
            return modifierMap.put(javaName, modifier);
        }
        throw new IllegalArgumentException("unsupported Matcher " + matcher);

    }

    @Override
    public void start(ClassPool pool) throws NotFoundException, CannotCompileException {
//        this.pool = pool;
    }

    @Override
    public void onLoad(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
        logger.debug("loading className:{}", classname);

        try {
            // Find Modifier from agent and try transforming
            final String jvmName = JavaAssistUtils.javaNameToJvmName(classname);
            byte[] transform = classFileTransformer.transform(this.loader, jvmName, null, null, null);
            if (transform != null) {
                pool.makeClass(new ByteArrayInputStream(transform));
                return;
            }
        } catch (IOException ex) {
            throw new NotFoundException(classname + " not found. Caused:" + ex.getMessage(), ex);
        } catch (IllegalClassFormatException ex) {
            throw new RuntimeException(classname + " not found. Caused:" + ex.getMessage(), ex);
        }
        
         // find from modifierMap
        findModifierMap(pool, classname);


    }
    private void findModifierMap(ClassPool pool, String classname) throws NotFoundException, CannotCompileException {
        AbstractModifier modifier = modifierMap.get(classname);
        if (modifier == null) {
            return;
        }
        logger.info("Modify loader:{}, name:{},  modifier{}", loader, classname, modifier);

        final Thread thread = Thread.currentThread();
        final ClassLoader beforeClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        try {
            byte[] modify = modifier.modify(this.loader, classname, null, null);
            pool.makeClass(new ByteArrayInputStream(modify));
        } catch (IOException ex) {
            throw new NotFoundException(classname + " not found. Caused:" + ex.getMessage(), ex);
        } finally {
            thread.setContextClassLoader(beforeClassLoader);
        }
    }
}
