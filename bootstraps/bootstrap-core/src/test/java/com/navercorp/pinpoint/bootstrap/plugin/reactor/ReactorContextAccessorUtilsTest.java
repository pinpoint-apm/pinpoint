/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReactorContextAccessorUtilsTest {
    @Test
    public void getAsyncContext() {
        ReactorContextAccessor accessorMock = mock(ReactorContextAccessor.class);
        when(accessorMock._$PINPOINT$_getReactorContext()).thenReturn(mock(AsyncContext.class));

        Object[] args = {accessorMock};

        AsyncContext asyncContext = ReactorContextAccessorUtils.getAsyncContext(args, 0);
        Assert.assertNotNull(asyncContext);
    }

    @Test
    public void getAsyncContext_null() {
        Object[] args = null;

        AsyncContext asyncContext = ReactorContextAccessorUtils.getAsyncContext(args, 0);
        Assert.assertNull(asyncContext);
    }

    @Test
    public void getAsyncContext_invalid_type() {
        Object[] args = {"Str"};

        AsyncContext asyncContext = ReactorContextAccessorUtils.getAsyncContext(args, 0);
        Assert.assertNull(asyncContext);
    }

    @Test
    public void getAsyncContext_OOB() {
        ReactorContextAccessor accessorMock = mock(ReactorContextAccessor.class);
        when(accessorMock._$PINPOINT$_getReactorContext()).thenReturn(mock(AsyncContext.class));

        Object[] args = {};

        AsyncContext asyncContext0 = ReactorContextAccessorUtils.getAsyncContext(args, 0);
        Assert.assertNull(asyncContext0);

        AsyncContext asyncContext1 = ReactorContextAccessorUtils.getAsyncContext(args, 1);
        Assert.assertNull(asyncContext1);
    }

    @Test
    public void findAsyncContext() {
        ReactorContextAccessor accessorMock = mock(ReactorContextAccessor.class);
        when(accessorMock._$PINPOINT$_getReactorContext()).thenReturn(mock(AsyncContext.class));

        Object[] args = {"foo", "bar", accessorMock};

        AsyncContext asyncContext = ReactorContextAccessorUtils.findAsyncContext(args, 0);
        Assert.assertNotNull(asyncContext);

        asyncContext = ReactorContextAccessorUtils.findAsyncContext(args, 1);
        Assert.assertNotNull(asyncContext);

        asyncContext = ReactorContextAccessorUtils.findAsyncContext(args, 2);
        Assert.assertNotNull(asyncContext);

        asyncContext = ReactorContextAccessorUtils.findAsyncContext(args, 3);
        Assert.assertNull(asyncContext);

        asyncContext = ReactorContextAccessorUtils.findAsyncContext(args, 0, 2);
        Assert.assertNotNull(asyncContext);

        asyncContext = ReactorContextAccessorUtils.findAsyncContext(args, 1, 2);
        Assert.assertNotNull(asyncContext);

        asyncContext = ReactorContextAccessorUtils.findAsyncContext(args, 0, 1);
        Assert.assertNull(asyncContext);

        asyncContext = ReactorContextAccessorUtils.findAsyncContext(args, 2, 0);
        Assert.assertNull(asyncContext);
    }

    @Test
    public void findAsyncContext_null() {
        Object[] args = null;

        AsyncContext asyncContext = ReactorContextAccessorUtils.findAsyncContext(args, 0);
        Assert.assertNull(asyncContext);
    }

    @Test
    public void findAsyncContext_invalid_type() {
        Object[] args = {"Str"};

        AsyncContext asyncContext = ReactorContextAccessorUtils.findAsyncContext(args, 0);
        Assert.assertNull(asyncContext);
    }

    @Test
    public void findAsyncContext_OOB() {
        Object[] args = {};

        AsyncContext asyncContext0 = ReactorContextAccessorUtils.findAsyncContext(args, 0);
        Assert.assertNull(asyncContext0);

        AsyncContext asyncContext1 = ReactorContextAccessorUtils.findAsyncContext(args, 1);
        Assert.assertNull(asyncContext1);
    }

    @Test
    public void setAsyncContext() {
        ReactorContextAccessor accessorMock = mock(ReactorContextAccessor.class);
        AsyncContext asyncContextMock = mock(AsyncContext.class);

        ReactorContextAccessorUtils.setAsyncContext(asyncContextMock, accessorMock);

        verify(accessorMock)._$PINPOINT$_setReactorContext(asyncContextMock);
    }

    @Test
    public void setAsyncContext_invalid_type() {
        ReactorContextAccessor accessorMock = mock(ReactorContextAccessor.class);
        AsyncContext asyncContextMock = mock(AsyncContext.class);

        ReactorContextAccessorUtils.setAsyncContext(asyncContextMock, "foo");

        verify(accessorMock, never())._$PINPOINT$_setReactorContext(asyncContextMock);
    }


    @Test
    public void setAsyncContextArray() {
        ReactorContextAccessor accessorMock = mock(ReactorContextAccessor.class);
        AsyncContext asyncContextMock = mock(AsyncContext.class);

        Object[] array = {"foo", "bar", accessorMock};

        ReactorContextAccessorUtils.setAsyncContext(asyncContextMock, array, 2);

        verify(accessorMock)._$PINPOINT$_setReactorContext(asyncContextMock);
    }
}