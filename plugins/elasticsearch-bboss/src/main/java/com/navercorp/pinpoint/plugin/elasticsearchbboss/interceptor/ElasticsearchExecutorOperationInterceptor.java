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
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.elasticsearchbboss.ClusterVersionAccessor;
import com.navercorp.pinpoint.plugin.elasticsearchbboss.ElasticsearchConstants;
import com.navercorp.pinpoint.plugin.elasticsearchbboss.ElasticsearchPluginConfig;

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
    private volatile Method getClusterVersionInfo;
    private int maxDslSize;

    public ElasticsearchExecutorOperationInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
        final ElasticsearchPluginConfig elasticsearchPluginConfig = new ElasticsearchPluginConfig(context.getProfilerConfig());
        recordResult = elasticsearchPluginConfig.isRecordResult();
        recordArgs = elasticsearchPluginConfig.isRecordArgs();
        recordDsl = elasticsearchPluginConfig.isRecordDsl();
        maxDslSize = elasticsearchPluginConfig.getMaxDslSize();
        recordResponseHandler = elasticsearchPluginConfig.isRecordResponseHandler();
        recordESVersion = elasticsearchPluginConfig.isRecordESVersion();


    }

    @Override
    public void before(Object target, Object[] args) {
        super.before(target, args);

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {


        super.after(target, args, result, throwable);
    }


    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {

        recorder.recordServiceType(ElasticsearchConstants.ELASTICSEARCH_EXECUTOR);
    }

    private String getClusterVersionInfo(Object target) {
        if (target instanceof ClusterVersionAccessor) {
            ClusterVersionAccessor clusterVersionAccessor = (ClusterVersionAccessor) target;
            if (clusterVersionAccessor._$PINPOINT$_getClusterVersion() != null) {
                return clusterVersionAccessor._$PINPOINT$_getClusterVersion();
            } else {
                synchronized (target.getClass()) {
                    if (clusterVersionAccessor._$PINPOINT$_getClusterVersion() == null) {
                        try {
                            Method _getClusterVersionInfo = target.getClass().getMethod("getClusterVersionInfo");
                            String version = (String) _getClusterVersionInfo.invoke(target);
                            if (version == null) {
                                clusterVersionAccessor._$PINPOINT$_setClusterVersion("UNKNOWN_VERSION");
                            } else {
                                clusterVersionAccessor._$PINPOINT$_setClusterVersion(version);
                            }

                        } catch (Exception e) {
                            clusterVersionAccessor._$PINPOINT$_setClusterVersion("UNKNOWN_VERSION");
                        }
                    }

                }
                return clusterVersionAccessor._$PINPOINT$_getClusterVersion();
            }
        } else {
            if (getClusterVersionInfo == null) {
                synchronized (target.getClass()) {
                    if (getClusterVersionInfo == null) {
                        try {
                            getClusterVersionInfo = target.getClass().getMethod("getClusterVersionInfo");
                        } catch (Exception e) {

                        }
                    }
                }
            }
            if (getClusterVersionInfo != null) {
                try {
                    String version = (String) getClusterVersionInfo.invoke(target);
                    if (version == null) {
                        return "UNKNOWN_VERSION";
                    } else {
                        return version;
                    }

                } catch (Exception e) {
                    return "UNKNOWN_VERSION";
                }
            } else {
                return "UNKNOWN_VERSION";
            }
        }
    }

    private String getEndPoint(Object[] args) {
        String url = (String) args[0];
        //http://xxx:9200/
        int idx = url.indexOf("://");
        String endPoint = null;
        if (idx > 0) {
            int sub = url.indexOf('/', idx + 3);
            if (sub > 0) {
                endPoint = url.substring(idx + 3, sub);
            } else {
                endPoint = url.substring(idx + 3);
            }
        } else {
            endPoint = "Unknown";
        }
        return endPoint;

    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
                                  Throwable throwable) {

        recorder.recordApi(getMethodDescriptor());
        recorder.recordDestinationId("ElasticsearchBBoss");
        if (ArrayUtils.hasLength(args)) {
            recorder.recordEndPoint(getEndPoint(args));
        }
        if (recordESVersion) {
            // Each target(ClientInstance) has a specific version of Elasticsearch Datasource in one application,
            // and each Elasticsearch Datasource retains its corresponding Elasticsearch cluster version information
            // such as Elasticsearch 1.x or 2.x or 5.x or 6.x or 7.x or 8.x and so on.
            // so we should get elasticsearchClusterVersionInfo in target everytime.
            String elasticsearchClusterVersionInfo = getClusterVersionInfo(target);
            recorder.recordAttribute(ElasticsearchConstants.ARGS_VERSION_ANNOTATION_KEY, elasticsearchClusterVersionInfo);//record elasticsearch version and cluster name.
        }
        recorder.recordException(throwable);
        if (recordArgs && ArrayUtils.hasLength(args)) {
            recordAttributes(recorder, methodDescriptor, args);
        }

        if (recordResult) {
            recorder.recordAttribute(AnnotationKey.RETURN_DATA, result);
        }
    }


    private void recordAttributes(SpanEventRecorder recorder, MethodDescriptor methodDescriptor, Object[] args) {
        if (methodDescriptor.getMethodName().equals("execute")) {

            recorder.recordAttribute(ElasticsearchConstants.ARGS_URL_ANNOTATION_KEY, args[0]);
            if (recordDsl)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, chunkDsl((String) args[1]));
            recorder.recordAttribute(ElasticsearchConstants.ARGS_ACTION_ANNOTATION_KEY, "POST");
            if (recordResponseHandler)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);
        } else if (methodDescriptor.getMethodName().equals("executeHttp")) {
            recorder.recordAttribute(ElasticsearchConstants.ARGS_URL_ANNOTATION_KEY, args[0]);
            if (recordDsl)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, chunkDsl((String) args[1]));
            recorder.recordAttribute(ElasticsearchConstants.ARGS_ACTION_ANNOTATION_KEY, args[2]);
            if (recordResponseHandler)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);
        } else if (methodDescriptor.getMethodName().equals("executeSimpleRequest")) {
            recorder.recordAttribute(ElasticsearchConstants.ARGS_URL_ANNOTATION_KEY, args[0]);
            if (recordDsl)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, chunkDsl((String) args[1]));
            recorder.recordAttribute(ElasticsearchConstants.ARGS_ACTION_ANNOTATION_KEY, "POST");
            if (recordResponseHandler)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[2]);

        } else if (methodDescriptor.getMethodName().equals("executeRequest")) {
            recorder.recordAttribute(ElasticsearchConstants.ARGS_URL_ANNOTATION_KEY, args[0]);
            if (recordDsl)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, chunkDsl((String) args[1]));
            recorder.recordAttribute(ElasticsearchConstants.ARGS_ACTION_ANNOTATION_KEY, args[2]);
            if (recordResponseHandler)
                recorder.recordAttribute(ElasticsearchConstants.ARGS_RESPONSEHANDLE_ANNOTATION_KEY, args[3]);

        }
    }

    private String chunkDsl(String dsl) {
        if (dsl == null) {
            return null;
        }
        if (dsl.length() <= maxDslSize || maxDslSize <= 0) {
            return dsl;
        } else {
            return dsl.substring(0, maxDslSize);
        }
    }


}
