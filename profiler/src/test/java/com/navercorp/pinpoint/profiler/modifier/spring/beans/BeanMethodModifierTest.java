package com.nhn.pinpoint.profiler.modifier.spring.beans;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;

import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;
import com.nhn.pinpoint.profiler.util.ClassTransformHelper;
import com.nhn.pinpoint.profiler.util.MockAgent;

public class BeanMethodModifierTest {

    private static final String TARGET = Maru.class.getName();
    private static final String TARGET_INTERNAL_NAME = TARGET.replace('.', '/');

    @Test
    public void test() throws Exception {
        DefaultAgent agent = MockAgent.of("pinpoint-spring-bean-test.config");
        ByteCodeInstrumentor realInstrumentor = agent.getByteCodeInstrumentor();

        ByteCodeInstrumentor instrumentor = mock(ByteCodeInstrumentor.class);
        InstrumentClass instrumentClass = mock(InstrumentClass.class);
        
        when(instrumentor.getClass(TARGET_INTERNAL_NAME)).thenReturn(instrumentClass);
        when(instrumentClass.isInterceptable()).thenReturn(true);
        when(instrumentClass.getDeclaredMethods(BeanMethodModifier.METHOD_FILTER)).thenReturn(realInstrumentor.getClass(TARGET).getDeclaredMethods(BeanMethodModifier.METHOD_FILTER));
        

        ClassLoader loader = getClass().getClassLoader();
        byte[] byteCode = ClassTransformHelper.getClassFile(loader, TARGET);
        

        BeanMethodModifier modifier = new BeanMethodModifier(instrumentor);
        modifier.modify(loader, TARGET_INTERNAL_NAME, null, byteCode);
        
                
        verify(instrumentor).checkLibrary(loader, TARGET_INTERNAL_NAME);
        verify(instrumentor).getClass(TARGET_INTERNAL_NAME);

        verify(instrumentClass).isInterceptable();
        verify(instrumentClass).getDeclaredMethods(BeanMethodModifier.METHOD_FILTER);
        verify(instrumentClass).addInterceptor(eq("publicMethod"), eq(new String[0]), isA(MethodInterceptor.class));
        verify(instrumentClass).addInterceptor(eq("compareTo"), eq(new String[] { TARGET }), isA(MethodInterceptor.class));
        verify(instrumentClass).toBytecode();
        
        verifyNoMoreInteractions(instrumentor, instrumentClass);
    }
}
