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

package com.navercorp.pinpoint.profiler.plugin.interceptor;

import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.interceptor.ExecutionPoint;
import com.navercorp.pinpoint.bootstrap.plugin.interceptor.InterceptorGroup;

/**
 * @author emeroad
 */
public class ScopedStaticAroundInterceptor implements StaticAroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final boolean debugEnabled = logger.isDebugEnabled();

    private final StaticAroundInterceptor delegate;
    private final InterceptorGroup group;
    private final ExecutionPoint point;

    public ScopedStaticAroundInterceptor(StaticAroundInterceptor delegate, InterceptorGroup group, ExecutionPoint point) {
        this.delegate = delegate;
        this.group = group;
        this.point = point;
    }

    @Override
    public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        Scope scope = group.getCurrentTransaction();
        
        if (scope.tryBefore(point)) {
            this.delegate.before(target, className, methodName, parameterDescription, args);
        } else {
            if (debugEnabled) {
                logger.debug("tryBefore() returns false: scope: {}, executionPonint: {}. Skip interceptor {}", new Object[] {scope, point, delegate.getClass()} );
            }
        }
    }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
        Scope scope = group.getCurrentTransaction();
        
        if (scope.tryAfter(point)) {
            this.delegate.after(target, className, methodName, parameterDescription, args, result, throwable);
        } else {
            if (debugEnabled) {
                logger.debug("tryAfter() returns false: scope: {}, executionPonint: {}. Skip interceptor {}", new Object[] {scope, point, delegate.getClass()} );
            }
        }
    }
}
