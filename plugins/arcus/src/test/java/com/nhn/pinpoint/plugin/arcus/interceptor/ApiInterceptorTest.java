package com.nhn.pinpoint.plugin.arcus.interceptor;

import static org.mockito.Mockito.*;

import org.junit.Test;

import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor;
import com.nhn.pinpoint.profiler.interceptor.DefaultMethodDescriptor;

public class ApiInterceptorTest {

    @Test
    public void testAround() {
        String[] parameterTypes = new String[] { "java.lang.String", "int", "java.lang.Object" };
        String[] parameterNames = new String[] { "key", "exptime", "value" };
        Object[] args = new Object[] { "key", 10, "my_value" };

        TraceContext traceContext = mock(TraceContext.class);
        MethodDescriptor methodDescriptor = new DefaultMethodDescriptor(Object.class.getName(), "set", parameterTypes, parameterNames);
        MethodInfo methodInfo = mock(MethodInfo.class);
        ServiceCodeAccessor target = mock(ServiceCodeAccessor.class);

        when(methodInfo.getDescriptor()).thenReturn(methodDescriptor);
        when(methodInfo.getParameterTypes()).thenReturn(parameterTypes);
        when(target.__getServiceCode()).thenReturn("serviceCode");

        ApiInterceptor interceptor = new ApiInterceptor(traceContext, methodInfo, true);


        interceptor.before(target, args);
        interceptor.after(target, args, null, null);
    }
}