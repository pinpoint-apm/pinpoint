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

import com.navercorp.pinpoint.bootstrap.context.TraceBlock;

/**
 * Combines {@link ResultReplaceAroundInterceptor} and {@link BlockAroundInterceptor}: the value
 * returned from {@link #after} replaces the intercepted method's return value, and the
 * {@link TraceBlock} returned from {@link #before} travels to {@link #after} through the weaver.
 * A result-replace interceptor that needs before/after pairing state would otherwise have to
 * carry it out-of-band (e.g. a per-thread frame stack); this shape hands it the same proven
 * channel the block interceptors use.
 * <p>
 * Contract:
 * <ul>
 * <li>The intercepted method must have a reference (object or array) return type. Attaching this
 * interceptor to a method returning {@code void} or a primitive, or to a constructor, fails at
 * instrumentation time.</li>
 * <li>The replacement takes effect only when it is non-null and an instance of the method's
 * declared return type ({@code returnType} parameter); otherwise the original {@code result} is
 * returned to the caller unchanged. Returning {@code result} as-is therefore means "keep".</li>
 * <li>When {@code throwable} is non-null the method is exiting exceptionally: there is no return
 * value to replace and the value returned from {@link #after} is discarded.</li>
 * <li>The {@code block} passed to {@link #after} is exactly what the paired {@link #before}
 * returned, {@code null} included.</li>
 * </ul>
 */
public interface ResultReplaceBlockAroundInterceptor extends Interceptor {
    TraceBlock before(Object target, Class<?> returnType, Object[] args);

    Object after(TraceBlock block, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable);
}
