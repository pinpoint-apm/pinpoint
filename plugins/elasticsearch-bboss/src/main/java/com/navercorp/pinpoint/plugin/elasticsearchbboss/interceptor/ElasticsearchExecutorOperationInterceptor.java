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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.elasticsearchbboss.ElasticsearchPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchExecutorOperationInterceptor extends ElasticsearchBaseOperationInterceptor {
    private boolean recordResult = false;
    private boolean recordArgs = false;
    private Method getClusterVersionInfo;

    public ElasticsearchExecutorOperationInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
        recordResult = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordResult",false);
        recordArgs = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordArgs",true);
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

    private String getClusterVersionInfo(Object target){
        try {
            if(getClusterVersionInfo != null)
                return (String)getClusterVersionInfo.invoke(target);
            synchronized (this) {
                if(getClusterVersionInfo == null) {
                    Method _getClusterVersionInfo = target.getClass().getMethod("getClusterVersionInfo");
                    getClusterVersionInfo = _getClusterVersionInfo;
                }
            }
            return (String)getClusterVersionInfo.invoke(target);
        } catch (NoSuchMethodException e) {

        } catch (IllegalAccessException e) {

        } catch (InvocationTargetException e) {

        }
        return "";

    }
    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
            Throwable throwable,boolean newTrace) {
        String elasticsearchClusterVersionInfo = getClusterVersionInfo( target);
        recorder.recordServiceType(ElasticsearchPlugin.ELASTICSEARCH_EXECUTOR);
		recorder.recordApi(getMethodDescriptor());
        recorder.recordAttribute(ElasticsearchPlugin.ARGS_VERSION_ANNOTATION_KEY,elasticsearchClusterVersionInfo);//record elasticsearch version and cluster name.
//        recorder.recordApiCachedString(getMethodDescriptor(),elasticsearchClusterVersionInfo,0);
        recorder.recordException(throwable);
        if (recordArgs && args != null && args.length > 0) {
            //recorder.recordApiCachedString(getMethodDescriptor(),convertParams(args),0);

//            MethodDescriptor methodDescriptor = getMethodDescriptor();
            recordAttributes(  recorder,   methodDescriptor,  args);
        } else {
//            recorder.recordApi(getMethodDescriptor());
        }

        if(recordResult){
        	recorder.recordAttribute(AnnotationKey.RETURN_DATA,result);
		}
    }

    private void recordAttributes(SpanEventRecorder recorder, MethodDescriptor methodDescriptor,Object[] args){
        if(methodDescriptor.getMethodName().equals("execute")) {
//            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, "POST");
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);
//                recorder.recordDestinationId(String.valueOf(args[0]));
        }
        else if(methodDescriptor.getMethodName().equals("executeHttp")) {
//            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, args[2]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);
//                recorder.recordDestinationId(String.valueOf(args[0]));
        }
        else if(methodDescriptor.getMethodName().equals("executeSimpleRequest")) {
//            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, "POST");
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);

        }
        else if(methodDescriptor.getMethodName().equals("executeRequest")) {
//            recorder.recordApi(getMethodDescriptor());
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, args[2] );
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);

        }
    }

    private void recordAttributes(SpanRecorder recorder, MethodDescriptor methodDescriptor,Object[] args){
        if(methodDescriptor.getMethodName().equals("execute")) {
//            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);
//                recorder.recordDestinationId(String.valueOf(args[0]));
        }
        else if(methodDescriptor.getMethodName().equals("executeHttp")) {
//            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, args[2]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);
//                recorder.recordDestinationId(String.valueOf(args[0]));
        }
        else if(methodDescriptor.getMethodName().equals("executeSimpleRequest")) {
//            recorder.recordApi(getMethodDescriptor());
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, "POST");
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);

        }
        else if(methodDescriptor.getMethodName().equals("executeRequest")) {
//            recorder.recordApi(getMethodDescriptor());
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_URL_ANNOTATION_KEY, args[0]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_DSL_ANNOTATION_KEY, args[1]);
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ACTION_ANNOTATION_KEY, args[2] );
            recorder.recordAttribute(ElasticsearchPlugin.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);

        }
    }
    @Override
    public void doInAfterTrace(SpanRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(ElasticsearchPlugin.ELASTICSEARCH_EXECUTOR);
		recorder.recordApi(getMethodDescriptor());
        String elasticsearchClusterVersionInfo = getClusterVersionInfo( target);
        recorder.recordAttribute(ElasticsearchPlugin.ARGS_VERSION_ANNOTATION_KEY,elasticsearchClusterVersionInfo);//record elasticsearch version and cluster name.
        recorder.recordException(throwable);

//        recorder.recordApiCachedString(getMethodDescriptor(),elasticsearchClusterVersionInfo,0);
        if (recordArgs && args != null && args.length > 0) {
//            recorder.recordApiCachedString(getMethodDescriptor(),elasticsearchClusterVersionInfo,0);
//            recorder.recordApi(getMethodDescriptor());
//            recorder.recordAttribute(ElasticsearchPlugin.ARGS_ANNOTATION_KEY,convertParams(args));
            recordAttributes(  recorder,   methodDescriptor,  args);
            //recorder.recordAttribute(AnnotationKey.ARGS0,convertParams(args));

        } else {
//            recorder.recordApi(getMethodDescriptor());
//            recorder.recordApiCachedString(getMethodDescriptor(),elasticsearchClusterVersionInfo,0);
        }

        if(recordResult){
            recorder.recordAttribute(AnnotationKey.RETURN_DATA,result);
        }
    }

}
