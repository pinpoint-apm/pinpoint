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

public class ClassEditorBuilderTest {

    @Test
    public void test() throws Exception {
        ByteCodeInstrumentor instrumentor = mock(ByteCodeInstrumentor.class);
        TraceContext traceContext = mock(TraceContext.class);
        InstrumentClass aClass = mock(InstrumentClass.class);
        MethodInfo aMethod = mock(MethodInfo.class);
        Scope aScope = mock(Scope.class);
        
        ClassLoader classLoader = getClass().getClassLoader();
        String targetClassName = "com.nhn.pinpoint.bootstrap.plugin.Foo";
        String methodName = "someMethod";
        String[] parameterTypes = new String[] { "java.lang.String" };
        String scopeName = "test";
        byte[] classFileBuffer = BytecodeUtils.getClassFile(classLoader, targetClassName);

        when(instrumentor.getClass(classLoader, targetClassName, classFileBuffer)).thenReturn(aClass);
        when(instrumentor.getScope(scopeName)).thenReturn(aScope);
        when(aClass.getDeclaredMethod(methodName, parameterTypes)).thenReturn(aMethod);
        when(aMethod.getName()).thenReturn(methodName);
        when(aMethod.getParameterTypes()).thenReturn(parameterTypes);
        when(aClass.addInterceptor(eq(methodName), eq(parameterTypes), isA(Interceptor.class))).thenReturn(0);
        
        
        ProfilerPluginHelper helper = new ProfilerPluginHelper(instrumentor, traceContext);
        ClassEditorBuilder builder = helper.getClassEditorBuilderFor(targetClassName);
        builder.intercept(methodName, parameterTypes).with("com.nhn.pinpoint.bootstrap.plugin.TestInterceptor").constructedWith("provided").in(scopeName);
        builder.inject(TestMetadata.class).initializeWithDefaultConstructorOf("java.util.HashMap");
        ClassEditor editor = builder.build();
        
        editor.edit(classLoader, targetClassName, null, classFileBuffer);
        
        verify(aClass).addInterceptor(eq(methodName), isA(String[].class), isA(Interceptor.class));
        verify(aClass).addTraceValue(TestMetadata.class, "new java.util.HashMap();");
    }
    
    public static interface TestMetadata extends TraceValue {
        public Map<String, Object> getMap();
        public void setMap(Map<String, Object> map);
    }
}
