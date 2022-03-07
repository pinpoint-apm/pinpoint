package com.navercorp.pinpoint.bootstrap.async;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    @Test
    public void findAsyncContext() {
        AsyncContextAccessor accessorMock = mock(AsyncContextAccessor.class);
        when(accessorMock._$PINPOINT$_getAsyncContext()).thenReturn(mock(AsyncContext.class));

        Object[] args = {"foo", "bar", accessorMock};

        AsyncContext asyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 0);
        Assert.assertNotNull(asyncContext);

        asyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 1);
        Assert.assertNotNull(asyncContext);

        asyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 2);
        Assert.assertNotNull(asyncContext);

        asyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 3);
        Assert.assertNull(asyncContext);

        asyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 0, 2);
        Assert.assertNotNull(asyncContext);

        asyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 1, 2);
        Assert.assertNotNull(asyncContext);

        asyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 0, 1);
        Assert.assertNull(asyncContext);

        asyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 2, 0);
        Assert.assertNull(asyncContext);
    }

    @Test
    public void findAsyncContext_null() {
        Object[] args = null;

        AsyncContext asyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 0);
        Assert.assertNull(asyncContext);
    }

    @Test
    public void findAsyncContext_invalid_type() {
        Object[] args = {"Str"};

        AsyncContext asyncContext = AsyncContextAccessorUtils.findAsyncContext(args, 0);
        Assert.assertNull(asyncContext);
    }

    @Test
    public void findAsyncContext_OOB() {
        Object[] args = {};

        AsyncContext asyncContext0 = AsyncContextAccessorUtils.findAsyncContext(args, 0);
        Assert.assertNull(asyncContext0);

        AsyncContext asyncContext1 = AsyncContextAccessorUtils.findAsyncContext(args, 1);
        Assert.assertNull(asyncContext1);
    }

    @Test
    public void setAsyncContext() {
        AsyncContextAccessor accessorMock = mock(AsyncContextAccessor.class);
        AsyncContext asyncContextMock = mock(AsyncContext.class);

        AsyncContextAccessorUtils.setAsyncContext(asyncContextMock, accessorMock);

        verify(accessorMock)._$PINPOINT$_setAsyncContext(asyncContextMock);
    }

    @Test
    public void setAsyncContext_invalid_type() {
        AsyncContextAccessor accessorMock = mock(AsyncContextAccessor.class);
        AsyncContext asyncContextMock = mock(AsyncContext.class);

        AsyncContextAccessorUtils.setAsyncContext(asyncContextMock, "foo");

        verify(accessorMock, never())._$PINPOINT$_setAsyncContext(asyncContextMock);
    }


    @Test
    public void setAsyncContextArray() {
        AsyncContextAccessor accessorMock = mock(AsyncContextAccessor.class);
        AsyncContext asyncContextMock = mock(AsyncContext.class);

        Object[] array = {"foo", "bar", accessorMock};

        AsyncContextAccessorUtils.setAsyncContext(asyncContextMock, array, 2);

        verify(accessorMock)._$PINPOINT$_setAsyncContext(asyncContextMock);
    }
}