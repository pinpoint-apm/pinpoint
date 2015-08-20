/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift.interceptor.client.async;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncCallEndFlagFieldAccessor;

/**
 * @author HyunGil Jeong
 */
public class TAsyncMethodCallCleanUpAndFireCallbackInterceptor extends TAsyncMethodCallInternalMethodInterceptor {
    
    public TAsyncMethodCallCleanUpAndFireCallbackInterceptor(
            TraceContext traceContext,
            MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        super.after(target, args, result, throwable);

        // Set a flag to end async trace block if this method completed successfully
        if (throwable != null) {
            return;
        }
        ((AsyncCallEndFlagFieldAccessor)target)._$PINPOINT$_setAsyncCallEndFlag(true);
    }

    @Override
    protected boolean validate(Object target) {
        if (!(target instanceof AsyncCallEndFlagFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", AsyncCallEndFlagFieldAccessor.class.getName());
            }
            return false;
        }
        return super.validate(target);
    }
    
}
