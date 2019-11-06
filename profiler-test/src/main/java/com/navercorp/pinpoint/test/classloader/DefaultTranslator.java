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

import com.navercorp.pinpoint.bootstrap.instrument.matcher.ClassNameMatcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.MultiClassNameMatcher;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import com.navercorp.pinpoint.test.util.BytecodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTranslator implements Translator {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentMap<String, MatchableClassFileTransformer> transformerMap = new ConcurrentHashMap<String, MatchableClassFileTransformer>();

    private final ClassLoader loader;
    private final ClassFileTransformer dispatcher;

    public DefaultTranslator(ClassLoader loader, ClassFileTransformer defaultTransformer) {
        if (defaultTransformer == null) {
            throw new NullPointerException("dispatcher");
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
    public byte[] transform(String className) {
        logger.debug("loading className:{}", className);

        final String classInternalName = JavaAssistUtils.javaNameToJvmName(className);
        try {
            // Find Modifier from agent and try transforming
            final byte[] transformBytes = dispatcher.transform(this.loader, classInternalName, null, null, null);
            if (transformBytes != null) {
                return transformBytes;
            }

            final byte[] customTransformBytes = customTransformer(classInternalName);
            if (customTransformBytes != null) {
                return customTransformBytes;
            }

            final byte[] classFile = BytecodeUtils.getClassFile(this.loader, className);
            if (classFile == null) {
                throw new ClassNotFoundException(className + " not found");
            }
            return classFile;
        } catch (Throwable th) {
            throw new RuntimeException(className + " transform fail" , th);
        }
    }

    private byte[] customTransformer(String jvmClassName) {
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

            return transformBytes;
        } catch (IllegalClassFormatException e) {
            throw new RuntimeException(jvmClassName + " transform fail" , e);
        } finally {
            thread.setContextClassLoader(beforeClassLoader);
        }
    }


}
