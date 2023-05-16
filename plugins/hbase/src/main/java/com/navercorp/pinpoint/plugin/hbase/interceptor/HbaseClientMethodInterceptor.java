/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;
import org.apache.hadoop.hbase.client.ClientScanner;
import org.apache.hadoop.hbase.client.Result;

import java.net.InetSocketAddress;

/**
 * The type Hbase client method interceptor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2018/10/12
 */
public class HbaseClientMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    /**
     * Instantiates a new Hbase client method interceptor.
     *
     * @param traceContext the trace context
     * @param descriptor   the descriptor
     */
    public HbaseClientMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(HbasePluginConstants.HBASE_CLIENT);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        String endPoint = getEndPoint(args);
        recorder.recordEndPoint(endPoint != null ? endPoint : HbasePluginConstants.UNKNOWN_TABLE);
        recorder.recordDestinationId(HbasePluginConstants.HBASE_DESTINATION_ID);
        recorder.recordApi(getMethodDescriptor());
        if (target instanceof ClientScanner) {
            recorder.recordAttribute(HbasePluginConstants.HBASE_SCAN_RPC_RESULT_NUM, getScanRpcResultNum(result));
        }
        recorder.recordException(throwable);
    }

    private int getScanRpcResultNum(Object result) {
        if (result instanceof Result[]) {
            return ((Result[]) result).length;
        }
        return 0;
    }

    /**
     * Gets end point.
     *
     * @param args the args
     * @return the end point
     */
    protected String getEndPoint(Object[] args) {
        // call(PayloadCarryingRpcController pcrc, MethodDescriptor md,
        //      Message param, Message returnType, User ticket, InetSocketAddress addr,
        //      MetricsConnection.CallStats callStats)
        if (ArrayUtils.getLength(args) == 7) {
            InetSocketAddress socketAddress = ArrayArgumentUtils.getArgument(args, 5, InetSocketAddress.class);
            if (socketAddress != null) {
                return SocketAddressUtils.getHostNameFirst(socketAddress);
            }
        }
        return null;
    }

}
