package com.nhn.pinpoint.profiler.modifier.spring.beans;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.Method;
import com.nhn.pinpoint.profiler.junit4.BasePinpointTest;
import com.nhn.pinpoint.profiler.junit4.PinpointJUnit4ClassRunner;
import com.nhn.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;
import com.nhn.pinpoint.profiler.util.ClassTransformHelper;
import com.nhn.pinpoint.profiler.util.MockAgent;

@RunWith(PinpointJUnit4ClassRunner.class)
public class BeanMethodModifierTest extends BasePinpointTest {

    private static final String TARGET = Maru.class.getName();
    private static final String TARGET_INTERNAL_NAME = TARGET.replace('.', '/');

    @Test
    public void test() throws Exception {
        ByteCodeInstrumentor instrumentor = mock(ByteCodeInstrumentor.class);
        InstrumentClass instrumentClass = mock(InstrumentClass.class);
        
        when(instrumentor.getClass(TARGET_INTERNAL_NAME)).thenReturn(instrumentClass);
        when(instrumentClass.isInterceptable()).thenReturn(true);
        when(instrumentClass.getDeclaredMethods(BeanMethodModifier.METHOD_FILTER)).thenReturn(getInstrumentor().getClass(TARGET).getDeclaredMethods(BeanMethodModifier.METHOD_FILTER));
        

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
        
        
        for (Method method : getInstrumentor().getClass(TARGET).getDeclaredMethods()) {
            System.out.println(method.getMethodName() + ", " + Arrays.toString(method.getMethodParams()));
        }
    }

    private ByteCodeInstrumentor getInstrumentor() throws IOException {
        DefaultAgent agent = MockAgent.of("pinpoint-spring-bean-test.config");
        return agent.getByteCodeInstrumentor();
    }
}
