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

package com.navercorp.pinpoint.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.google.inject.Inject;
import com.navercorp.pinpoint.bootstrap.instrument.RequestHandle;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultDynamicTransformerRegistry implements DynamicTransformerRegistry {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentMap<TransformerKey, ClassFileTransformer> transformerMap = new ConcurrentHashMap<TransformerKey, ClassFileTransformer>();

    @Inject
    public DefaultDynamicTransformerRegistry() {
    }

    @Override
    public RequestHandle onRetransformRequest(Class<?> target, final ClassFileTransformer transformer) {
        if (target == null) {
            throw new NullPointerException("target");
        }
        if (transformer == null) {
            throw new NullPointerException("transformer");
        }

        final TransformerKey key = createTransformKey(target);
        add(key, transformer);
        if (logger.isInfoEnabled()) {
            logger.info("added retransformer classLoader: {}, class: {}, registry size: {}", target.getClassLoader(), target.getName(), transformerMap.size());
        }
        return new DefaultRequestHandle(key);
    }



    @Override
    public void onTransformRequest(ClassLoader classLoader, String targetClassName, ClassFileTransformer transformer) {


        final TransformerKey transformKey = createTransformKey(classLoader, targetClassName);
        add(transformKey, transformer);

        if (logger.isInfoEnabled()) {
            logger.info("added dynamic transformer classLoader: {}, className: {}, registry size: {}", classLoader, targetClassName, transformerMap.size());
        }
    }

    private void add(TransformerKey key, ClassFileTransformer transformer) {
        final ClassFileTransformer prev = transformerMap.putIfAbsent(key, transformer);
        
        if (prev != null) {
            throw new ProfilerException("Transformer already exists. TransformKey: " + key + ", transformer: " + prev);
        }
    }

    private TransformerKey createTransformKey(ClassLoader classLoader, String targetClassName) {
        final String classInternName = JavaAssistUtils.javaNameToJvmName(targetClassName);
        return new TransformerKey(classLoader, classInternName);
    }

    private TransformerKey createTransformKey(Class<?> targetClass) {

        final ClassLoader classLoader = targetClass.getClassLoader();
        final String targetClassName = targetClass.getName();

        return createTransformKey(classLoader, targetClassName);
    }

    @Override
    public ClassFileTransformer getTransformer(ClassLoader classLoader, String targetClassName) {
        // TODO fix classLoader null case
        if (transformerMap.isEmpty()) {
            return null;
        }

        final TransformerKey key = new TransformerKey(classLoader, targetClassName);
        final ClassFileTransformer transformer = transformerMap.remove(key);
        if (transformer != null) {
            if (logger.isInfoEnabled()) {
                logger.info("removed dynamic transformer classLoader: {}, className: {}, registry size: {}", classLoader, targetClassName, transformerMap.size());
            }
        }
        
        return transformer;
    }

    int size() {
        return transformerMap.size();
    }
    
    private static final class TransformerKey {
        // TODO defense classLoader memory leak
        private final ClassLoader classLoader;
        private final String targetClassInternalName;
        
        public TransformerKey(ClassLoader classLoader, String targetClassInternalName) {
            this.classLoader = classLoader;
            this.targetClassInternalName = Assert.requireNonNull(targetClassInternalName, "targetClassInternalName");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TransformerKey that = (TransformerKey) o;

            if (classLoader != null ? !classLoader.equals(that.classLoader) : that.classLoader != null) return false;
            return targetClassInternalName.equals(that.targetClassInternalName);

        }

        @Override
        public int hashCode() {
            int result = classLoader != null ? classLoader.hashCode() : 0;
            result = 31 * result + targetClassInternalName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "TransformerKey{" +
                    "classLoader=" + classLoader +
                    ", targetClassInternalName='" + targetClassInternalName + '\'' +
                    '}';
        }
    }

    private class DefaultRequestHandle implements RequestHandle {
        private final TransformerKey key;

        public DefaultRequestHandle(TransformerKey key) {
            if (key == null) {
                throw new NullPointerException("key");
            }
            this.key = key;
        }

        @Override
        public boolean cancel() {
            final ClassFileTransformer remove = transformerMap.remove(key);
            if (remove == null) {
                return false;
            }
            return true;
        }
    }

}
