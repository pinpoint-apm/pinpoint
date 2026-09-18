/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;

/**
 * Produces the exception-guard wrapper for an interceptor. Both methods are best-effort: a
 * {@code null} result means "no dedicated wrapper for this delegate" and the caller must fall
 * back to the shared {@code ExceptionHandle*} wrapper, so the worst case is exactly the shared
 * wrapper behavior.
 */
public interface GuardedInterceptorFactory {

    /**
     * @return the delegate wrapped in a dedicated exception guard, or {@code null} to use the
     * shared wrapper
     */
    Interceptor wrap(Interceptor delegate, ExceptionHandler exceptionHandler);

    /**
     * @return the delegate wrapped in a dedicated scoped exception guard, or {@code null} to use
     * the shared scoped wrapper
     */
    Interceptor wrapScoped(Interceptor delegate, InterceptorScope scope, ExecutionPolicy policy, ExceptionHandler exceptionHandler);
}
