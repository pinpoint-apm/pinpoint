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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorContextAccessor;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class FluxAndMonoSubscribeInterceptorTest {

    @Test
    public void getAsyncContextTest() {
        final TraceContext mockContext = Mockito.mock(TraceContext.class);
        final MethodDescriptor mockMethodDescriptor = Mockito.mock(MethodDescriptor.class);

        final FluxAndMonoSubscribeInterceptor interceptor = new FluxAndMonoSubscribeInterceptor(mockContext, mockMethodDescriptor, ServiceType.INTERNAL_METHOD);

        AsyncContext asyncContext = interceptor.getAsyncContext(new MockTarget(Mockito.mock(AsyncContext.class), null), new Object[0]);
        Assert.assertNotNull(asyncContext);


        asyncContext = interceptor.getAsyncContext(new MockTarget(null, Mockito.mock(AsyncContext.class)), new Object[0]);
        Assert.assertNotNull(asyncContext);


        Object[] args = {new MockTarget(null, Mockito.mock(AsyncContext.class))};
        asyncContext = interceptor.getAsyncContext(new MockTarget(null, null), args);
        Assert.assertNotNull(asyncContext);
    }

    static class MockTarget implements AsyncContextAccessor, ReactorContextAccessor {

        public MockTarget(AsyncContext asyncContext, AsyncContext reacctorAsyncContext) {
            this.asyncContext = asyncContext;
            this.reacctorAsyncContext = reacctorAsyncContext;
        }

        private AsyncContext asyncContext;
        private AsyncContext reacctorAsyncContext;

        @Override
        public void _$PINPOINT$_setAsyncContext(AsyncContext asyncContext) {
            this.asyncContext = asyncContext;
        }

        @Override
        public AsyncContext _$PINPOINT$_getAsyncContext() {
            return asyncContext;
        }

        @Override
        public void _$PINPOINT$_setReactorContext(AsyncContext asyncContext) {
            this.reacctorAsyncContext = asyncContext;
        }

        @Override
        public AsyncContext _$PINPOINT$_getReactorContext() {
            return reacctorAsyncContext;
        }
    }

}
