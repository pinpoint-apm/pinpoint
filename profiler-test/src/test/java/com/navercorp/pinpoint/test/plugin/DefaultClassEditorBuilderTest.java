/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.test.plugin;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.profiler.context.module.ApplicationContext;
import com.navercorp.pinpoint.profiler.util.TypeUtils;
import org.junit.Test;

import static com.navercorp.pinpoint.common.util.VarArgs.va;
import static org.mockito.Mockito.*;

public class DefaultClassEditorBuilderTest {
    public static final String SCOPE_NAME = "test";

    @Test
    public void test() throws Exception {
        InstrumentEngine instrumentEngine = mock(InstrumentEngine.class);
        TraceContext traceContext = mock(TraceContext.class);
        InstrumentClass aClass = mock(InstrumentClass.class);
        InstrumentMethod aMethod = mock(InstrumentMethod.class);
        MethodDescriptor aDescriptor = mock(MethodDescriptor.class);
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        InstrumentContext context = mock(InstrumentContext.class);
        
        ClassLoader classLoader = getClass().getClassLoader();
        String className = "someClass";
        String methodName = "someMethod";
        byte[] classFileBuffer = new byte[0];
        Class<?>[] parameterTypes = new Class<?>[] { String.class };
        String[] parameterTypeNames = TypeUtils.toClassNames(parameterTypes);
        
        when(applicationContext.getInstrumentEngine()).thenReturn(instrumentEngine);
        when(applicationContext.getTraceContext()).thenReturn(traceContext);
        when(instrumentEngine.getClass(context, classLoader, className, classFileBuffer)).thenReturn(aClass);
        when(aClass.getDeclaredMethod(methodName, parameterTypeNames)).thenReturn(aMethod);
        when(aMethod.getName()).thenReturn(methodName);
        when(aMethod.getParameterTypes()).thenReturn(parameterTypeNames);
        when(aMethod.getDescriptor()).thenReturn(aDescriptor);
        when(aClass.addInterceptor(eq(methodName), va(eq(parameterTypeNames)))).thenReturn(0);
        
        
//        DefaultClassFileTransformerBuilder builder = new DefaultClassFileTransformerBuilder(context, "TargetClass");
//        builder.injectField("some.accessor.Type", "java.util.HashMap");
//        builder.injectGetter("some.getter.Type", "someField");
//
//        MethodTransformerBuilder ib = builder.editMethod(methodName, parameterTypeNames);
//        ib.injectInterceptor("com.navercorp.pinpoint.profiler.plugin.TestInterceptor", "provided");
//
//        ClassFileTransformer transformer = builder.build();
//
//        transformer.transform(classLoader, className, null, null, classFileBuffer);
//
//        verify(aMethod).addScopedInterceptor(eq("com.navercorp.pinpoint.profiler.plugin.TestInterceptor"), eq(va("provided")), (InterceptorScope)isNull(), (ExecutionPolicy)isNull());
//        verify(aClass).addField("some.accessor.Type", "new java.util.HashMap();");
//        verify(aClass).addGetter("some.getter.Type", "someField");
    }
}
