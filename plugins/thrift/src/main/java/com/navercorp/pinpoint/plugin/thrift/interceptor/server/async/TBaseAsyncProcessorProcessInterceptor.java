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

import static com.navercorp.pinpoint.plugin.thrift.ThriftScope.THRIFT_SERVER_SCOPE;

import java.net.Socket;

import org.apache.thrift.TBaseAsyncProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContext;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;
import com.navercorp.pinpoint.plugin.thrift.ThriftUtils;

/**
 * Entry/exit point for tracing asynchronous processors for Thrift services.
 * <p>
 * Because trace objects cannot be created until the message is read, this interceptor works in tandem with 
 * other interceptors in the tracing pipeline. The actual processing of input messages is not off-loaded to
 * <tt>AsyncProcessFunction</tt> (unlike synchronous processors where <tt>ProcessFunction</tt> does most of the
 * work).
 * <ol>
 *   <li><p> {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor TProtocolReadMessageBeginInterceptor}
 *   retrieves the method name called by the client.</li></p>
 *   
 *   <li><p> {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor},
 *   {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor TProtocolReadTTypeInterceptor}
 *   reads the header fields and injects the parent trace object (if any).</li></p>
 *   
 *   <li><p> {@link com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageEndInterceptor TProtocolReadMessageEndInterceptor}
 *   creates the actual root trace object.</li></p>
 * </ol>
 * <p>
 * <b><tt>TBaseAsyncProcessorProcessInterceptor</tt></b> -> <tt>TProtocolReadMessageBeginInterceptor</tt> -> 
 * <tt>TProtocolReadFieldBeginInterceptor</tt> <-> <tt>TProtocolReadTTypeInterceptor</tt> -> <tt>TProtocolReadMessageEndInterceptor</tt>
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
@Group(value=THRIFT_SERVER_SCOPE, executionPoint=ExecutionPolicy.BOUNDARY)
public class TBaseAsyncProcessorProcessInterceptor implements SimpleAroundInterceptor, ThriftConstants {
    
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorGroup group;
    private final MetadataAccessor socketAccessor;
    private final MetadataAccessor serverTraceMarker;
    private final MetadataAccessor asyncMarker;
    
    public TBaseAsyncProcessorProcessInterceptor(
            TraceContext traceContext,
            MethodDescriptor descriptor,
            @Name(THRIFT_SERVER_SCOPE) InterceptorGroup group,
            @Name(METADATA_SOCKET) MetadataAccessor socketAccessor,
            @Name(METADATA_SERVER_MARKER) MetadataAccessor serverTraceMarker,
            @Name(METADATA_ASYNC_MARKER) MetadataAccessor asyncMarker) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.group = group;
        this.socketAccessor = socketAccessor;
        this.serverTraceMarker = serverTraceMarker;
        this.asyncMarker = asyncMarker;
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
            attachMarkersToInputProtocol(frameBuffer.getInputProtocol(), Boolean.TRUE);
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
            attachMarkersToInputProtocol(frameBuffer.getInputProtocol(), Boolean.FALSE);
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
    
    private void attachMarkersToInputProtocol(TProtocol iprot, Boolean value) {
        if (this.serverTraceMarker.isApplicable(iprot) && this.asyncMarker.isApplicable(iprot)) {
            this.serverTraceMarker.set(iprot, value);
            this.asyncMarker.set(iprot, value);
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
        // retrieve connection information
        String localIpPort = UNKNOWN_ADDRESS;
        String remoteAddress = UNKNOWN_ADDRESS;
        if (args.length == 1 && args[0] instanceof AsyncFrameBuffer) {
            AsyncFrameBuffer frameBuffer = (AsyncFrameBuffer)args[0];
            if (this.socketAccessor.isApplicable(frameBuffer.getInputProtocol().getTransport())) {
                Socket socket = this.socketAccessor.get(frameBuffer.getInputProtocol().getTransport());
                if (socket != null) {
                    localIpPort = ThriftUtils.getHostPort(socket.getLocalSocketAddress());
                    remoteAddress = ThriftUtils.getHost(socket.getRemoteSocketAddress());
                }
            }
        }
        if (localIpPort != UNKNOWN_ADDRESS) {
            recorder.recordEndPoint(localIpPort);
        }
        if (remoteAddress != UNKNOWN_ADDRESS) {
            recorder.recordRemoteAddress(remoteAddress);
        }
    }
    
    private String getMethodUri(Object target) {
        String methodUri = UNKNOWN_METHOD_URI;
        InterceptorGroupInvocation currentTransaction = this.group.getCurrentInvocation();
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
