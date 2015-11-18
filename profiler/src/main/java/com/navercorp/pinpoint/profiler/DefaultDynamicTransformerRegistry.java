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

import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultDynamicTransformerRegistry implements DynamicTransformerRegistry {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentMap<TransformerKey, ClassFileTransformer> transformerMap = new ConcurrentHashMap<TransformerKey, ClassFileTransformer>();

    @Override
    public void onRetransformRequest(Class<?> target, final ClassFileTransformer transformer) {
        add(target.getClassLoader(), target.getName(), transformer);
        if (logger.isInfoEnabled()) {
            logger.info("added retransformer classLoader: {}, class: {}, registry size: {}", target.getClassLoader(), target.getName(), transformerMap.size());
        }
    }
    
    @Override
    public void onTransformRequest(ClassLoader classLoader, String targetClassName, ClassFileTransformer transformer) {
        // TODO fix classLoader null case
//        if (classLoader== null) {
//            boot? ext? system?
//            classLoader = ClassLoader.getSystemClassLoader();
//        }
        add(classLoader, targetClassName, transformer);

        if (logger.isInfoEnabled()) {
            logger.info("added dynamic transformer classLoader: {}, className: {}, registry size: {}", classLoader, targetClassName, transformerMap.size());
        }
    }

    private void add(ClassLoader classLoader, String targetClassName, ClassFileTransformer transformer) {
        final String jvmName = JavaAssistUtils.javaNameToJvmName(targetClassName);

        final TransformerKey key = new TransformerKey(classLoader, jvmName);
        final ClassFileTransformer prev = transformerMap.putIfAbsent(key, transformer);
        
        if (prev != null) {
            throw new ProfilerException("Transformer already exists. classLoader: " + classLoader + ", target: " + targetClassName + ", transformer: " + prev);
        }
    }
    
    @Override
    public ClassFileTransformer getTransformer(ClassLoader classLoader, String targetClassName) {
        // TODO fix classLoader null case
        if (transformerMap.isEmpty()) {
            return null;
        }

        final TransformerKey key = new TransformerKey(classLoader, targetClassName);
        final ClassFileTransformer transformer = transformerMap.remove(key);
        
        if (logger.isDebugEnabled()) {
            logger.info("removed dynamic transformer classLoader: {}, className: {}, registry size: {}", classLoader, targetClassName, transformerMap.size());
        }
        
        return transformer;
    }
    
    private static final class TransformerKey {
        // TODO depends classLoader memory leak
        private final ClassLoader classLoader;
        private final String targetClassName;
        
        public TransformerKey(ClassLoader classLoader, String targetClassName) {
            if (targetClassName == null) {
                throw new NullPointerException("targetClassName must not be null");
            }
            this.classLoader = classLoader;
            this.targetClassName = targetClassName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TransformerKey that = (TransformerKey) o;

            if (classLoader != null ? !classLoader.equals(that.classLoader) : that.classLoader != null) return false;
            return targetClassName.equals(that.targetClassName);

        }

        @Override
        public int hashCode() {
            int result = classLoader != null ? classLoader.hashCode() : 0;
            result = 31 * result + targetClassName.hashCode();
            return result;
        }
    }
}
