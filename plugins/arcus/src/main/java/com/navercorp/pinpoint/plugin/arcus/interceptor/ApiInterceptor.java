/*
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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;
import com.navercorp.pinpoint.plugin.arcus.OperationAccessor;
import com.navercorp.pinpoint.plugin.arcus.ServiceCodeAccessor;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class ApiInterceptor implements AroundInterceptor {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final MethodDescriptor methodDescriptor;
    protected final TraceContext traceContext;

    private final boolean traceKey;
    private final int keyIndex;

    public ApiInterceptor(TraceContext context, MethodDescriptor targetMethod, boolean traceKey) {
        this.traceContext = context;
        this.methodDescriptor = targetMethod;

        if (traceKey) {
            int index = findFirstString(targetMethod);

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
    }

    private static int findFirstString(MethodDescriptor method) {
        if (method == null) {
            return -1;
        }
        final String[] methodParams = method.getParameterTypes();
        final int minIndex = Math.min(methodParams.length, 3);
        for (int i = 0; i < minIndex; i++) {
            if ("java.lang.String".equals(methodParams[i])) {
                return i;
            }
        }
        return -1;
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
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
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
            if (result instanceof Future && result instanceof OperationAccessor) {
                final Operation op = ((OperationAccessor)result)._$PINPOINT$_getOperation();
                if (op != null) {
                    final MemcachedNode handlingNode = op.getHandlingNode();
                    if (handlingNode != null) {
                        final String endPoint = getEndPoint(handlingNode);
                        if (endPoint != null) {
                            recorder.recordEndPoint(endPoint);
                        }
                    }
                } else {
                    logger.info("operation not found");
                }
            }

            if (target instanceof ServiceCodeAccessor) {
                // determine the service type
                String serviceCode = ((ServiceCodeAccessor)target)._$PINPOINT$_getServiceCode();
                if (serviceCode != null) {
                    recorder.recordDestinationId(serviceCode);
                    recorder.recordServiceType(ArcusConstants.ARCUS);
                } else {
                    recorder.recordDestinationId("MEMCACHED");
                    recorder.recordServiceType(ArcusConstants.MEMCACHED);
                }
            } else {
                recorder.recordDestinationId("MEMCACHED");
                recorder.recordServiceType(ArcusConstants.MEMCACHED);
            }

            try {
                if (isAsynchronousInvocation(target, args, result, throwable)) {
                    // set asynchronous trace
                    final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                    // type check isAsynchronousInvocation
                    ((AsyncContextAccessor)result)._$PINPOINT$_setAsyncContext(asyncContext);
                    if (isDebug) {
                        logger.debug("Set AsyncContext {}", asyncContext);
                    }
                }
            } catch (Throwable t) {
                logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private String getEndPoint(MemcachedNode handlingNode) {
        // TODO duplicated code : ApiInterceptor, FutureGetInterceptor
        final SocketAddress socketAddress = handlingNode.getSocketAddress();
        if (socketAddress instanceof InetSocketAddress) {
            final InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            final String hostAddress = getHostAddress(inetSocketAddress);
            if (hostAddress == null) {
                // TODO return "Unknown Host"; ?
                logger.debug("hostAddress is null");
                return null;
            }
            return HostAndPort.toHostAndPortString(hostAddress, inetSocketAddress.getPort());

        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("invalid socketAddress:{}", socketAddress);
            }
            return null;
        }
    }

    private String getHostAddress(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return null;
        }
        // TODO JDK 1.7 InetSocketAddress.getHostString();
        // Warning : Avoid unnecessary DNS lookup  (warning:InetSocketAddress.getHostName())
        final InetAddress inetAddress = inetSocketAddress.getAddress();
        if (inetAddress == null) {
            return null;
        }
        return inetAddress.getHostAddress();
    }

    private boolean isAsynchronousInvocation(final Object target, final Object[] args, Object result, Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!(result instanceof AsyncContextAccessor)) {
            logger.debug("Invalid result object. Need accessor({}).", AsyncContextAccessor.class.getName());
            return false;
        }

        return true;
    }
}
