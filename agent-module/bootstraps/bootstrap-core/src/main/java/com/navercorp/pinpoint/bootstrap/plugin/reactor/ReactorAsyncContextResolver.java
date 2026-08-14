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
package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.common.util.ArrayUtils;

/**
 * Resolves the {@link AsyncContext} carried by constructor arguments, choosing one only when the
 * choice is unambiguous. Shared by {@link CoreSubscriberConstructorInterceptor} (operator
 * subscribers) and {@link SchedulerTaskConstructorInterceptor} (scheduler task carriers).
 */
public final class ReactorAsyncContextResolver {

    private ReactorAsyncContextResolver() {
    }

    /**
     * Returns the single distinct non-null {@link AsyncContext} among the arguments, or
     * {@code null} when there is none — or more than one. Some constructors receive both a
     * publisher/source and their actual subscriber; both are instrumented as
     * {@link AsyncContextAccessor}, and a reusable publisher may retain an older context. Two
     * different contexts cannot be told apart here, so neither is chosen arbitrarily: the caller
     * falls back to its own recovery path (subscribe/onSubscribe relay, or the current trace).
     */
    public static AsyncContext findUnique(Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        AsyncContext candidateAsyncContext = null;
        for (Object arg : args) {
            if (arg instanceof AsyncContextAccessor) {
                final AsyncContext asyncContext = ((AsyncContextAccessor) arg)._$PINPOINT$_getAsyncContext();
                if (asyncContext == null) {
                    continue;
                }
                if (candidateAsyncContext == null) {
                    candidateAsyncContext = asyncContext;
                    continue;
                }
                if (candidateAsyncContext != asyncContext) {
                    return null;
                }
            }
        }
        return candidateAsyncContext;
    }
}
