/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.modifier.arcus.interceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue1Utils;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue2Utils;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.ServiceType;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

/**
 * @author emeroad
 */
public class FutureGetInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport, TargetClassLoader {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private MethodDescriptor methodDescriptor;
    private TraceContext traceContext;

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();
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
            trace.recordApi(methodDescriptor);
//            Do not record because it's not important.
//            String annotation = "future.get() timeout:" + args[0] + " " + ((TimeUnit)args[1]).name();
//            trace.recordAttribute(AnnotationKey.ARCUS_COMMAND, annotation);

            // find the target node
            final Operation op = getOperation(target);
            if (op != null) {
                MemcachedNode handlingNode = op.getHandlingNode();
                if (handlingNode != null) {
                    SocketAddress socketAddress = handlingNode.getSocketAddress();
                    if (socketAddress instanceof InetSocketAddress) {
                        InetSocketAddress address = (InetSocketAddress) socketAddress;
                        trace.recordEndPoint(address.getHostName() + ":" + address.getPort());
                    }
                    trace.recordException(op.getException());
                } else {
                    logger.info("no handling node");
                }
            } else {
                logger.info("operation not found");
            }

            // determine the service type
            final String serviceCode = getServiceCode(op);
            if (serviceCode != null) {
                trace.recordDestinationId(serviceCode);
                trace.recordServiceType(ServiceType.ARCUS_FUTURE_GET);
            } else {
                trace.recordDestinationId("MEMCACHED");
                trace.recordServiceType(ServiceType.MEMCACHED_FUTURE_GET);
            }

//            When it's canceled, doen't it throw exception?
//            if (op.isCancelled()) {
//                trace.recordAttribute(AnnotationKey.EXCEPTION, "cancelled by user");
//            }

            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
        }
    }

//    __operation -> ObjectTraceValue1.class
    private Operation getOperation(Object target) {
        final Object operationObject = ObjectTraceValue1Utils.__getTraceObject1(target, null);
        if (operationObject instanceof Operation) {
            return (Operation) operationObject;
        }
        return null;
    }

//    __serviceCode -> ObjectTraceValue2.class
    private String getServiceCode(Object target) {
        final Object serviceCodeObject = ObjectTraceValue2Utils.__getTraceObject2(target, null);
        if (serviceCodeObject instanceof String) {
            return (String) serviceCodeObject;
        }
        return null;
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.methodDescriptor = descriptor;
        this.traceContext.cacheApi(descriptor);
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

}
