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
package com.navercorp.pinpoint.profiler.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import java.util.Objects;
import com.navercorp.pinpoint.profiler.instrument.transformer.LambdaClassFileResolver;
import com.navercorp.pinpoint.profiler.instrument.transformer.TransformerRegistry;


/**
 * @author emeroad
 * @author netspider
 * @author jaehong.kim
 */
public class DefaultClassFileTransformerDispatcher implements ClassFileTransformerDispatcher {

    private final BaseClassFileTransformer baseClassFileTransformer;
    private final TransformerRegistry transformerRegistry;
    private final DynamicTransformerRegistry dynamicTransformerRegistry;

    private final ClassFileFilter classLoaderFilter;
    private final ClassFileFilter pinpointClassFilter;
    private final ClassFileFilter unmodifiableFilter;

    private final LambdaClassFileResolver lambdaClassFileResolver;

    public DefaultClassFileTransformerDispatcher(ClassFileFilter unmodifiableFilter, TransformerRegistry transformerRegistry,
                                                 DynamicTransformerRegistry dynamicTransformerRegistry, LambdaClassFileResolver lambdaClassFileResolver) {

        this.baseClassFileTransformer = new BaseClassFileTransformer(this.getClass().getClassLoader());

        this.classLoaderFilter = new PinpointClassLoaderFilter(this.getClass().getClassLoader());
        this.pinpointClassFilter = new PinpointClassFilter();
        this.unmodifiableFilter = Objects.requireNonNull(unmodifiableFilter, "unmodifiableFilter");

        this.transformerRegistry = Objects.requireNonNull(transformerRegistry, "transformerRegistry");
        this.dynamicTransformerRegistry = Objects.requireNonNull(dynamicTransformerRegistry, "dynamicTransformerRegistry");
        this.lambdaClassFileResolver = Objects.requireNonNull(lambdaClassFileResolver, "lambdaClassFileResolver");
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String classInternalName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) throws IllegalClassFormatException {
        if (!classLoaderFilter.accept(classLoader, classInternalName, classBeingRedefined, protectionDomain, classFileBuffer)) {
            return null;
        }

        final String internalName = lambdaClassFileResolver.resolve(classLoader, classInternalName, protectionDomain, classFileBuffer);
        if (internalName == null) {
            return null;
        }

        if (!pinpointClassFilter.accept(classLoader, internalName, classBeingRedefined, protectionDomain, classFileBuffer)) {
            return null;
        }

        final ClassFileTransformer dynamicTransformer = dynamicTransformerRegistry.getTransformer(classLoader, internalName);
        if (dynamicTransformer != null) {
            return baseClassFileTransformer.transform(classLoader, internalName, classBeingRedefined, protectionDomain, classFileBuffer, dynamicTransformer);
        }

        if (!unmodifiableFilter.accept(classLoader, internalName, classBeingRedefined, protectionDomain, classFileBuffer)) {
            return null;
        }

        final ClassFileTransformer transformer = this.transformerRegistry.findTransformer(classLoader, internalName, classFileBuffer);
        if (transformer == null) {
            return null;
        }

        return baseClassFileTransformer.transform(classLoader, internalName, classBeingRedefined, protectionDomain, classFileBuffer, transformer);
    }

}