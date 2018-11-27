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
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Arrays;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LambdaTransformBootloader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

    public void transformLambdaFactory(Instrumentation instrumentation, final ClassFileTransformModuleAdaptor classFileTransformer, final JavaModuleFactory javaModuleFactory)  {
        logger.info("LambdaTransformBootloader.transformLambdaFactory");
        try {
            redefineLambdaClass(instrumentation);
        } catch (Exception e) {
            logger.warn("Lambda transform fail Caused by:" + e.getMessage(), e);
        }

        try {
            logger.info("Add LambdaBytecodeHandler");
            final boolean debugEnabled = logger.isDebugEnabled();
            // for java9 handler
            LambdaBytecodeHandler lambdaBytecodeHandler = new LambdaBytecodeHandler() {
                @Override
                public byte[] handleLambdaBytecode(Class<?> hostClass, byte[] data, Object[] cpPatches) {
                    if (debugEnabled) {
                        logger.debug("handleLambdaBytecode hostClass:{} cpPatches:{}", hostClass, Arrays.toString(cpPatches));
                    }
                    try {
                        final ClassLoader classLoader = hostClass.getClassLoader();
                        final Object module = javaModuleFactory.getModule(hostClass);
                        final ProtectionDomain protectionDomain = hostClass.getProtectionDomain();
                        final byte[] transform = classFileTransformer.transform(module, classLoader, null, null, protectionDomain, data);
                        if (transform != null) {
                            return transform;
                        }
                        return data;
                    } catch (IllegalClassFormatException e) {
                        return data;
                    }
                }
            };

            Class<?> unsafeDelegator = getUnsafeDelegator();
            Method register = unsafeDelegator.getMethod("register", LambdaBytecodeHandler.class);
            register.invoke(unsafeDelegator, lambdaBytecodeHandler);
        } catch (Exception e) {
            logger.warn("LambdaBytecodeHandler add fail Caused by:" + e.getMessage(), e);
        }
    }

    private void redefineLambdaClass(Instrumentation instrumentation) throws Exception {
        final Class<?> lamdbaFactoryClazz = systemClassLoader.loadClass("java.lang.invoke.InnerClassLambdaMetafactory");

        final Class<?> lambdaAdaptor = this.getClass().getClassLoader().loadClass("com.navercorp.pinpoint.profiler.instrument.lambda.LambdaFactoryClassAdaptor");
        final Object instance = lambdaAdaptor.getConstructor().newInstance();

        Method loadTransformedBytecodeMethod = lambdaAdaptor.getMethod("loadTransformedBytecode");
        byte[] transformBytecode = (byte[]) loadTransformedBytecodeMethod.invoke(instance);

        ClassDefinition classDefinition = new ClassDefinition(lamdbaFactoryClazz, transformBytecode);
        instrumentation.redefineClasses(classDefinition);
    }

    private Class<?> getUnsafeDelegator() throws ClassNotFoundException {
        String delegatorName = getDelegatorName();
        return systemClassLoader.loadClass(delegatorName);
    }

    private String getDelegatorName() {
        if (JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_9)) {
            return JavaAssistUtils.jvmNameToJavaName(LambdaClassJava9.DELEGATE_CLASS);
        } else {
            return JavaAssistUtils.jvmNameToJavaName(LambdaClassJava8.DELEGATE_CLASS);
        }
    }
}
