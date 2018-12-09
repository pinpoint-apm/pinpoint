/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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

package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.elasticsearchbboss.ElasticsearchPlugin;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.AnnotationKey;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchOperationInterceptor extends ElasticsearchBaseOperationInterceptor {
    private boolean recordResult = false;
    private boolean recordArgs = false;

    public ElasticsearchOperationInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
        recordResult = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordResult",false);
        recordArgs = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordArgs",true);
    }

    @Override
    public void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args, boolean newTrace) {
        if(newTrace) {
            StringBuilder buffer = new StringBuilder(256);
            buffer.append(className);
            buffer.append(".");
            buffer.append(methodDescriptor.getMethodName());
            mergeParameterVariableNameDescription(buffer,methodDescriptor.getParameterTypes(),methodDescriptor.getParameterVariableName());
            String rpc = buffer.toString();//builder.append();
            recorder.recordRpcName(rpc);
            recorder.recordEndPoint(rpc);
            //recorder.recordRemoteAddress(rpc);
        }
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args,boolean newTrace) {

    }


    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
            Throwable throwable,boolean newTrace) {

        doInAfterTrace( new SpanEventRecorderWraper( recorder),   target,   args,   result,   throwable,ElasticsearchPlugin.ELASTICSEARCH_EVENT);
    }

    @Override
    public void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        doInAfterTrace( new SpanRecorderWraper( recorder),   target,   args,   result,   throwable,ElasticsearchPlugin.ELASTICSEARCH);
    }

    private void doInAfterTrace(RecorderWraper recorder, Object target, Object[] args, Object result, Throwable throwable, ServiceType serviceType) {
        recorder.recordServiceType(serviceType);
        recorder.recordException(throwable);
        if (recordArgs && args != null && args.length > 0) {
            recorder.recordApi(getMethodDescriptor());
//            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ANNOTATION_KEY,convertParams(args));
        } else {
            recorder.recordApi(getMethodDescriptor());
        }

        if(recordResult){
            recorder.recordAttribute(AnnotationKey.RETURN_DATA,result);
        }
    }

}
