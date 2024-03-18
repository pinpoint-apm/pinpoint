/*
 * Copyright 2023 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.context.javamodule;

import com.navercorp.pinpoint.bootstrap.module.JavaModule;
import com.navercorp.pinpoint.bootstrap.module.JavaModuleFactory;
import com.navercorp.pinpoint.common.util.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author youngjin.kim2
 */
@ExtendWith(MockitoExtension.class)
public class ClassFileTransformerModuleHandlerTest {

    @Mock
    Instrumentation instrumentation;

    @Mock
    ClassFileTransformer classFileTransformer;

    @Mock
    JavaModuleFactory javaModuleFactory;

    @Mock
    JavaModule transformedModule;

    private static final Object transformedModuleObject = new Object();
    private static final ClassLoader classLoader = ClassFileTransformerModuleHandler.class.getClassLoader();
    private static final String className = ClassFileTransformerModuleHandler.class.getName();
    private static final Class<?> classBeingRedefined = ClassFileTransformerModuleHandler.class;
    private static final ProtectionDomain protectionDomain = Mockito.mock(ProtectionDomain.class);

    @Test
    public void shouldDoNothingWhenTransformedModuleObjectIsNull() throws IllegalClassFormatException, IOException {
        byte[] originClassBuffer = readClassBuffer(ClassFileTransformerModuleHandler.class);
        byte[] transformedClassBuffer = readClassBuffer(ClassFileTransformerModuleHandlerTest.class);

        when(classFileTransformer.transform(any(), anyString(), any(), any(), any()))
                .thenReturn(transformedClassBuffer);
        byte[] result = buildHandler().transform(
                null,
                classLoader,
                className,
                classBeingRedefined,
                protectionDomain,
                originClassBuffer
        );
        assertThat(result).isEqualTo(transformedClassBuffer);
    }

    @Test
    public void shouldEnsureClassName() throws IllegalClassFormatException, IOException {
        byte[] originClassBuffer = readClassBuffer(ClassFileTransformerModuleHandler.class);
        byte[] transformedClassBuffer = readClassBuffer(ClassFileTransformerModuleHandlerTest.class);

        when(classFileTransformer.transform(any(), nullable(String.class), any(), any(), any()))
                .thenReturn(transformedClassBuffer);
        when(javaModuleFactory.isNamedModule(any())).thenReturn(true);
        when(javaModuleFactory.wrapFromModule(same(transformedModuleObject))).thenReturn(transformedModule);
        when(transformedModule.canRead((JavaModule) any())).thenReturn(false);
        byte[] result = buildHandler().transform(
                transformedModuleObject,
                classLoader,
                null,
                classBeingRedefined,
                protectionDomain,
                originClassBuffer
        );
        verify(transformedModule, atLeast(1)).addReads(any());
        assertThat(result).isEqualTo(transformedClassBuffer);
    }

    private static byte[] readClassBuffer(Class<?> clazz) throws IOException {
        String path = clazz.getName().replace('.', '/') + ".class";
        InputStream inputStream = clazz.getClassLoader().getResourceAsStream(path);
        return IOUtils.toByteArray(inputStream);
    }

    private ClassFileTransformerModuleHandler buildHandler() {
        return new ClassFileTransformerModuleHandler(instrumentation, classFileTransformer, javaModuleFactory);
    }

}
