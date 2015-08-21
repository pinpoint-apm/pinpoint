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

import org.apache.thrift.async.TAsyncMethodCall;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.thrift.ThriftUtils;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncCallEndFlagFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncCallRemoteAddressFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncNextSpanIdFieldAccessor;

/**
 * @author HyunGil Jeong
 */
public class TAsyncMethodCallDoWritingRequestBodyInterceptor extends TAsyncMethodCallInternalMethodInterceptor {

    public TAsyncMethodCallDoWritingRequestBodyInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        super.doInBeforeTrace(recorder, target, args);

        Long nextSpanId = ((AsyncNextSpanIdFieldAccessor)target)._$PINPOINT$_getAsyncNextSpanId();
        recorder.recordNextSpanId(nextSpanId);

        String remoteAddress = ((AsyncCallRemoteAddressFieldAccessor)target)._$PINPOINT$_getAsyncCallRemoteAddress();
        recorder.recordDestinationId(remoteAddress);

        String methodUri = ThriftUtils.getAsyncMethodCallName((TAsyncMethodCall<?>)target);
        String thriftUrl = remoteAddress + "/" + methodUri;
        recorder.recordAttribute(THRIFT_URL, thriftUrl);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        super.after(target, args, result, throwable);

        // End async trace block if TAsyncMethodCall.cleanUpAndFireCallback(...) call completed successfully
        // if there was an exception, TAsyncMethodCall.onError(...) will be called and the async trace block will be ended there
        if (throwable != null) {
            return;
        }
        boolean endAsyncBlock = ((AsyncCallEndFlagFieldAccessor)target)._$PINPOINT$_getAsyncCallEndFlag();
        if (endAsyncBlock) {
            final Trace trace = super.traceContext.currentTraceObject();
            // shouldn't be null
            if (trace == null) {
                return;
            }

            if (trace.isAsync() && trace.isRootStack()) {
                trace.close();
                super.traceContext.removeTraceObject();
            }
        }
    }

    @Override
    protected boolean validate(Object target) {
        if (!(target instanceof AsyncNextSpanIdFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", AsyncNextSpanIdFieldAccessor.class.getName());
            }
            return false;
        }
        if (!(target instanceof AsyncCallRemoteAddressFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", AsyncCallRemoteAddressFieldAccessor.class.getName());
            }
            return false;
        }
        if (!(target instanceof AsyncCallEndFlagFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", AsyncCallEndFlagFieldAccessor.class.getName());
            }
            return false;
        }
        return super.validate(target);
    }

    @Override
    protected ServiceType getServiceType() {
        return THRIFT_CLIENT;
    }
}