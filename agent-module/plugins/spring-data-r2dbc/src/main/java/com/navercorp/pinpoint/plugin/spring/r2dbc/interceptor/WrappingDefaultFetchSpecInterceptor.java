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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ResultReplaceAroundInterceptor;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;

/**
 * Wrapping variant of {@link DefaultFetchSpecInterceptor} (PoC, config-gated by
 * {@code profiler.spring.data.r2dbc.wrap.publisher}). The original relays the target's
 * AsyncContext into the returned row publisher's injected field; this variant hands back a
 * wrapped publisher carrying the same context instead. {@code all()} returns a per-row Flux,
 * so every row is delivered inside the trace window here.
 */
public class WrappingDefaultFetchSpecInterceptor implements ResultReplaceAroundInterceptor {

    @Override
    public void before(Object target, Class<?> returnType, Object[] args) {
    }

    @Override
    public Object after(Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return result;
        }
        // pure relay, same as the original: no new AsyncContext is created here.
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        return SeamPublisherWrapper.wrap(result, asyncContext);
    }
}
