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
import com.navercorp.pinpoint.profiler.instrument.transformer.InnerClassLambdaMetafactoryTransformer;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LambdaTransformBootloader {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    public void transformLambdaFactory(Instrumentation instrumentation, final ClassFileTransformModuleAdaptor classFileTransformer, final JavaModuleFactory javaModuleFactory)  {
        logger.info("LambdaTransformBootloader.transformLambdaFactory");
        retransform(instrumentation);

        registerLambdaBytecodeHandler(classFileTransformer, javaModuleFactory);

    }

    private void retransform(Instrumentation instrumentation) {
        final String lambdaMetaFactoryName = "java.lang.invoke.InnerClassLambdaMetafactory";
        try {
            final Class<?> lamdbaFactoryClazz = Class.forName(lambdaMetaFactoryName, false, null);
            logger.info("retransformClasses:{}", lamdbaFactoryClazz);
            final ClassFileTransformer classFileTransfomrer = new InnerClassLambdaMetafactoryTransformer();
            instrumentation.addTransformer(classFileTransfomrer, true);
            try {
                instrumentation.retransformClasses(lamdbaFactoryClazz);
            } finally {
                instrumentation.removeTransformer(classFileTransfomrer);
            }
        } catch (Exception e) {
            logger.warn("retransform fail class:{}", lambdaMetaFactoryName, e);
        }
    }

    private void registerLambdaBytecodeHandler(final ClassFileTransformModuleAdaptor classFileTransformer, final JavaModuleFactory javaModuleFactory)  {
        try {
            logger.info("register LambdaBytecodeHandler");
            // for java9 handler
            final LambdaBytecodeHandler lambdaBytecodeHandler = newLambdaBytecodeHandler(classFileTransformer, javaModuleFactory);

            final Class<?> unsafeDelegator = getUnsafeDelegator();
            final Method register = unsafeDelegator.getMethod("register", LambdaBytecodeHandler.class);
            final boolean success = (Boolean) register.invoke(unsafeDelegator, lambdaBytecodeHandler);
            if (success) {
                logger.info("LambdaBytecodeHandler registration success");
            } else {
                logger.warn("LambdaBytecodeHandler registration fail");
            }
        } catch (Exception e) {
            logger.error("LambdaBytecodeHandler registration fail Caused by:" + e.getMessage(), e);
        }
    }

    private LambdaBytecodeHandler newLambdaBytecodeHandler(ClassFileTransformModuleAdaptor classFileTransformer, JavaModuleFactory javaModuleFactory) {
        final LambdaBytecodeHandler lambdaBytecodeHandler = new DefaultLambdaBytecodeHandler(classFileTransformer, javaModuleFactory);
        if (LoggerFactory.getLogger(lambdaBytecodeHandler.getClass()).isDebugEnabled()) {
            return new LambdaBytecodeLogger(lambdaBytecodeHandler);
        }
        return lambdaBytecodeHandler;
    }

    private Class<?> getUnsafeDelegator() throws ClassNotFoundException {
        String delegatorName = getDelegatorName();
        return Class.forName(delegatorName, false, null);
    }

    private String getDelegatorName() {
        if (JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_9)) {
            return JavaAssistUtils.jvmNameToJavaName(LambdaClassJava9.DELEGATE_CLASS);
        } else {
            return JavaAssistUtils.jvmNameToJavaName(LambdaClassJava8.DELEGATE_CLASS);
        }
    }
}
