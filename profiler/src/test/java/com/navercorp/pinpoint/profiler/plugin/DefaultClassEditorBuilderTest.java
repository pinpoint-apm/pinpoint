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

package com.navercorp.pinpoint.profiler.plugin;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.ClassEditor;
import com.navercorp.pinpoint.bootstrap.plugin.editor.MethodEditorBuilder;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.plugin.editor.DefaultClassEditorBuilder;

public class DefaultClassEditorBuilderTest {
    public static final String SCOPE_NAME = "test";

    @Test
    public void test() throws Exception {
        ByteCodeInstrumentor instrumentor = mock(ByteCodeInstrumentor.class);
        TraceContext traceContext = mock(TraceContext.class);
        InstrumentClass aClass = mock(InstrumentClass.class);
        MethodInfo aMethod = mock(MethodInfo.class);
        MethodDescriptor aDescriptor = mock(MethodDescriptor.class);
        Scope aScope = mock(Scope.class);
        DefaultAgent agent = mock(DefaultAgent.class);
        
        ClassLoader classLoader = getClass().getClassLoader();
        String methodName = "someMethod";
        Class<?>[] parameterTypes = new Class<?>[] { String.class };
        String[] parameterTypeNames = TypeUtils.toClassNames(parameterTypes);
        
        when(agent.getByteCodeInstrumentor()).thenReturn(instrumentor);
        when(agent.getTraceContext()).thenReturn(traceContext);
        when(instrumentor.getScope(SCOPE_NAME)).thenReturn(aScope);
        when(aClass.getDeclaredMethod(methodName, parameterTypeNames)).thenReturn(aMethod);
        when(aMethod.getName()).thenReturn(methodName);
        when(aMethod.getParameterTypes()).thenReturn(parameterTypeNames);
        when(aMethod.getDescriptor()).thenReturn(aDescriptor);
        when(aClass.addInterceptor(eq(methodName), eq(parameterTypeNames), isA(Interceptor.class))).thenReturn(0);
        
        DefaultProfilerPluginContext context = new DefaultProfilerPluginContext(agent);
        DefaultClassEditorBuilder builder = new DefaultClassEditorBuilder(context, "TargetClass");
        builder.injectMetadata("a", "java.util.HashMap");
        builder.injectFieldAccessor("someField");
        
        MethodEditorBuilder ib = builder.editMethod(methodName, parameterTypeNames);
        ib.injectInterceptor("com.navercorp.pinpoint.profiler.plugin.TestInterceptor", "provided");
        
        ClassEditor editor = builder.build();
        
        editor.edit(classLoader, aClass);
        
        verify(aClass).addInterceptor(eq(methodName), isA(String[].class), isA(Interceptor.class));
        verify(aClass).addTraceValue(MetadataAccessor.get(0).getType(), "new java.util.HashMap();");
        verify(aClass).addGetter(FieldAccessor.get(0).getType(), "someField");
    }
}
