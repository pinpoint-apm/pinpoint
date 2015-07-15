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

package com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server;

import static com.navercorp.pinpoint.plugin.thrift.ThriftScope.*;

import org.apache.thrift.protocol.TProtocol;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContext;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;
import com.navercorp.pinpoint.plugin.thrift.ThriftRequestProperty;
import com.navercorp.pinpoint.plugin.thrift.descriptor.ThriftServerEntryMethodDescriptor;

/**
 * This interceptor intercepts the point in which the client message is read, and creates a new trace object
 * to indicate the starting point of a new span.
 * <ul>
 *   <li>Synchronous
 *     <p><tt>TBaseProcessorProcessInterceptor</tt> -> <tt>ProcessFunctionProcessInterceptor</tt> -> 
 *        <tt>TProtocolReadFieldBeginInterceptor</tt> <-> <tt>TProtocolReadTTypeInterceptor</tt> -> <b><tt>TProtocolReadMessageEndInterceptor</tt></b>
 *   </li>
 *   <li>Asynchronous
 *     <p><tt>TBaseAsyncProcessorProcessInterceptor</tt> -> <tt>TProtocolReadMessageBeginInterceptor</tt> -> 
 *        <tt>TProtocolReadFieldBeginInterceptor</tt> <-> <tt>TProtocolReadTTypeInterceptor</tt> -> <b><tt>TProtocolReadMessageEndInterceptor</tt></b>
 * </ul>
 * <p>
 * Based on Thrift 0.8.0+
 * 
 * @author HyunGil Jeong
 * 
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.server.TBaseProcessorProcessInterceptor TBaseProcessorProcessInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.server.ProcessFunctionProcessInterceptor ProcessFunctionProcessInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.server.async.TBaseAsyncProcessorProcessInterceptor TBaseAsyncProcessProcessInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor TProtocolReadMessageBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor TProtocolReadTTypeInterceptor
 */
@Group(value=THRIFT_SERVER_SCOPE, executionPoint=ExecutionPolicy.INTERNAL)
public class TProtocolReadMessageEndInterceptor implements SimpleAroundInterceptor, ThriftConstants {
    
    private final ThriftServerEntryMethodDescriptor thriftServerEntryMethodDescriptor = new ThriftServerEntryMethodDescriptor();
    
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();
    
    private final TraceContext traceContext;
    private final InterceptorGroup group;
    private final MetadataAccessor serverTraceMarker;

    public TProtocolReadMessageEndInterceptor(
            TraceContext traceContext,
            @Name(THRIFT_SERVER_SCOPE) InterceptorGroup group,
            @Name(METADATA_SERVER_MARKER) MetadataAccessor serverTraceMarker) {
        this.traceContext = traceContext;
        this.group = group;
        this.serverTraceMarker = serverTraceMarker;
        this.traceContext.cacheApi(this.thriftServerEntryMethodDescriptor);
    }
    
    @Override
    public void before(Object target, Object[] args) {
        // Do nothing
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        if (!shouldTrace(target)) {
            return;
        }
        
        InterceptorGroupInvocation currentTransaction = this.group.getCurrentInvocation();
        Object attachment = currentTransaction.getAttachment();
        if (attachment instanceof ThriftClientCallContext) {
            ThriftClientCallContext clientCallContext = (ThriftClientCallContext)attachment;
            String methodName = clientCallContext.getMethodName();
            ThriftRequestProperty parentTraceInfo = clientCallContext.getTraceHeader();
            try {
                this.logger.debug("parentTraceInfo : {}", parentTraceInfo);
                recordTrace(parentTraceInfo, methodName);
            } catch (Throwable t) {
                logger.warn("Error creating trace object. Cause:{}", t.getMessage(), t);
            }
        }
    }
    
    private boolean shouldTrace(Object target) {
        if (target instanceof TProtocol) {
            TProtocol protocol = (TProtocol)target;
            if (this.serverTraceMarker.isApplicable(protocol) && this.serverTraceMarker.get(protocol) != null) {
                Boolean tracingServer = this.serverTraceMarker.get(protocol);
                return tracingServer;
            }
        }
        return false;
    }

