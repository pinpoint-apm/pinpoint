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
package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.elasticsearchbboss.ElasticsearchConstants;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchOperationInterceptor extends ElasticsearchBaseOperationInterceptor {
    protected boolean recordResult = false;
    protected boolean recordArgs = false;
    public ElasticsearchOperationInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
        recordResult = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordResult",false);
        recordArgs = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordArgs",true);
    }


    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(ElasticsearchConstants.ELASTICSEARCH);
        recorder.recordException(throwable);
        if (recordArgs && args != null && args.length > 0) {
            recorder.recordApi(getMethodDescriptor());
//            recorder.recordAttribute(ElasticsearchConstants.ARGS_ANNOTATION_KEY,convertParams(args));
        } else {
            recorder.recordApi(getMethodDescriptor());
        }

        if(recordResult){
            recorder.recordAttribute(AnnotationKey.RETURN_DATA,result);
        }
    }

}
