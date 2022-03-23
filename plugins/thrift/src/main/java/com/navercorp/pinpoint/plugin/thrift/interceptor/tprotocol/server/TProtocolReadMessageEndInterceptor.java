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

import java.net.Socket;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanId;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.thrift.ThriftClientCallContext;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;
import com.navercorp.pinpoint.plugin.thrift.ThriftRequestProperty;
import com.navercorp.pinpoint.plugin.thrift.ThriftUtils;
import com.navercorp.pinpoint.plugin.thrift.descriptor.ThriftServerEntryMethodDescriptor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.ServerMarkerFlagFieldAccessor;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.SocketFieldAccessor;

/**
 * This interceptor intercepts the point in which the client message is read, and creates a new trace object to indicate the starting point of a new span.
 * <ul>
 * <li>Synchronous
 * <p>
 * <tt>TBaseProcessorProcessInterceptor</tt> -> <tt>ProcessFunctionProcessInterceptor</tt> -> <tt>TProtocolReadFieldBeginInterceptor</tt> <->
 * <tt>TProtocolReadTTypeInterceptor</tt> -> <b><tt>TProtocolReadMessageEndInterceptor</tt></b></li>
 * <li>Asynchronous
 * <p>
 * <tt>TBaseAsyncProcessorProcessInterceptor</tt> -> <tt>TProtocolReadMessageBeginInterceptor</tt> -> <tt>TProtocolReadFieldBeginInterceptor</tt> <->
 * <tt>TProtocolReadTTypeInterceptor</tt> -> <b><tt>TProtocolReadMessageEndInterceptor</tt></b>
 * </ul>
 * <p>
 * Based on Thrift 0.8.0+
 *
 * @author HyunGil Jeong
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.server.TBaseProcessorProcessInterceptor TBaseProcessorProcessInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.server.ProcessFunctionProcessInterceptor ProcessFunctionProcessInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.server.async.TBaseAsyncProcessorProcessInterceptor TBaseAsyncProcessProcessInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadMessageBeginInterceptor TProtocolReadMessageBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadFieldBeginInterceptor TProtocolReadFieldBeginInterceptor
 * @see com.navercorp.pinpoint.plugin.thrift.interceptor.tprotocol.server.TProtocolReadTTypeInterceptor TProtocolReadTTypeInterceptor
 */
public class TProtocolReadMessageEndInterceptor implements AroundInterceptor {
    private static final String SCOPE_NAME = "##THRIFT_SERVER_PROTOCOL_TRACE";
    private final ThriftServerEntryMethodDescriptor thriftServerEntryMethodDescriptor = new ThriftServerEntryMethodDescriptor();

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;

    public TProtocolReadMessageEndInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
        this.traceContext.cacheApi(this.thriftServerEntryMethodDescriptor);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validate(target)) {
            return;
        }

        final InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
        if (currentTransaction == null) {
            return;
        }

        final Object attachment = currentTransaction.getAttachment();
        if (attachment instanceof ThriftClientCallContext) {
            ThriftClientCallContext clientCallContext = (ThriftClientCallContext) attachment;
            try {
                if (traceContext.currentRawTraceObject() != null) {
                    // duplicate trace.
                    return;
                }

                // Check if there is an active trace first for cases such as TServlet, which one of the WAS plugins would
                // have created one already.
                ThriftRequestProperty parentTraceInfo = clientCallContext.getTraceHeader();
                this.logger.debug("parentTraceInfo : {}", parentTraceInfo);

                final String methodUri = toMethodUri(clientCallContext);
                final Trace trace = createTrace(target, parentTraceInfo, methodUri);
                clientCallContext.setEntryPoint(true);
                if (trace == null) {
                    return;
                }

                if (!initScope(trace)) {
                    // invalid scope.
                    deleteTrace(trace);
                    return;
                }
                entryScope(trace);

                if (!trace.canSampled()) {
                    return;
                }

                SpanEventRecorder recorder = trace.traceBlockBegin();
                recorder.recordServiceType(ThriftConstants.THRIFT_SERVER_INTERNAL);
                if (methodUri != null) {
                    recorder.recordAttribute(ThriftConstants.THRIFT_URL, methodUri);
                }
            } catch (Throwable t) {
                logger.warn("Error creating trace object. Cause:{}", t.getMessage(), t);
            }
        }
    }

    private String toMethodUri(ThriftClientCallContext clientCallContext) {
        String methodUri = ThriftConstants.UNKNOWN_METHOD_URI;
        final String methodName = clientCallContext.getMethodName();
        final String processName = clientCallContext.getProcessName();
        if (processName != null) {
            StringBuilder sb = new StringBuilder(processName);
            if (!processName.endsWith("/")) {
                sb.append("/");
            }
            if (methodName != null) {
                sb.append(methodName);
            }
            methodUri = sb.toString();
        }
        return methodUri;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (!validate(target)) {
            return;
        }

        final InterceptorScopeInvocation currentTransaction = this.scope.getCurrentInvocation();
        final Object attachment = currentTransaction.getAttachment();
        if (attachment instanceof ThriftClientCallContext) {
            if (!hasScope(trace)) {
                // not vertx trace.
                return;
            }

            if (!leaveScope(trace)) {
                if (logger.isInfoEnabled()) {
                    logger.info("Failed to leave scope. trace={}, sampled={}", trace, trace.canSampled());
                }
                // delete unstable trace.
                deleteTrace(trace);
                return;
            }

            if (!isEndScope(trace)) {
                // ignored recursive call.
                return;
            }

            if (!trace.canSampled()) {
                deleteTrace(trace);
                return;
            }

            try {
                final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordException(throwable);
                recorder.recordApi(this.descriptor);
            } catch (Throwable t) {
                logger.warn("AFTER. Cause:{}", t.getMessage(), t);
            } finally {
                trace.traceBlockEnd();
                deleteTrace(trace);
            }
        }
    }

    private boolean validate(Object target) {
        if (!(target instanceof TProtocol)) {
            return false;
        }
        if (!(target instanceof ServerMarkerFlagFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", ServerMarkerFlagFieldAccessor.class.getName());
            }
            return false;
        }
        TTransport transport = ((TProtocol) target).getTransport();
        if (!(transport instanceof SocketFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", SocketFieldAccessor.class.getName());
            }
            return false;
        }
        final boolean shouldTrace = ((ServerMarkerFlagFieldAccessor) target)._$PINPOINT$_getServerMarkerFlag();
        if (Boolean.FALSE == shouldTrace) {
            return false;
        }

        return true;
    }

    private Trace createTrace(Object target, ThriftRequestProperty parentTraceInfo, String methodName) {
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
                recordRootSpan(trace, parentTraceInfo, target, methodName);
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
                recordRootSpan(trace, parentTraceInfo, target, methodName);
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

    private void recordRootSpan(final Trace trace, final ThriftRequestProperty parentTraceInfo, Object target, String methodName) {
        // begin root span
        SpanRecorder recorder = trace.getSpanRecorder();
        recorder.recordServiceType(ThriftConstants.THRIFT_SERVER);
        recorder.recordApi(this.thriftServerEntryMethodDescriptor);
        if (!trace.isRoot()) {
            recordParentInfo(recorder, parentTraceInfo);
        }
        // record connection information here as the socket may be closed by the time the Span is popped in
        // TBaseAsyncProcessorProcessInterceptor's after section.
        TTransport transport = ((TProtocol) target).getTransport();
        recordConnection(recorder, transport, methodName);
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
        short flags = parentTraceInfo.getFlags((short) 0);

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

    private void recordConnection(SpanRecorder recorder, TTransport transport, String methodName) {
        // retrieve connection information
        String localIpPort = ThriftConstants.UNKNOWN_ADDRESS;
        String remoteAddress = ThriftConstants.UNKNOWN_ADDRESS;
        Socket socket = ((SocketFieldAccessor) transport)._$PINPOINT$_getSocket();
        if (socket != null) {
            localIpPort = ThriftUtils.getIpPort(socket.getLocalSocketAddress());
            remoteAddress = ThriftUtils.getIp(socket.getRemoteSocketAddress());
        }
        if (localIpPort != ThriftConstants.UNKNOWN_ADDRESS) {
            recorder.recordEndPoint(localIpPort);
        }
        if (remoteAddress != ThriftConstants.UNKNOWN_ADDRESS) {
            recorder.recordRemoteAddress(remoteAddress);
        }
        recorder.recordRpcName(methodName);
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }

    private boolean initScope(final Trace trace) {
        // add user scope.
        final TraceScope oldScope = trace.addScope(SCOPE_NAME);
        if (oldScope != null) {
            // delete corrupted trace.
            if (logger.isInfoEnabled()) {
                logger.info("Duplicated trace scope={}.", oldScope.getName());
            }
            return false;
        }

        return true;
    }

    private void entryScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            scope.tryEnter();
            if (isDebug) {
                logger.debug("Try enter trace scope={}", scope.getName());
            }
        }
    }

    private boolean leaveScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
                if (isDebug) {
                    logger.debug("Leave trace scope={}", scope.getName());
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean hasScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        return scope != null;
    }

    private boolean isEndScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        return scope != null && !scope.isActive();
    }
}
