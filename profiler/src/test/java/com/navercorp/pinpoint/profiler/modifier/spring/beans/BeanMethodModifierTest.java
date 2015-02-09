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

package com.navercorp.pinpoint.profiler.modifier.spring.beans;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.profiler.DefaultAgent;
import com.navercorp.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;
import com.navercorp.pinpoint.profiler.modifier.spring.beans.BeanMethodModifier;
import com.navercorp.pinpoint.test.MockAgent;
import com.navercorp.pinpoint.test.util.BytecodeUtils;

import org.junit.Test;

public class BeanMethodModifierTest {

    private static final String TARGET = Maru.class.getName();
    private static final String TARGET_INTERNAL_NAME = TARGET.replace('.', '/');

    @Test
    public void test() throws Exception {
        DefaultAgent agent = MockAgent.of("pinpoint-spring-bean-test.config");
        ByteCodeInstrumentor realInstrumentor = agent.getByteCodeInstrumentor();

        final ClassLoader loader = getClass().getClassLoader();
        byte[] byteCode = BytecodeUtils.getClassFile(loader, TARGET);

        ByteCodeInstrumentor instrumentor = mock(ByteCodeInstrumentor.class);
        InstrumentClass instrumentClass = mock(InstrumentClass.class);
        
        when(instrumentor.getClass(loader, TARGET_INTERNAL_NAME, byteCode)).thenReturn(instrumentClass);
        when(instrumentClass.isInterceptable()).thenReturn(true);
        when(instrumentClass.getDeclaredMethods(BeanMethodModifier.METHOD_FILTER)).thenReturn(realInstrumentor.getClass(loader, TARGET, byteCode).getDeclaredMethods(BeanMethodModifier.METHOD_FILTER));

        BeanMethodModifier modifier = new BeanMethodModifier(instrumentor);
        modifier.modify(loader, TARGET_INTERNAL_NAME, null, byteCode);
        
                
        verify(instrumentor).getClass(loader, TARGET_INTERNAL_NAME, byteCode);

        verify(instrumentClass).isInterceptable();
        verify(instrumentClass).getDeclaredMethods(BeanMethodModifier.METHOD_FILTER);
        verify(instrumentClass).addInterceptor(eq("publicMethod"), eq(new String[0]), isA(MethodInterceptor.class));
        verify(instrumentClass).addInterceptor(eq("compareTo"), eq(new String[] { TARGET }), isA(MethodInterceptor.class));
        verify(instrumentClass).toBytecode();
        
        verifyNoMoreInteractions(instrumentor, instrumentClass);
    }
}
