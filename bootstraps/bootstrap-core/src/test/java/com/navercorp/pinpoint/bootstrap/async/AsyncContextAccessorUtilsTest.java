package com.navercorp.pinpoint.bootstrap.async;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsyncContextAccessorUtilsTest {
    @Test
    public void getAsyncContext() {
        AsyncContextAccessor accessorMock = mock(AsyncContextAccessor.class);
        when(accessorMock._$PINPOINT$_getAsyncContext()).thenReturn(mock(AsyncContext.class));

        Object[] args = {accessorMock};

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
        Assert.assertNotNull(asyncContext);
    }

    @Test
    public void getAsyncContext_null() {
        Object[] args = null;

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
        Assert.assertNull(asyncContext);
    }

    @Test
    public void getAsyncContext_invalid_type() {
        Object[] args = {"Str"};

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
        Assert.assertNull(asyncContext);
    }

    @Test
    public void getAsyncContext_OOB() {
        AsyncContextAccessor accessorMock = mock(AsyncContextAccessor.class);
        when(accessorMock._$PINPOINT$_getAsyncContext()).thenReturn(mock(AsyncContext.class));

        Object[] args = {};

        AsyncContext asyncContext0 = AsyncContextAccessorUtils.getAsyncContext(args, 0);
        Assert.assertNull(asyncContext0);

        AsyncContext asyncContext1 = AsyncContextAccessorUtils.getAsyncContext(args, 1);
        Assert.assertNull(asyncContext1);
    }


}