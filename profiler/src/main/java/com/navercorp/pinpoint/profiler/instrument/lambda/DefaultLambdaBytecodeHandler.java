/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.lambda;

import com.navercorp.pinpoint.bootstrap.instrument.lambda.LambdaBytecodeHandler;
import com.navercorp.pinpoint.bootstrap.module.ClassFileTransformModuleAdaptor;
import com.navercorp.pinpoint.bootstrap.module.JavaModuleFactory;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;

import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultLambdaBytecodeHandler implements LambdaBytecodeHandler {

    private final JavaModuleFactory javaModuleFactory;
    private final ClassFileTransformModuleAdaptor classFileTransformer;

    public DefaultLambdaBytecodeHandler(ClassFileTransformModuleAdaptor classFileTransformer, JavaModuleFactory javaModuleFactory) {
        this.classFileTransformer = Objects.requireNonNull(classFileTransformer, "classFileTransformer");
        this.javaModuleFactory = Objects.requireNonNull(javaModuleFactory, "javaModuleFactory");
    }

    @Override
    public byte[] handleLambdaBytecode(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        if (hostClass != null && data != null) {
            try {
                final ClassLoader classLoader = hostClass.getClassLoader();
                final Object module = javaModuleFactory.getModule(hostClass);
                final ProtectionDomain protectionDomain = hostClass.getProtectionDomain();
                final byte[] transform = classFileTransformer.transform(module, classLoader, null, null, protectionDomain, data);
                if (transform != null) {
                    return transform;
                }
            } catch (Exception e) {
                LogManager.getLogger(this.getClass()).warn("lambda transform fail Caused by:" + e.getMessage(), e);
            }
        }
        return data;
    }
}