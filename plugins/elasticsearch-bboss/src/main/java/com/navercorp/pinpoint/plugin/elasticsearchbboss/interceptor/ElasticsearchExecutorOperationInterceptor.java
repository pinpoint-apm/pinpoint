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
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.elasticsearchbboss.ElasticsearchConstants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchExecutorOperationInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private boolean recordResult = false;
    private boolean recordArgs = false;
    private boolean recordDsl = false;
    private boolean recordResponseHandler = false;
    private boolean recordESVersion = false;
    private Method getClusterVersionInfo;

    private int maxDslSize;

    public ElasticsearchExecutorOperationInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
        recordResult = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordResult",false);
        recordArgs = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordArgs",true);
        recordDsl =  this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordDsl",true);
        maxDslSize =  this.getTraceContext().getProfilerConfig().readInt("profiler.elasticsearchbboss.maxDslSize",ElasticsearchConstants.maxDslSize);
        recordResponseHandler =  this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordResponseHandlerClass",false);
        recordESVersion = this.getTraceContext().getProfilerConfig().readBoolean("profiler.elasticsearchbboss.recordESVersion",true);


    }
    @Override
    public void before(Object target, Object[] args) {
        super.before(target,args);

    }
    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable){


        super.after(target,args,result,throwable);
    }


    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {

        recorder.recordServiceType(ElasticsearchConstants.ELASTICSEARCH_EXECUTOR);
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
    private String getEndPoint(Object[] args){
        String url = (String)args[0];
        //http://xxx:9200/
        int idx = url.indexOf("://");
        String endPoint = null;
        if(idx > 0){
            int sub = url.indexOf('/',idx + 3);
            if(sub > 0 ){
                endPoint = url.substring(idx + 3,sub);
            }
            else
            {
                endPoint = url.substring(idx + 3);
            }
        }
        else{
            endPoint = "Unknown";
        }
        return endPoint;

    }
    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
            Throwable throwable) {
        String elasticsearchClusterVersionInfo = getClusterVersionInfo( target);

		recorder.recordApi(getMethodDescriptor());
//        String endPoint = getEndPoint(args);
//        recorder.recordEndPoint(endPoint != null ? endPoint : "Unknown");
//        recorder.recordDestinationId(HbasePluginConstants.HBASE_DESTINATION_ID);
//        recorder.recordApi(getMethodDescriptor());
        recorder.recordDestinationId("Elasticsearch");

        recorder.recordEndPoint(getEndPoint( args));
        if(recordESVersion)
            recorder.recordAttribute(ElasticsearchConstants.ARGS_VERSION_ANNOTATION_KEY,elasticsearchClusterVersionInfo);//record elasticsearch version and cluster name.
        recorder.recordException(throwable);
        if (recordArgs && args != null && args.length > 0) {
            recordAttributes(  recorder,   methodDescriptor,  args);
        }

        if(recordResult){
        	recorder.recordAttribute(AnnotationKey.RETURN_DATA,result);
		}
    }




    private void recordAttributes(SpanEventRecorder recorder, MethodDescriptor methodDescriptor,Object[] args){
        if(methodDescriptor.getMethodName().equals("execute")) {

            recorder.recordAttribute(ElasticsearchConstants.ARGS_URL_ANNOTATION_KEY, args[0]);
            if(recordDsl)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, chunkDsl((String)args[1]));
            recorder.recordAttribute(ElasticsearchConstants.ARGS_ACTION_ANNOTATION_KEY, "POST");
            if(recordResponseHandler)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);
        }
        else if(methodDescriptor.getMethodName().equals("executeHttp")) {
            recorder.recordAttribute(ElasticsearchConstants.ARGS_URL_ANNOTATION_KEY, args[0]);
            if(recordDsl)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, chunkDsl((String)args[1]));
            recorder.recordAttribute(ElasticsearchConstants.ARGS_ACTION_ANNOTATION_KEY, args[2]);
            if(recordResponseHandler)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);
        }
        else if(methodDescriptor.getMethodName().equals("executeSimpleRequest")) {
            recorder.recordAttribute(ElasticsearchConstants.ARGS_URL_ANNOTATION_KEY, args[0]);
            if(recordDsl)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, chunkDsl((String)args[1]));
            recorder.recordAttribute(ElasticsearchConstants.ARGS_ACTION_ANNOTATION_KEY, "POST");
            if(recordResponseHandler)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);

        }
        else if(methodDescriptor.getMethodName().equals("executeRequest")) {
            recorder.recordAttribute(ElasticsearchConstants.ARGS_URL_ANNOTATION_KEY, args[0]);
            if(recordDsl)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, chunkDsl((String)args[1]));
            recorder.recordAttribute(ElasticsearchConstants.ARGS_ACTION_ANNOTATION_KEY, args[2] );
            if(recordResponseHandler)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);

        }
    }
    private String chunkDsl(String dsl){
        if(dsl == null){
            return null;
        }
        if(dsl.length() <= maxDslSize || maxDslSize <= 0){
            return dsl;
        }
        else{
            return dsl.substring(0,maxDslSize);
        }
    }


}
