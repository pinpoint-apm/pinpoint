/**
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author Jongho Moon
 *
 */
public class InterceptorInvokerHelper {
    private static boolean propagateException = false;
    private static final PLogger logger = PLoggerFactory.getLogger(InterceptorInvokerHelper.class.getName());
    private static final boolean debugEnabled = logger.isDebugEnabled();
    
    public static void handleException(Throwable t) {
        if (propagateException) {
            throw new RuntimeException(t);
        } else {
            logger.warn("Excetpion occured from interceptor", t);
        }
    }
    
    public static void setPropagateException(boolean propagate) {
        propagateException = propagate;
    }
    
    public static void logSkipBeforeByExecutionPolicy(InterceptorInstance holder, InterceptorGroupInvocation invocation, ExecutionPolicy policy) {
        if (debugEnabled) {
            logger.debug("Skip before interceptor due to execution policy: interceptorGroupInvocation: {}, executionPolicy: {}, interceptor: {}", new Object[] {invocation, policy, holder.getInterceptor().getClass().getName()} );
        }
    }
    
    public static void logSkipAfterByExecutionPolicy(InterceptorInstance holder, InterceptorGroupInvocation invocation, ExecutionPolicy policy) {
        if (debugEnabled) {
            logger.debug("Skip after interceptor due to execution policy: interceptorGroupInvocation: {}, executionPolicy: {}, interceptor: {}", new Object[] {invocation, policy, holder.getInterceptor().getClass().getName()} );
        }
    }

}
