/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.interceptor.group;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author emeroad
 */
public class GroupedInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean debugEnabled = logger.isDebugEnabled();

    private final AroundInterceptor interceptor;
    private final InterceptorGroup group;
    private final ExecutionPolicy policy;

    public GroupedInterceptor(AroundInterceptor interceptor, InterceptorGroup group, ExecutionPolicy policy) {
        if (interceptor == null) {
            throw new NullPointerException("interceptor must not be null");
        }
        if (group == null) {
            throw new NullPointerException("group must not be null");
        }
        if (policy == null) {
            throw new NullPointerException("policy must not be null");
        }
        this.interceptor = interceptor;
        this.group = group;
        this.policy = policy;
    }

    @Override
    public void before(Object target, Object[] args) {
        final InterceptorGroupInvocation transaction = group.getCurrentInvocation();
        
        if (transaction.tryEnter(policy)) {
            interceptor.before(target, args);
        } else {
            if (debugEnabled) {
                logger.debug("tryBefore() returns false: interceptorGroupTransaction: {}, executionPoint: {}. Skip interceptor {}", new Object[] {transaction, policy, interceptor.getClass()} );
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final InterceptorGroupInvocation transaction = group.getCurrentInvocation();
        
        if (transaction.canLeave(policy)) {
            interceptor.after(target, args, result, throwable);
            transaction.leave(policy);
        } else {
            if (debugEnabled) {
                logger.debug("tryAfter() returns false: interceptorGroupTransaction: {}, executionPoint: {}. Skip interceptor {}", new Object[] {transaction, policy, interceptor.getClass()} );
            }
        }
    }
}
