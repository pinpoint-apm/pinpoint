/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoOperatorSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoSubscribeInterceptor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.reactor.interceptor.FluxAndMonoSubscribeInterceptorTest.MockTarget;

public class FluxAndMonoOperatorSubscribeInterceptorTest {


    @Test
    public void getAsyncContextTest() {
        final TraceContext mockContext = Mockito.mock(TraceContext.class);
        final MethodDescriptor mockMethodDescriptor = Mockito.mock(MethodDescriptor.class);

        final FluxAndMonoOperatorSubscribeInterceptor interceptor = new FluxAndMonoOperatorSubscribeInterceptor(mockContext, mockMethodDescriptor, ServiceType.INTERNAL_METHOD);

        AsyncContext asyncContext = interceptor.getAsyncContext(new MockTarget(Mockito.mock(AsyncContext.class), null), new Object[0]);
        Assert.assertNotNull(asyncContext);

        asyncContext = interceptor.getAsyncContext(new MockTarget(null, Mockito.mock(AsyncContext.class)), new Object[0]);
        Assert.assertNotNull(asyncContext);

        Object[] args = {new MockTarget(null, Mockito.mock(AsyncContext.class))};
        asyncContext = interceptor.getAsyncContext(new MockTarget(null, null), args);
        Assert.assertNotNull(asyncContext);
    }
}
