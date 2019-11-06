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

package com.navercorp.pinpoint.plugin.thrift.interceptor.server.async;

import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import org.apache.thrift.TBaseAsyncProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContext;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;
import com.navercorp.pinpoint.plugin.thrift.ThriftUtils;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncMarkerFlagFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.ServerMarkerFlagFieldAccessor;

/**
 * Entry/exit point for tracing asynchronous processors for Thrift services.
 * <p>
 * Because trace objects cannot be created until the message is read, this interceptor works in tandem with other interceptors in the tracing pipeline. The
 * actual processing of input messages is not off-loaded to <tt>AsyncProcessFunction</tt> (unlike synchronous processors where <tt>ProcessFunction</tt> does
 * most of the work).
 * <ol>
 * <li>
 * <p>
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor TProtocolReadMessageBeginInterceptor} retrieves
 * the method name called by the client.</li>
 * </p>
 * 
 * <li>
 * <p>
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor},
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor TProtocolReadTTypeInterceptor} reads the header fields
 * and injects the parent trace object (if any).</li></p>
 * 
 * <li>
 * <p>
 * {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor TProtocolReadMessageEndInterceptor} creates the
 * actual root trace object.</li></p> </ol>
 * <p>
 * <b><tt>TBaseAsyncProcessorProcessInterceptor</tt></b> -> <tt>TProtocolReadMessageBeginInterceptor</tt> -> <tt>TProtocolReadFieldBeginInterceptor</tt> <->
 * <tt>TProtocolReadTTypeInterceptor</tt> -> <tt>TProtocolReadMessageEndInterceptor</tt>
 * <p>
 * Based on Thrift 0.9.1+
 * 
 * @author HyunGil Jeong
 * 
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor TProtocolReadMessageBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor TProtocolReadTTypeInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor TProtocolReadMessageEndInterceptor
 */
public class TBaseAsyncProcessorProcessInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;

    public TBaseAsyncProcessorProcessInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        // process(final AsyncFrameBuffer fb)
        if (args.length != 1) {
            return;
        }
        // Set server markers
        if (args[0] instanceof AsyncFrameBuffer) {
            AsyncFrameBuffer frameBuffer = (AsyncFrameBuffer)args[0];
            attachMarkersToInputProtocol(frameBuffer.getInputProtocol(), true);
        }

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        // Unset server markers
        if (args[0] instanceof AsyncFrameBuffer) {
            AsyncFrameBuffer frameBuffer = (AsyncFrameBuffer)args[0];
            attachMarkersToInputProtocol(frameBuffer.getInputProtocol(), false);
        }
        final Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        this.traceContext.removeTraceObject();
        if (trace.canSampled()) {
            try {
                processTraceObject(trace, target, args, throwable);
            } catch (Throwable t) {
                logger.warn("Error processing trace object. Cause:{}", t.getMessage(), t);
            } finally {
                trace.close();
            }
        }
    }

    private boolean validateInputProtocol(Object iprot) {
        if (iprot instanceof TProtocol) {
            if (!(iprot instanceof ServerMarkerFlagFieldAccessor)) {
                if (isDebug) {
                    logger.debug("Invalid target object. Need field accessor({}).", ServerMarkerFlagFieldAccessor.class.getName());
                }
                return false;
            }
            if (!(iprot instanceof AsyncMarkerFlagFieldAccessor)) {
                if (isDebug) {
                    logger.debug("Invalid target object. Need field accessor({}).", AsyncMarkerFlagFieldAccessor.class.getName());
                }
                return false;
            }
            return true;
        }
        return false;
    }

    private void attachMarkersToInputProtocol(TProtocol iprot, boolean flag) {
        if (validateInputProtocol(iprot)) {
            ((ServerMarkerFlagFieldAccessor)iprot)._$PINPOINT$_setServerMarkerFlag(flag);
            ((AsyncMarkerFlagFieldAccessor)iprot)._$PINPOINT$_setAsyncMarkerFlag(flag);
        }
    }

    private void processTraceObject(final Trace trace, Object target, Object[] args, Throwable throwable) {
        // end spanEvent
        try {
            // TODO Might need a way to collect and record method arguments
            // trace.recordAttribute(...);
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordException(throwable);
            recorder.recordApi(this.descriptor);
        } catch (Throwable t) {
            logger.warn("Error processing trace object. Cause:{}", t.getMessage(), t);
        } finally {
            trace.traceBlockEnd();
        }

        // end root span
        SpanRecorder recorder = trace.getSpanRecorder();
        String methodUri = getMethodUri(target);
        recorder.recordRpcName(methodUri);
    }

    private String getMethodUri(Object target) {
        String methodUri = ThriftConstants.UNKNOWN_METHOD_URI;
        InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
        Object attachment = currentTransaction.getAttachment();
        if (attachment instanceof ThriftClientCallContext && target instanceof TBaseAsyncProcessor) {
            ThriftClientCallContext clientCallContext = (ThriftClientCallContext)attachment;
            String methodName = clientCallContext.getMethodName();
            methodUri = ThriftUtils.getAsyncProcessorNameAsUri((TBaseAsyncProcessor<?>)target);
            StringBuilder sb = new StringBuilder(methodUri);
            if (!methodUri.endsWith("/")) {
                sb.append("/");
            }
            sb.append(methodName);
            methodUri = sb.toString();
        }
        return methodUri;
    }

}