    private void recordTrace(ThriftRequestProperty parentTraceInfo, String methodName) {
        final Trace trace = createTrace(parentTraceInfo, methodName);
        if (trace == null) {
            return;
        }
        if (!trace.canSampled()) {
            return;
        }
        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(THRIFT_SERVER_INTERNAL);
    }

    private Trace createTrace(ThriftRequestProperty parentTraceInfo, String methodName) {
        // Check if parent trace info is set.
        // If it is, then make a continued trace object (from parent application)
        // If not, make a new trace object (from user cloud)

        // Check sampling flag from client. If the flag is false, do not sample this request. 
        final boolean shouldSample = checkSamplingFlag(parentTraceInfo);
        if (!shouldSample) {
            // Even if this transaction is not a sampling target, we have to create Trace object to mark 'not sampling'.
            // For example, if this transaction invokes rpc call, we can add parameter to tell remote node 'don't sample this transaction'
            if (isDebug) {
                logger.debug("Disable sampling flag given from remote. Skipping trace for method:{}", methodName);
            }
            return this.traceContext.disableSampling();
        }
        
        final TraceId traceId = populateTraceIdThriftHeader(parentTraceInfo);
        if (traceId != null) {
            // Parent trace info given
            // TODO Maybe we should decide to trace or not even if the sampling flag is true to prevent too many requests are traced.
            final Trace trace = this.traceContext.continueTraceObject(traceId);
            if (trace.canSampled()) {
                recordRootSpan(trace, parentTraceInfo);
                if (isDebug) {
                    logger.debug("TraceId exists - continue trace. TraceId:{}, method:{}", traceId, methodName);
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceId exists, canSampled is false - skip trace. TraceId:{}, method:{}", traceId, methodName);
                }
            }
            return trace;
        } else {
            // No parent trace info, start new trace
            final Trace trace = traceContext.newTraceObject();
            if (trace.canSampled()) {
                recordRootSpan(trace, parentTraceInfo);
                if (isDebug) {
                    logger.debug("TraceId does not exist - start new trace. Method:{}", methodName);
                }
            } else {
                if (isDebug) {
                    logger.debug("TraceId does not exist, canSampled is false - skip trace. Method:{}", methodName);
                }
            }
            return trace;
        }
    }
    
    private void recordRootSpan(final Trace trace, final ThriftRequestProperty parentTraceInfo) {
        // begin root span
        SpanRecorder recorder = trace.getSpanRecorder();
        recorder.recordServiceType(THRIFT_SERVER);
        recorder.recordApi(this.thriftServerEntryMethodDescriptor);
        if (!trace.isRoot()) {
            recordParentInfo(recorder, parentTraceInfo);
        }
    }

    private boolean checkSamplingFlag(ThriftRequestProperty parentTraceInfo) {
        // parent trace info not given, should start a new trace and thus be sampled
        if (parentTraceInfo == null) {
            return true;
        }
        // optional value
        final Boolean samplingFlag = parentTraceInfo.shouldSample();
        if (isDebug) {
            logger.debug("SamplingFlag:{}", samplingFlag);
        }
        if (samplingFlag == null) {
            return true;
        }
        return samplingFlag;
    }

    private TraceId populateTraceIdThriftHeader(ThriftRequestProperty parentTraceInfo) {
        if (parentTraceInfo == null) {
            return null;
        }
        String transactionId = parentTraceInfo.getTraceId();
        long parentSpanId = parentTraceInfo.getParentSpanId(SpanId.NULL);
        long spanId = parentTraceInfo.getSpanId(SpanId.NULL);
        short flags = parentTraceInfo.getFlags((short)0);
        
        return this.traceContext.createTraceId(transactionId, parentSpanId, spanId, flags);
    }
    
    private void recordParentInfo(SpanRecorder recorder, ThriftRequestProperty parentTraceInfo) {
        if (parentTraceInfo == null) {
            return;
        }
        final String parentApplicationName = parentTraceInfo.getParentApplicationName();
        final short parentApplicationType = parentTraceInfo.getParentApplicationType(ServiceType.UNDEFINED.getCode());
        final String acceptorHost = parentTraceInfo.getAcceptorHost();
        recorder.recordParentApplication(parentApplicationName, parentApplicationType);
        recorder.recordAcceptorHost(acceptorHost);
    }
    
}
