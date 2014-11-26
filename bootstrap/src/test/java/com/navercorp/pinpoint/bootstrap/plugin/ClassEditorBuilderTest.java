package com.nhn.pinpoint.bootstrap.plugin;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.Test;

import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.instrument.Scope;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder.InterceptorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder.MetadataBuilder;

public class ClassEditorBuilderTest {

    @Test
    public void test() throws Exception {
        ByteCodeInstrumentor instrumentor = mock(ByteCodeInstrumentor.class);
        TraceContext traceContext = mock(TraceContext.class);
        InstrumentClass aClass = mock(InstrumentClass.class);
        MethodInfo aMethod = mock(MethodInfo.class);
        Scope aScope = mock(Scope.class);
        
        ClassLoader classLoader = getClass().getClassLoader();
        String methodName = "someMethod";
        Class<?>[] parameterTypes = new Class<?>[] { String.class };
        String[] parameterTypeNames = TypeUtils.toClassNames(parameterTypes);
        String scopeName = "test";
        
        when(instrumentor.getScope(scopeName)).thenReturn(aScope);
        when(aClass.getDeclaredMethod(methodName, parameterTypeNames)).thenReturn(aMethod);
        when(aMethod.getName()).thenReturn(methodName);
        when(aMethod.getParameterTypes()).thenReturn(parameterTypeNames);
        when(aClass.addInterceptor(eq(methodName), eq(parameterTypeNames), isA(Interceptor.class))).thenReturn(0);
        
        
        ProfilerPluginContext helper = new ProfilerPluginContext(instrumentor, traceContext);
        ClassEditorBuilder builder = helper.newClassEditorBuilder();
        MetadataBuilder mb = builder.newMetadataBuilder();
        mb.inject("com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilderTest$TestMetadata");
        mb.initializeWithDefaultConstructorOf("java.util.HashMap");
        
        InterceptorBuilder ib = builder.newInterceptorBuilder();
        ib.intercept(methodName, parameterTypeNames);
        ib.with("com.nhn.pinpoint.bootstrap.plugin.TestInterceptor");
        ib.constructedWith("provided");
        ib.in(scopeName);
        
        ClassEditor editor = builder.build();
        
        editor.edit(classLoader, aClass);
        
        verify(aClass).addInterceptor(eq(methodName), isA(String[].class), isA(Interceptor.class));
        verify(aClass).addTraceValue(TestMetadata.class, "new java.util.HashMap();");
    }
    
    public static interface TestMetadata extends TraceValue {
        public Map<String, Object> getMap();
        public void setMap(Map<String, Object> map);
    }
}
