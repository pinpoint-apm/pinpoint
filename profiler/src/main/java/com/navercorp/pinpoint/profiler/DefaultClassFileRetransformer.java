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
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.util.Maps;


public class DefaultClassFileRetransformer implements DynamicTrnasformerRegistry {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ConcurrentMap<TransformerKey, ClassFileTransformer> transformerMap = Maps.newWeakConcurrentMap();

    @Override
    public void onRetransformRequest(Class<?> target, final ClassFileTransformer transformer) {
        if (logger.isInfoEnabled()) {
            logger.info("addRetransformEvent class:{}", target.getName());
        }

        add(target.getClassLoader(), target.getName(), transformer);
    }
    
    @Override
    public void onTransformRequest(ClassLoader classLoader, String targetClassName, ClassFileTransformer transformer) {
        if (logger.isInfoEnabled()) {
            logger.info("addDynamicTransformer classLoader:{] className:{}", classLoader, targetClassName);
        }

        add(classLoader, targetClassName, transformer);
    }

    private void add(ClassLoader classLoader, String targetClassName, ClassFileTransformer transformer) {
        ClassFileTransformer prev = transformerMap.putIfAbsent(new TransformerKey(classLoader, targetClassName.replace('.', '/')), transformer);
        
        if (prev != null) {
            throw new ProfilerException("Transformer already exists. classLoader: " + classLoader + ", target: " + targetClassName + ", transformer: " + prev);
        }
    }
    
    @Override
    public ClassFileTransformer getTransformer(ClassLoader classLoader, String targetClassName) {
        if (transformerMap.isEmpty()) {
            return null;
        }
        
        return transformerMap.remove(new TransformerKey(classLoader, targetClassName));
    }
    
    private static final class TransformerKey {
        private final ClassLoader classLoader;
        private final String targetClassName;
        
        public TransformerKey(ClassLoader classLoader, String targetClassName) {
            this.classLoader = classLoader;
            this.targetClassName = targetClassName;
        }

        @Override
        public int hashCode() {
            return classLoader.hashCode() * 31 + targetClassName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            TransformerKey other = (TransformerKey) obj;
            return this.classLoader.equals(other.classLoader) && this.targetClassName.equals(other.targetClassName);
        }
    }
}
