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
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import java.util.concurrent.ConcurrentMap;

import com.navercorp.pinpoint.profiler.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultClassFileRetransformer implements ClassFileRetransformer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

//    private final ConcurrentMap<Class<?>, ClassFileTransformer> transformerMap = new ConcurrentHashMap<Class<?>, ClassFileTransformer>();
    private final ConcurrentMap<Class<?>, ClassFileTransformer> transformerMap = Maps.newWeakConcurrentMap();

    public DefaultClassFileRetransformer() {
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classBeingRedefined == null) {
            return null;
        }
        logger.info("retransform className:{} cl:{} ", loader, className);

        final ClassFileTransformer transformer = transformerMap.remove(classBeingRedefined);
        if (transformer != null) {
            try {
                return transformer.transform(loader, classBeingRedefined.getName(), classBeingRedefined, protectionDomain, classfileBuffer);
            } catch (Throwable t) {
                logger.warn("Failed to retransform {} with {}", className, transformer);
                return null;
            }
        }

        logger.warn("Unexpected retransform request for className:{} cl:{}", className, loader);
        return null;
    }

    @Override
    public void addRetransformEvent(Class<?> target, final ClassFileTransformer transformer) {

        if (logger.isInfoEnabled()) {
            logger.info("addRetransformEvent class:{}", target.getName());
        }

        final ClassFileTransformer prev = transformerMap.putIfAbsent(target, transformer);
        if (prev != null) {
            throw new ProfilerException("Retransform already requested. target: " + target + ", transformer: " + prev);
        }
    }
}
