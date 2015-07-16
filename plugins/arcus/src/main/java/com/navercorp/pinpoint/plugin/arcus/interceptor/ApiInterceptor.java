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
package com.navercorp.pinpoint.plugin.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;
import com.navercorp.pinpoint.plugin.arcus.ParameterUtils;

/**
 * @author emeroad
 * @author jaehong.kim
 */
@Group(ArcusConstants.ARCUS_SCOPE)
public class ApiInterceptor implements SimpleAroundInterceptor, ArcusConstants {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final MethodDescriptor methodDescriptor;
    protected final TraceContext traceContext;

    private final MetadataAccessor serviceCodeAccessor;
    private final MetadataAccessor operationAccessor;
    private final MetadataAccessor asyncTraceIdAccessor;

    private final boolean traceKey;
    private final int keyIndex;

    public ApiInterceptor(TraceContext context, MethodInfo targetMethod, @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor, @Name(METADATA_SERVICE_CODE) MetadataAccessor serviceCodeAccessor,
            @Name(METADATA_OPERATION) MetadataAccessor operationAccessor, boolean traceKey) {

        this.traceContext = context;
        this.methodDescriptor = targetMethod.getDescriptor();

        if (traceKey) {
            int index = ParameterUtils.findFirstString(targetMethod, 3);

            if (index != -1) {
                this.traceKey = true;
                this.keyIndex = index;
            } else {
                this.traceKey = false;
                this.keyIndex = -1;
            }
        } else {
            this.traceKey = false;
            this.keyIndex = -1;
        }

        this.serviceCodeAccessor = serviceCodeAccessor;
        this.operationAccessor = operationAccessor;
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            trace.traceBlockBegin();
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("before. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (traceKey) {
                final Object recordObject = args[keyIndex];
                recorder.recordApi(methodDescriptor, recordObject, keyIndex);
            } else {
                recorder.recordApi(methodDescriptor);
            }
            recorder.recordException(throwable);

            // find the target node
            if (result instanceof Future && operationAccessor.isApplicable(result)) {
                Operation op = operationAccessor.get(result);

                if (op != null) {
                    MemcachedNode handlingNode = op.getHandlingNode();
                    SocketAddress socketAddress = handlingNode.getSocketAddress();

                    if (socketAddress instanceof InetSocketAddress) {
                        InetSocketAddress address = (InetSocketAddress) socketAddress;
                        recorder.recordEndPoint(address.getHostName() + ":" + address.getPort());
                    }
                } else {
                    logger.info("operation not found");
                }
            }

            if (serviceCodeAccessor.isApplicable(target)) {
                // determine the service type
                String serviceCode = serviceCodeAccessor.get(target);
                if (serviceCode != null) {
                    recorder.recordDestinationId(serviceCode);
                    recorder.recordServiceType(ARCUS);
                } else {
                    recorder.recordDestinationId("MEMCACHED");
                    recorder.recordServiceType(ServiceType.MEMCACHED);
                }
            } else {
                recorder.recordDestinationId("MEMCACHED");
                recorder.recordServiceType(ServiceType.MEMCACHED);
            }

            try {
                if (isAsynchronousInvocation(target, args, result, throwable)) {
                    // set asynchronous trace
                    this.traceContext.getAsyncId();
                    final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
                    recorder.recordNextAsyncId(asyncTraceId.getAsyncId());
                    asyncTraceIdAccessor.set(result, asyncTraceId);
                    if (isDebug) {
                        logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
                    }
                }
            } catch (Throwable t) {
                logger.warn("Failed to before process. {}", t.getMessage(), t);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean isAsynchronousInvocation(final Object target, final Object[] args, Object result, Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!asyncTraceIdAccessor.isApplicable(result)) {
            logger.debug("Invalid result object. Need metadata accessor({}).", METADATA_ASYNC_TRACE_ID);
            return false;
        }

        return true;
    }
}
