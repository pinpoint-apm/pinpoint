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
package com.navercorp.pinpoint.bootstrap.interceptor.scope;

import com.navercorp.pinpoint.bootstrap.context.TraceBlock;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;
import com.navercorp.pinpoint.bootstrap.interceptor.ResultReplaceBlockAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;

import java.util.Objects;

public class ExceptionHandleScopedResultReplaceBlockAroundInterceptor implements ResultReplaceBlockAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean debugEnabled = logger.isDebugEnabled();

    private final ResultReplaceBlockAroundInterceptor delegate;
    private final InterceptorScope scope;
    private final ExecutionPolicy policy;
    private final ExceptionHandler exceptionHandler;

    public ExceptionHandleScopedResultReplaceBlockAroundInterceptor(ResultReplaceBlockAroundInterceptor delegate, InterceptorScope scope, ExecutionPolicy policy, ExceptionHandler exceptionHandler) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.scope = Objects.requireNonNull(scope, "scope");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler, "exceptionHandler");
    }

    @Override
    public TraceBlock before(Object target, Class<?> returnType, Object[] args) {
        final InterceptorScopeInvocation transaction = scope.getCurrentInvocation();

        if (transaction.tryEnter(policy)) {
            try {
                return this.delegate.before(target, returnType, args);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        } else {
            if (debugEnabled) {
                logger.debug("tryBefore() returns false: interceptorScopeTransaction: {}, executionPoint: {}. Skip interceptor {}", transaction, policy, delegate.getClass());
            }
        }

        return null;
    }

    @Override
    public Object after(TraceBlock block, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        final InterceptorScopeInvocation transaction = scope.getCurrentInvocation();

        if (transaction.canLeave(policy)) {
            try {
                return this.delegate.after(block, target, returnType, args, result, throwable);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
                // keep the original return value when the delegate fails.
                return result;
            } finally {
                transaction.leave(policy);
            }
        } else {
            if (debugEnabled) {
                logger.debug("tryAfter() returns false: interceptorScopeTransaction: {}, executionPoint: {}. Skip interceptor {}", transaction, policy, delegate.getClass());
            }
            // skipped by scope policy: keep the original return value.
            return result;
        }
    }
}
