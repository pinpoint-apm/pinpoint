/*
 *  Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.elasticsearch.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.elasticsearch.ElasticsearchConstants;
import com.navercorp.pinpoint.plugin.elasticsearch.ElasticsearchPluginConfig;
import com.navercorp.pinpoint.plugin.elasticsearch.accessor.ClusterInfoAccessor;
import com.navercorp.pinpoint.plugin.elasticsearch.accessor.EndPointAccessor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;

/**
 * @author Roy Kim
 */
public class ElasticsearchExecutorInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private boolean recordDsl;
    private boolean recordESVersion;

    public ElasticsearchExecutorInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
        final ElasticsearchPluginConfig elasticsearchPluginConfig = new ElasticsearchPluginConfig(context.getProfilerConfig());
        recordDsl = elasticsearchPluginConfig.isRecordDsl();
        recordESVersion = elasticsearchPluginConfig.isRecordESVersion();
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {

        recorder.recordServiceType(ElasticsearchConstants.ELASTICSEARCH_EXECUTOR);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
                                  Throwable throwable) {

        recorder.recordApi(getMethodDescriptor());
        recorder.recordDestinationId("ElasticSearch");

        if (target instanceof EndPointAccessor) {
            String endPoint = ((EndPointAccessor) target)._$PINPOINT$_getEndPoint();
            recorder.recordEndPoint(endPoint);
        }

        recordeESattributes(recorder, target, args, result, throwable);
        recorder.recordException(throwable);
    }

    //TODO max dsl limit need
    private void recordeESattributes(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {

        if (recordESVersion) {
            if (target instanceof ClusterInfoAccessor) {
                //record elasticsearch version and cluster name.
                recorder.recordAttribute(ElasticsearchConstants.ARGS_VERSION_ANNOTATION_KEY, ((ClusterInfoAccessor) target)._$PINPOINT$_getClusterInfo());
            }
        }
        if (recordDsl) {

            if (args[0] instanceof SearchRequest) {
                SearchRequest request = (SearchRequest) args[0];
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, StringUtils.abbreviate(request.source().toString(), 256));
            } else if (args[0] instanceof GetRequest) {
//                GetRequest request = (GetRequest) args[0];
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, StringUtils.abbreviate(args[0].toString(), 256));
            } else if (args[0] instanceof IndexRequest) {
//                IndexRequest request = (IndexRequest) args[0];
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, StringUtils.abbreviate(args[0].toString(), 256));
            } else if (args[0] instanceof DeleteRequest) {
//                DeleteRequest request = (DeleteRequest) args[0];
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, StringUtils.abbreviate(args[0].toString(), 256));
            } else if (args[0] instanceof UpdateRequest) {
//                UpdateRequest request = (UpdateRequest) args[0];
                recorder.recordAttribute(ElasticsearchConstants.ARGS_DSL_ANNOTATION_KEY, StringUtils.abbreviate(args[0].toString(), 256));
            }
        }


    }

}
