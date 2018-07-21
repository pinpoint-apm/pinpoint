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

package com.navercorp.pinpoint.plugin.elasticsearch.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.elasticsearch.ElasticsearchPlugin;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchExecutorOperationInterceptor extends ElasticsearchBaseOperationInterceptor {
    private boolean recordResult = false;
    private boolean recordArgs = false;

    public ElasticsearchExecutorOperationInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
        recordResult = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearch.recordResult",false);
        recordArgs = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearch.recordArgs",true);
    }

    @Override
    protected void doInBeforeTrace(SpanRecorder recorder, Object target, Object[] args, boolean newTrace) {
        if(newTrace) {
            StringBuilder buffer = new StringBuilder(256);

            buffer.append("ElasticsearchExecutor.");
            buffer.append(methodDescriptor.getMethodName());
            String rpc = buffer.toString();//builder.append();
            recorder.recordRpcName(rpc);
            recorder.recordEndPoint(rpc);
            //recorder.recordRemoteAddress(rpc);
        }
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args,boolean newTrace) {

    }

    public String convertParams(Object[] args){
        if(args != null && args.length > 0){
            StringBuilder builder = new StringBuilder();
            for(Object arg:args) {
                boolean isArray = arg != null && arg.getClass().isArray();


                if(builder.length() > 0) {
                    builder.append(",");

                }
                if(!isArray) {
                    builder.append(arg);
                }
                else{
                    convertArray(  arg,  builder);
                }
            }
            return builder.toString();
        }
        return null;
    }

    public void convertArray(Object arg,StringBuilder builder){
       {
            builder.append("[");
            Object[] fields = (Object[])arg;
            boolean isfirst = true;
            for(Object f:fields){
                if(isfirst){
                    isfirst = false;
                }
                else{
                    builder.append(",");

                }
                builder.append(f);
            }
            builder.append("]");
        }

    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
            Throwable throwable,boolean newTrace) {
        recorder.recordServiceType(ElasticsearchPlugin.ELASTICSEARCH_EXECUTOR);
        recorder.recordException(throwable);
        if (recordArgs && args != null && args.length > 0) {
            //recorder.recordApiCachedString(getMethodDescriptor(),convertParams(args),0);
            MethodDescriptor methodDescriptor = getMethodDescriptor();
            recordAttributes(  recorder,   methodDescriptor,  args);
        } else {
            recorder.recordApi(getMethodDescriptor());
        }

        if(recordResult){
        	recorder.recordAttribute(AnnotationKey.RETURN_DATA,result);
		}
    }

    private void recordAttributes(SpanEventRecorder recorder, MethodDescriptor methodDescriptor,Object[] args){
        if(methodDescriptor.getMethodName().equals("execute")) {
            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, "POST");
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);
//                recorder.recordDestinationId(String.valueOf(args[0]));
        }
        else if(methodDescriptor.getMethodName().equals("executeHttp")) {
            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, args[2]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);
//                recorder.recordDestinationId(String.valueOf(args[0]));
        }
        else if(methodDescriptor.getMethodName().equals("executeSimpleRequest")) {
            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, "POST");
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);

        }
        else if(methodDescriptor.getMethodName().equals("executeRequest")) {
            recorder.recordApi(getMethodDescriptor());
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, args[2] );
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);

        }
    }

    private void recordAttributes(SpanRecorder recorder, MethodDescriptor methodDescriptor,Object[] args){
        if(methodDescriptor.getMethodName().equals("execute")) {
            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);
//                recorder.recordDestinationId(String.valueOf(args[0]));
        }
        else if(methodDescriptor.getMethodName().equals("executeHttp")) {
            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, args[2]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);
//                recorder.recordDestinationId(String.valueOf(args[0]));
        }
        else if(methodDescriptor.getMethodName().equals("executeSimpleRequest")) {
            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, "POST");
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);

        }
        else if(methodDescriptor.getMethodName().equals("executeRequest")) {
            recorder.recordApi(getMethodDescriptor());
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, args[2] );
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);

        }
    }
    @Override
    public void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(ElasticsearchPlugin.ELASTICSEARCH_EXECUTOR);
        recorder.recordException(throwable);
        if (recordArgs && args != null && args.length > 0) {
            //recorder.recordApiCachedString(getMethodDescriptor(),convertParams(args),0);
            recorder.recordApi(getMethodDescriptor());
//            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ANNOTATION_KEY,convertParams(args));
            recordAttributes(  recorder,   methodDescriptor,  args);
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));

        } else {
            recorder.recordApi(getMethodDescriptor());
        }

        if(recordResult){
            recorder.recordAttribute(AnnotationKey.RETURN_DATA,result);
        }
    }

}
