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
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;

import java.util.Arrays;

/**
 * The type Hbase admin method interceptor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2018/10/12
 */
public class HbaseAdminMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final boolean paramsProfile;

    /**
     * Instantiates a new Hbase admin method interceptor.
     *
     * @param traceContext  the trace context
     * @param descriptor    the descriptor
     * @param paramsProfile
     */
    public HbaseAdminMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor, boolean paramsProfile) {
        super(traceContext, descriptor);
        this.paramsProfile = paramsProfile;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(HbasePluginConstants.HBASE_CLIENT_ADMIN);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (paramsProfile) {
            String attributes = parseAttributes(args);
            if (attributes != null)
                recorder.recordAttribute(HbasePluginConstants.HBASE_CLIENT_PARAMS, attributes);
        }
        recorder.recordApi(getMethodDescriptor());
        recorder.recordException(throwable);
    }

    /**
     * Parse attributes string.
     *
     * @param args the args
     * @return the string
     */
    protected String parseAttributes(Object[] args) {

        if (args != null && args.length > 0) { // if has params, print all params.

            return Arrays.toString(args);
        }
        return null;
    }

}
