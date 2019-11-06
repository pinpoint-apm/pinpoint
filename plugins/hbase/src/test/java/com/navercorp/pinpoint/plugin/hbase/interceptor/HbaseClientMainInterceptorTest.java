package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HbaseClientMainInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private Trace trace;

    @Mock
    private SpanEventRecorder recorder;

    @Mock
    private InterceptorScope scope;

    @Mock
    private AsyncContext asyncContext;

    @Mock
    private InterceptorScopeInvocation invocation;

    @Test
    public void before() {

        doReturn(trace).when(traceContext).currentTraceObject();
        doReturn(recorder).when(trace).traceBlockBegin();
        doReturn(asyncContext).when(recorder).recordNextAsyncContext();
        doReturn(invocation).when(scope).getCurrentInvocation();

        Object target = new Object();
        Object[] args = new Object[]{"foo", "bar"};

        HbaseClientMainInterceptor interceptor = new HbaseClientMainInterceptor(traceContext, descriptor, scope);
        interceptor.before(target, args);

        verify(recorder).recordServiceType(HbasePluginConstants.HBASE_ASYNC_CLIENT);
        verify(recorder).recordApi(descriptor, args);

    }

    @Test
    public void after() {

        doReturn(trace).when(traceContext).currentTraceObject();

        Object target = new Object();
        Object[] args = new Object[]{"foo", "bar"};

        HbaseClientMainInterceptor interceptor = new HbaseClientMainInterceptor(traceContext, descriptor, scope);
        interceptor.after(target, args,null,null);
    }
}