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

/**
 * Like {@link AroundInterceptor}, but the value returned from {@link #after} replaces the
 * intercepted method's return value. This lets a plugin substitute the object the application
 * receives (e.g. wrap a returned {@code Publisher}) instead of mutating it through an injected
 * field.
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
 * </ul>
 * The {@code returnType} parameter carries the intercepted method's declared return type so an
 * implementation can validate a candidate replacement itself before returning it.
 */
public interface ResultReplaceAroundInterceptor extends Interceptor {
    void before(Object target, Class<?> returnType, Object[] args);

    Object after(Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable);
}
