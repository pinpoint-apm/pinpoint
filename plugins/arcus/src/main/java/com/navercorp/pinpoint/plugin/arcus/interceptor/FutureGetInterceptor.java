/*
 * Copyright 2014 NAVER Corp.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;
import com.navercorp.pinpoint.plugin.arcus.OperationAccessor;
import com.navercorp.pinpoint.plugin.arcus.ServiceCodeAccessor;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class FutureGetInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public FutureGetInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordDestinationId("MEMCACHED");
        recorder.recordServiceType(ArcusConstants.MEMCACHED_FUTURE_GET);

        if (!(target instanceof OperationAccessor)) {
            logger.info("operation not found");
            return;
        }

        // find the target node
        final Operation op = ((OperationAccessor) target)._$PINPOINT$_getOperation();
        if (op == null) {
            logger.info("operation is null");
            return;
        }

        recorder.recordException(op.getException());

        final MemcachedNode handlingNode = op.getHandlingNode();
        if (handlingNode != null) {
            final String endPoint = getEndPoint(handlingNode);
            if (endPoint != null) {
                recorder.recordEndPoint(endPoint);
            }
            recorder.recordException(op.getException());
        } else {
            logger.info("no handling node");
        }

        if (op instanceof ServiceCodeAccessor) {
            // determine the service type
            String serviceCode = ((ServiceCodeAccessor) op)._$PINPOINT$_getServiceCode();
            if (serviceCode != null) {
                recorder.recordDestinationId(serviceCode);
                recorder.recordServiceType(ArcusConstants.ARCUS_FUTURE_GET);
            }
        }
    }

    private String getEndPoint(MemcachedNode handlingNode) {
        // TODO duplicated code : ApiInterceptor, FutureGetInterceptor
        final SocketAddress socketAddress = handlingNode.getSocketAddress();
        if (socketAddress instanceof InetSocketAddress) {
            final InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
            final String hostAddress = getHostAddress(inetSocketAddress);
            if (hostAddress == null) {
                // TODO return "Unknown Host";
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
}