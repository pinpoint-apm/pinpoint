/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;

/**
 * Like {@link ApiIdAwareAroundInterceptor}, but the {@link AsyncContext} is supplied as an argument
 * instead of being resolved inside the interceptor.
 * <p>
 * The weaver reads the target's injected {@code AsyncContextAccessor} field at the weave site
 * ({@code ALOAD 0; GETFIELD}) and passes the value in. Because that read happens per instrumented
 * class (where {@code this} is a single concrete type), it is a monomorphic field load, avoiding the
 * megamorphic {@code instanceof}+{@code invokeinterface} that {@code AsyncContextAccessorUtils}
 * incurs when a single shared call site observes many receiver types.
 * <p>
 * Contract: a class instrumented with an interceptor of this type MUST also have
 * {@code addField(AsyncContextAccessor.class)} applied; otherwise there is no field to read and the
 * weaver supplies {@code null}.
 */
public interface InjectedAsyncContextApiIdAwareAroundInterceptor extends Interceptor {
    void before(Object target, AsyncContext asyncContext, int apiId, Object[] args);

    void after(Object target, AsyncContext asyncContext, int apiId, Object[] args, Object result, Throwable throwable);
}
