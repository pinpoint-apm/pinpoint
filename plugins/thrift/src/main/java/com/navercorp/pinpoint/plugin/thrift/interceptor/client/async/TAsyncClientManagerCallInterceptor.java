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

import static com.navercorp.pinpoint.plugin.thrift.ThriftScope.THRIFT_CLIENT_SCOPE;

import java.net.SocketAddress;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;
import com.navercorp.pinpoint.plugin.thrift.ThriftRequestProperty;
import com.navercorp.pinpoint.plugin.thrift.ThriftUtils;

/**
 * @author HyunGil Jeong
 */
@Group(value=THRIFT_CLIENT_SCOPE, executionPoint=ExecutionPolicy.BOUNDARY)
public class TAsyncClientManagerCallInterceptor implements SimpleAroundInterceptor, ThriftConstants {
   
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorGroup group;
    private final MetadataAccessor asyncTraceIdAccessor;
    private final MetadataAccessor asyncNextSpanIdAccessor;
    private final MetadataAccessor asyncCallRemoteAddressAccessor;
    private final MetadataAccessor nonblockingSocketAddressAccessor;
    
    public TAsyncClientManagerCallInterceptor(
            TraceContext traceContext,
            MethodDescriptor descriptor,
            @Name(THRIFT_CLIENT_SCOPE) InterceptorGroup group,
            @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor,
            @Name(METADATA_ASYNC_NEXT_SPAN_ID) MetadataAccessor asyncNextSpanIdAccessor,
            @Name(METADATA_ASYNC_CALL_REMOTE_ADDRESS) MetadataAccessor asyncCallRemoteAddressAccessor,
            @Name(METADATA_NONBLOCKING_SOCKET_ADDRESS) MetadataAccessor nonblockingSocketAddressAccessor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.group = group;
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;
        this.asyncNextSpanIdAccessor = asyncNextSpanIdAccessor;
        this.asyncCallRemoteAddressAccessor = asyncCallRemoteAddressAccessor;
        this.nonblockingSocketAddressAccessor = nonblockingSocketAddressAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        
        if (!validate(target, args)) {
            return;
        }
        
        final Trace trace = this.traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            ThriftRequestProperty parentTraceInfo = new ThriftRequestProperty();
            final boolean shouldSample = trace.canSampled();
            if (!shouldSample) {
                if (isDebug) {
                    logger.debug("set Sampling flag=false");
                }
                parentTraceInfo.setShouldSample(shouldSample);
            } else {
                SpanEventRecorder recorder = trace.traceBlockBegin();
                Object asyncMethodCallObj = args[0];
                // inject async trace info to AsyncMethodCall object
                final AsyncTraceId asyncTraceId = injectAsyncTraceId(asyncMethodCallObj, trace);
                
                recorder.recordServiceType(THRIFT_CLIENT_INTERNAL);
                
                // retrieve connection information
                String remoteAddress = getRemoteAddress(asyncMethodCallObj);
                
                final TraceId nextId = asyncTraceId.getNextTraceId();
                
                // Inject nextSpanId as the actual sending of data will be handled asynchronously.
                final long nextSpanId = nextId.getSpanId();
                parentTraceInfo.setSpanId(nextSpanId);
                
                parentTraceInfo.setTraceId(nextId.getTransactionId());
                parentTraceInfo.setParentSpanId(nextId.getParentSpanId());
                
                parentTraceInfo.setFlags(nextId.getFlags());
                parentTraceInfo.setParentApplicationName(this.traceContext.getApplicationName());
                parentTraceInfo.setParentApplicationType(this.traceContext.getServerTypeCode());
                parentTraceInfo.setAcceptorHost(remoteAddress);

                this.asyncCallRemoteAddressAccessor.set(asyncMethodCallObj, remoteAddress);
                this.asyncNextSpanIdAccessor.set(asyncMethodCallObj, nextSpanId);
            }
            InterceptorGroupInvocation currentTransaction = this.group.getCurrentInvocation();
            currentTransaction.setAttachment(parentTraceInfo);
        } catch (Throwable t) {
            logger.warn("before error. Caused:{}", t.getMessage(), t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        
        final Trace trace = this.traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        
        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(this.descriptor);
            recorder.recordException(throwable);
        } catch (Throwable t) {
            logger.warn("after error. Caused:{}", t.getMessage(), t);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (args.length != 1) {
            return false;
        }
        
        Object asyncMethodCallObj = args[0];
        if (asyncMethodCallObj == null) {
            if (isDebug) {
                logger.debug("Metadata injection target object is null.");
            }
            return false;
        }
        
        if (!this.asyncTraceIdAccessor.isApplicable(asyncMethodCallObj)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({})", METADATA_ASYNC_TRACE_ID);
            }
            return false;
        }
        
        if (!this.asyncNextSpanIdAccessor.isApplicable(asyncMethodCallObj)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({})", METADATA_ASYNC_NEXT_SPAN_ID);
            }
            return false;
        }
        
        if (!this.asyncCallRemoteAddressAccessor.isApplicable(asyncMethodCallObj)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({})", METADATA_ASYNC_CALL_REMOTE_ADDRESS);
            }
            return false;
        }
        
        return true;
    }
    
    private AsyncTraceId injectAsyncTraceId(final Object asyncMethodCallObj, final Trace trace) {
        final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
        SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        recorder.recordNextAsyncId(asyncTraceId.getAsyncId());
        this.asyncTraceIdAccessor.set(asyncMethodCallObj, asyncTraceId);
        if (isDebug) {
            logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
        }
        return asyncTraceId;
    }
    
    private String getRemoteAddress(Object asyncMethodCallObj) {
        if (!this.nonblockingSocketAddressAccessor.isApplicable(asyncMethodCallObj)) {
            if (isDebug) {
                logger.debug("Invalid TAsyncMethodCall object. Need metadata accessor({})", METADATA_NONBLOCKING_SOCKET_ADDRESS);
            }
            return UNKNOWN_ADDRESS;
        }
        Object socketAddress = this.nonblockingSocketAddressAccessor.get(asyncMethodCallObj);
        if (socketAddress instanceof SocketAddress) {
            return ThriftUtils.getHostPort((SocketAddress)socketAddress);
        }
        return UNKNOWN_ADDRESS;
    }
    
}
