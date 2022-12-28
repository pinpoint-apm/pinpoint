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

import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Objects;


/**
 * @author Woonduk Kang(emeroad)
 */
public class LambdaFactoryClassAdaptor {
    private static final String LAMBDA_FACTORY_CLASS_NAME = "java/lang/invoke/InnerClassLambdaMetafactory";

    private final Logger logger = LogManager.getLogger(this.getClass());

    public LambdaFactoryClassAdaptor() {
    }

    public byte[] loadTransformedBytecode(byte[] bytes) {
        final LambdaClass lambdaClass = getLambdaClass();
        return transform(bytes, lambdaClass);
    }

    private LambdaClass getLambdaClass() {
        if (JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_16)) {
            return new LambdaClassJava16();
        } else if (JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_15)) {
            return new LambdaClassJava15();
        } else if (JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_9)) {
            return new LambdaClassJava9();
        } else {
            return new LambdaClassJava8();
        }
    }

    public byte[] transform(byte[] bytes, LambdaClass lambdaClass) {
        Objects.requireNonNull(bytes, "bytes");

        final ClassReader reader = new ClassReader(bytes);
        final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        final List<MethodInsn> methodInsnList = lambdaClass.getMethodInsnList();
        final MethodInstReplacer methodInstReplacer = new MethodInstReplacer(writer, methodInsnList);
        reader.accept(methodInstReplacer, 0);

        if (!LAMBDA_FACTORY_CLASS_NAME.equals(methodInstReplacer.getClassName())) {
            throw new IllegalArgumentException("unexpected class " + methodInstReplacer.getClassName());
        }

        if (methodInstReplacer.getTransformCount() != 1) {
            logger.warn("unexpected {} invoke count {}", methodInsnList, methodInstReplacer.getTransformCount());
            // dump bytecode
        }
        return writer.toByteArray();
    }
}
