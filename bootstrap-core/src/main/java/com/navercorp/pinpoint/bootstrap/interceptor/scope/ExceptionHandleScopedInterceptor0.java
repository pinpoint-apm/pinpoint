/*
 * Copyright 2016 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionHandler;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class ExceptionHandleScopedInterceptor0 implements AroundInterceptor0 {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean debugEnabled = logger.isDebugEnabled();

    private final AroundInterceptor0 interceptor;
    private final InterceptorScope scope;
    private final ExecutionPolicy policy;
    private final ExceptionHandler exceptionHandler;

    public ExceptionHandleScopedInterceptor0(AroundInterceptor0 interceptor, InterceptorScope scope, ExecutionPolicy policy, ExceptionHandler exceptionHandler) {
        if (interceptor == null) {
            throw new NullPointerException("interceptor");
        }
        if (scope == null) {
            throw new NullPointerException("scope");
        }
        if (policy == null) {
            throw new NullPointerException("policy");
        }
        if (exceptionHandler == null) {
            throw new NullPointerException("exceptionHandler");
        }
        this.interceptor = interceptor;
        this.scope = scope;
        this.policy = policy;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void before(Object target) {
        final InterceptorScopeInvocation transaction = scope.getCurrentInvocation();

        if (transaction.tryEnter(policy)) {
            try {
                this.interceptor.before(target);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            }
        } else {
            if (debugEnabled) {
                logger.debug("tryBefore() returns false: interceptorScopeTransaction: {}, executionPoint: {}. Skip interceptor {}", transaction, policy, interceptor.getClass());
            }
        }
    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {
        final InterceptorScopeInvocation transaction = scope.getCurrentInvocation();

        if (transaction.canLeave(policy)) {
            try {
                this.interceptor.after(target, result, throwable);
            } catch (Throwable t) {
                exceptionHandler.handleException(t);
            } finally {
                transaction.leave(policy);
            }
        } else {
            if (debugEnabled) {
                logger.debug("tryAfter() returns false: interceptorScopeTransaction: {}, executionPoint: {}. Skip interceptor {}", transaction, policy, interceptor.getClass());
            }
        }
    }
}