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

import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.profiler.instrument.ASMBytecodeDisassembler;
import com.navercorp.pinpoint.profiler.instrument.ASMVersion;
import com.navercorp.pinpoint.profiler.instrument.classloading.DefineClassUtils;
import com.navercorp.pinpoint.profiler.instrument.lambda.mock.DefineAnonymousClassDelegator;
import com.navercorp.pinpoint.profiler.instrument.lambda.mock.UnsafeClassMock;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LambdaFactoryTest {

    private static final String lambdaMetaFactory = "java.lang.invoke.InnerClassLambdaMetafactory";
    private static final String lambdaMetaFactoryResourceName = JavaAssistUtils.javaClassNameToJvmResourceName(lambdaMetaFactory);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final ASMBytecodeDisassembler disassembler = new ASMBytecodeDisassembler();

    @Test
    public void dumpInnerClassLamdbaMetaFactory() throws IOException {
        InputStream resourceStream = ClassLoader.getSystemResourceAsStream(lambdaMetaFactoryResourceName);
        byte[] bytes = IOUtils.toByteArray(resourceStream);
        logger.debug("dump-------");
        ByteCodeDumper.dumpByteCode(bytes);
    }



    @Test
    public void transformTest() throws IOException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {

        ClassLoader classLoader = this.getClass().getClassLoader();
        String name = JavaAssistUtils.javaClassNameToJvmResourceName(UnsafeClassMock.class.getName());
        InputStream resourceStream = ClassLoader.getSystemResourceAsStream(name);
        byte[] bytes = IOUtils.toByteArray(resourceStream);
        logger.info("dump-------");
        ByteCodeDumper.dumpByteCode(bytes);

        ClassReader reader = new ClassReader(bytes, 0, bytes.length);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        String delegateClassName = "com/navercorp/pinpoint/profiler/instrument/lambda/mock/DefineAnonymousClassDelegator";
        String delegateMethod = "delegate";
        MethodInstReplacer replacer = new MethodInstReplacer(writer, "test",
                "com/navercorp/pinpoint/profiler/instrument/lambda/mock/UnsafeMock", "defineAnonymousClass",
                delegateClassName, delegateMethod
        );

        renameClass(reader, replacer);

        byte[] bytes1 = writer.toByteArray();
        ClassReader newReader1 = new ClassReader(bytes1);
        ClassNode node = new ClassNode(ASMVersion.VERSION);
        newReader1.accept(node, 0);

        logger.debug("dump-------");
        ByteCodeDumper.dumpByteCode(bytes1);

        Class<?> delegatorClazz = DefineClassUtils.defineClass(classLoader, JavaAssistUtils.jvmNameToJavaName(node.name), bytes1);
        logger.debug("class:{}", delegatorClazz);
        Object o = delegatorClazz.getDeclaredConstructor().newInstance();
        Method test = o.getClass().getMethod("test");
        Object invoke = test.invoke(o);

        Assert.assertEquals(DefineAnonymousClassDelegator.count, 1);


    }

    private void renameClass(ClassReader reader, ClassVisitor classVisitor) {
        String className = "com/navercorp/pinpoint/profiler/instrument/lambda/mock/UnsafeClassMock";
        Remapper remapper = new SimpleRemapper(className,className + "2");
        ClassRemapper classRemapper = new ClassRemapper(classVisitor, remapper);
        reader.accept(classRemapper, 0);
    }

}
