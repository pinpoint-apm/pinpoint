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

package com.navercorp.pinpoint.profiler.instrument.transformer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 */
public class InnerClassLambdaMetafactoryTransformer implements ClassFileTransformer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String InnerClassLambdaMetafactory = "java/lang/invoke/InnerClassLambdaMetafactory";

    private final Object lambdaFactoryClassAdaptor;
    private final Method transformMethod;

    public InnerClassLambdaMetafactoryTransformer() {
        try {
            final ClassLoader agentClassLoader = this.getClass().getClassLoader();
            final Class<?> lambdaAdaptor = agentClassLoader.loadClass("com.navercorp.pinpoint.profiler.instrument.lambda.LambdaFactoryClassAdaptor");
            final Constructor<?> constructor = lambdaAdaptor.getConstructor();
            this.lambdaFactoryClassAdaptor = constructor.newInstance();
            this.transformMethod = lambdaAdaptor.getMethod("loadTransformedBytecode", byte[].class);
        } catch (Exception e) {
            throw new IllegalStateException("LambdaFactoryClassAdaptor initialize fail Caused by:" + e.getMessage(), e);
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!InnerClassLambdaMetafactory.equals(className)) {
            return null;
        }
        try {
            logger.debug("transform InnerClassLambdaMetafactory");
            final byte[] transformBytecode = (byte[]) transformMethod.invoke(lambdaFactoryClassAdaptor, new Object[]{classfileBuffer});
            return transformBytecode;
        } catch (Exception e) {
            logger.warn("InnerClassLambdaMetafactory transform fail Caused by:" + e.getMessage(), e);
            return null;
        }
    }
}
