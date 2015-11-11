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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Future;

import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue1Utils;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue2Utils;
import com.navercorp.pinpoint.common.ServiceType;

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;
import net.spy.memcached.plugin.FrontCacheGetFuture;

/**
 * @author emeroad
 */
public class ApiInterceptor extends SpanEventSimpleAroundInterceptor implements ParameterExtractorSupport, TargetClassLoader {

    private ParameterExtractor parameterExtractor;

    public ApiInterceptor() {
        super(ApiInterceptor.class);
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, final Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {

        if (parameterExtractor != null) {
            final int index = parameterExtractor.getIndex();
            final Object recordObject = parameterExtractor.extractObject(args);
            trace.recordApi(getMethodDescriptor(), recordObject, index);
        } else {
            trace.recordApi(getMethodDescriptor());
        }

        // find the target node
        if (result instanceof Future && !(result instanceof FrontCacheGetFuture)) {
            final Operation op = getOperation(result);
            if (op != null) {
                final MemcachedNode handlingNode = op.getHandlingNode();
                if (handlingNode != null) {
                    final String endPoint = getEndPoint(handlingNode);
                    if (endPoint != null) {
                        trace.recordEndPoint(endPoint);
                    }
                }
            } else {
                logger.info("operation not found");
            }
        }

        // determine the service type
        final String serviceCode = getServiceCode(target);
        if (serviceCode != null) {
            trace.recordDestinationId(serviceCode);
            trace.recordServiceType(ServiceType.ARCUS);
        } else {
            trace.recordDestinationId("MEMCACHED");
            trace.recordServiceType(ServiceType.MEMCACHED);
        }

        trace.markAfterTime();
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
            return hostAddress + ":" + inetSocketAddress.getPort();

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

    private Operation getOperation(Object result) {
//        __operation -> ObjectTraceValue1.class
        final Object operationObject = ObjectTraceValue1Utils.__getTraceObject1(result, null);
        if (operationObject instanceof Operation) {
            return (Operation) operationObject;
        }
        return null;
    }

    private String getServiceCode(Object target) {
        final Object serviceCodeObject = ObjectTraceValue2Utils.__getTraceObject2(target, null);
        if (serviceCodeObject instanceof String) {
            return (String) serviceCodeObject;
        }
        return null;
    }

    @Override
    public void setParameterExtractor(ParameterExtractor parameterExtractor) {
        this.parameterExtractor = parameterExtractor;
    }
}
