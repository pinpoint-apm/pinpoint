/*
 * Copyright 2022 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.elasticsearch8.interceptor;

import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.UpdateRequest;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.elasticsearch8.Elasticsearch8Constants;
import com.navercorp.pinpoint.plugin.elasticsearch8.Elasticsearch8PluginConfig;
import com.navercorp.pinpoint.plugin.elasticsearch8.accessor.ClusterInfoAccessor;
import com.navercorp.pinpoint.plugin.elasticsearch8.accessor.EndPointAccessor;

public class ElasticsearchClientMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private final boolean recordDsl;
    private final boolean recordESVersion;

    public ElasticsearchClientMethodInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
        final Elasticsearch8PluginConfig elasticsearchPluginConfig = new Elasticsearch8PluginConfig(context.getProfilerConfig());
        this.recordDsl = elasticsearchPluginConfig.isRecordDsl();
        this.recordESVersion = elasticsearchPluginConfig.isRecordESVersion();
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {

        recorder.recordServiceType(Elasticsearch8Constants.ELASTICSEARCH_EXECUTOR);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
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
                recorder.recordAttribute(Elasticsearch8Constants.ARGS_VERSION_ANNOTATION_KEY, ((ClusterInfoAccessor) target)._$PINPOINT$_getClusterInfo());
            }
        }

        if (recordDsl) {
            if (args[0] instanceof SearchRequest) {
                SearchRequest request = (SearchRequest) args[0];
                recorder.recordAttribute(Elasticsearch8Constants.ARGS_DSL_ANNOTATION_KEY, StringUtils.abbreviate(request.source().toString(), 256));
            } else if (args[0] instanceof GetRequest) {
                recorder.recordAttribute(Elasticsearch8Constants.ARGS_DSL_ANNOTATION_KEY, StringUtils.abbreviate(args[0].toString(), 256));
            } else if (args[0] instanceof IndexRequest) {
                recorder.recordAttribute(Elasticsearch8Constants.ARGS_DSL_ANNOTATION_KEY, StringUtils.abbreviate(args[0].toString(), 256));
            } else if (args[0] instanceof DeleteRequest) {
                recorder.recordAttribute(Elasticsearch8Constants.ARGS_DSL_ANNOTATION_KEY, StringUtils.abbreviate(args[0].toString(), 256));
            } else if (args[0] instanceof UpdateRequest) {
                recorder.recordAttribute(Elasticsearch8Constants.ARGS_DSL_ANNOTATION_KEY, StringUtils.abbreviate(args[0].toString(), 256));
            }
        }
    }
}
