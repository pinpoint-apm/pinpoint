/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.mybatis.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.mybatis.MyBatisConstants;
import com.navercorp.pinpoint.plugin.mybatis.MyBatisPluginConfig;


/**
 * @author HyunGil Jeong
 */
public class SqlSessionOperationInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final boolean markError;

    public SqlSessionOperationInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
        MyBatisPluginConfig config = new MyBatisPluginConfig(context.getProfilerConfig());
        this.markError = config.isMarkError();
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        // do nothing
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
                                  Throwable throwable) {
        recorder.recordServiceType(MyBatisConstants.MYBATIS);
        recorder.recordException(markError, throwable);
        final String arg = ArrayArgumentUtils.getArgument(args, 0, String.class);
        if (arg != null) {
            recorder.recordApiCachedString(getMethodDescriptor(), arg, 0);
        } else {
            recorder.recordApi(getMethodDescriptor());
        }
    }

}
