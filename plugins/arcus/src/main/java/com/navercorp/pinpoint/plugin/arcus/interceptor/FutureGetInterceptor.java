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

import net.spy.memcached.MemcachedNode;
import net.spy.memcached.ops.Operation;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanAsyncEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.arcus.ArcusConstants;
import com.navercorp.pinpoint.plugin.arcus.OperationAccessor;
import com.navercorp.pinpoint.plugin.arcus.ServiceCodeAccessor;

/**
 * @author emeroad
 * @author jaehong.kim
 */
@Group(ArcusConstants.ARCUS_FUTURE_SCOPE)
public class FutureGetInterceptor extends SpanAsyncEventSimpleAroundInterceptor implements ArcusConstants {

    public FutureGetInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncTraceId asyncTraceId, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        // find the target node
        final Operation op = ((OperationAccessor)target)._$PINPOINT$_getOperation();
        if (op != null) {
            MemcachedNode handlingNode = op.getHandlingNode();
            if (handlingNode != null) {
                SocketAddress socketAddress = handlingNode.getSocketAddress();
                if (socketAddress instanceof InetSocketAddress) {
                    InetSocketAddress address = (InetSocketAddress) socketAddress;
                    recorder.recordEndPoint(address.getHostName() + ":" + address.getPort());
                }
            } else {
                logger.info("no handling node");
            }
        } else {
            logger.info("operation not found");
        }

        // determine the service type
        String serviceCode = ((ServiceCodeAccessor)op)._$PINPOINT$_getServiceCode();
        if (serviceCode != null) {
            recorder.recordDestinationId(serviceCode);
            recorder.recordServiceType(ARCUS_FUTURE_GET);
        } else {
            recorder.recordDestinationId("MEMCACHED");
            recorder.recordServiceType(ServiceType.MEMCACHED_FUTURE_GET);
        }

        if (op != null) {
            recorder.recordException(op.getException());
        }
        recorder.recordApi(methodDescriptor);
    }
}
