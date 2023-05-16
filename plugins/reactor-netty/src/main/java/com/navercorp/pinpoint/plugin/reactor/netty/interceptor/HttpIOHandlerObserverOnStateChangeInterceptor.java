/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.IntBooleanIntBooleanValue;
import com.navercorp.pinpoint.plugin.reactor.netty.HttpCallContext;
import com.navercorp.pinpoint.plugin.reactor.netty.HttpCallContextAccessor;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyConstants;
import reactor.netty.ConnectionObserver;

public class HttpIOHandlerObserverOnStateChangeInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    // The request has been prepared and ready for I/O handler to be invoked
    private static final String REQUEST_PREPARED = "[request_prepared]";
    // The request has been sent
    private static final String REQUEST_SENT = "[request_sent]";
    // The request has been sent but the response has not been fully received and the connection has been prematurely closed
    private static final String RESPONSE_INCOMPLETE = "[response_incomplete]";
    // The response status and headers have been received
    private static final String RESPONSE_RECEIVED = "[response_received]";
    // The response fully received
    private static final String RESPONSE_COMPLETED = "[response_completed]";
    // Propagated when a connection has been established and is available
    private static final String CONNECTED = "[connected]";
    // Propagated when a connection is bound to a channelOperation and ready for user interaction
    private static final String CONFIGURED = "[configured]";
    // Propagated when a connection has been reused / acquired (keep-alive or pooling)
    private static final String ACQUIRED = "[acquired]";
    // Propagated when a connection has been released but not fully closed (keep-alive or pooling)
    private static final String RELEASED = "[released]";
    // Propagated when a connection is being fully closed
    private static final String DISCONNECTING = "[disconnecting]";

    public HttpIOHandlerObserverOnStateChangeInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args) {
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (asyncContext == null) {
            return null;
        }
        // for compatibility.
        final Object state = ArrayArgumentUtils.getArgument(args, 1, Object.class);
        if (state == null) {
            return null;
        }

        final String rawState = state.toString();
        if (isReady(rawState) || isClosed(rawState)) {
            return asyncContext;
        }

        if (target instanceof HttpCallContextAccessor) {
            final HttpCallContext callContext = ((HttpCallContextAccessor) target)._$PINPOINT$_getHttpCallContext();
            setupHttpCall(callContext, rawState);
        }

        return null;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        // for compatibility.
        final Object state = ArrayArgumentUtils.getArgument(args, 1, Object.class);
        if (state == null) {
            return;
        }
        final String rawState = state.toString();
        if (isReady(rawState)) {
            final String value = "READY " + rawState;
            recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, value);
        } else if (isClosed(rawState)) {
            if (target instanceof HttpCallContextAccessor) {
                final HttpCallContext httpCallContext = ((HttpCallContextAccessor) target)._$PINPOINT$_getHttpCallContext();
                final IntBooleanIntBooleanValue value = new IntBooleanIntBooleanValue((int) httpCallContext.getWriteElapsedTime(), httpCallContext.isWriteFail(), (int) httpCallContext.getReadElapsedTime(), httpCallContext.isReadFail());
                recorder.recordAttribute(AnnotationKey.HTTP_IO, value);
                // Clear HttpCallContext
                ((HttpCallContextAccessor) target)._$PINPOINT$_setHttpCallContext(null);
            }
            final String value = "CLOSED " + rawState;
            recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, value);
        }
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (asyncContext == null) {
            return null;
        }

        // for compatibility.
        final Object state = ArrayArgumentUtils.getArgument(args, 1, Object.class);
        if (state == null) {
            return null;
        }

        final String rawState = state.toString();
        if (isReady(rawState) || isClosed(rawState)) {
            return asyncContext;
        }

        return null;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordException(throwable);
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(ReactorNettyConstants.REACTOR_NETTY_CLIENT_INTERNAL);
    }

    boolean isReady(String state) {
        if (state == null) {
            return false;
        }
        if (state.equals(CONNECTED) || state.equals(ACQUIRED)) {
            return true;
        }
        return false;
    }

    boolean isClosed(String state) {
        if (state == null) {
            return false;
        }

        if (state.equals(RELEASED) || state.equals(DISCONNECTING)) {
            return true;
        }
        return false;
    }

    void setupHttpCall(HttpCallContext httpCallContext, String state) {
        if (httpCallContext == null || state == null) {
            return;
        }

        if (state.equals(REQUEST_PREPARED)) {
            httpCallContext.setWriteBeginTime(System.currentTimeMillis());
        } else if (state.equals(REQUEST_SENT)) {
            httpCallContext.setWriteEndTime(System.currentTimeMillis());
        } else if (state.equals(RESPONSE_RECEIVED)) {
            httpCallContext.setReadBeginTime(System.currentTimeMillis());
        } else if (state.equals(RESPONSE_COMPLETED)) {
            httpCallContext.setReadEndTime(System.currentTimeMillis());
        } else if (state.equals(RESPONSE_INCOMPLETE)) {
            httpCallContext.setReadFail(Boolean.TRUE);
        }
    }
}
