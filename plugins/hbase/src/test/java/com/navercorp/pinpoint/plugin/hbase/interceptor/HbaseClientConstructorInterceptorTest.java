package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
public class HbaseClientConstructorInterceptorTest {

    @Mock
    private AsyncContext context;

    @Mock
    private InterceptorScope scope;

    @Mock
    private AsyncContextAccessor target;

    @Mock
    private InterceptorScopeInvocation invocation;

    @Test
    public void after() {
        doReturn(invocation).when(scope).getCurrentInvocation();
        doReturn(context).when(invocation).getAttachment();

        HbaseClientConstructorInterceptor interceptor = new HbaseClientConstructorInterceptor(scope);
        interceptor.after(target, null, null, null);
    }
}