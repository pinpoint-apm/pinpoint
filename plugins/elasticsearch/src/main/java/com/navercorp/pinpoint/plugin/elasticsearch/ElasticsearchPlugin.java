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
package com.navercorp.pinpoint.plugin.elasticsearch;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.plugin.elasticsearch.accessor.ClusterInfoAccessor;
import com.navercorp.pinpoint.plugin.elasticsearch.accessor.EndPointAccessor;
import com.navercorp.pinpoint.plugin.elasticsearch.accessor.HttpHostInfoAccessor;
import com.navercorp.pinpoint.plugin.elasticsearch.interceptor.ElasticsearchExecutorInterceptor;
import com.navercorp.pinpoint.plugin.elasticsearch.interceptor.HighLevelConnectInterceptor;
import com.navercorp.pinpoint.plugin.elasticsearch.interceptor.RestClientConnectInterceptor;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

/**
 * @author Roy Kim
 */
public class ElasticsearchPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    private static String[] methodList() {

        final String[] methodlist = new String[]{"index", "get", "exists", "delete", "update"
//                , "termvectors", "bulk", "mget", "reindex", "updateByQuery", "deleteByQuery", "reindexRethrottle", "updateByQueryRethrottle", "deleteByQueryRethrottle", "mtermvectors"
                , "search"
        };

        return methodlist;
    }

    private static String[] methodAyncList() {

        final String[] methodlist = new String[]{"indexAsync", "getAsync", "existsAsync", "deleteAsync", "updateAsync"
//                , "termvectorsAsync", "bulkAsync", "mgetAsync", "reindexAsync", "updateByQueryAsync"
//                , "deleteByQueryAsync", "reindexRethrottleAsync", "updateByQueryRethrottleAsync", "deleteByQueryRethrottleAsync", "mtermvectorsAsync"
        };

        return methodlist;
    }

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        if (context == null) {
            return;
        }

        final ElasticsearchPluginConfig elasticsearchPluginConfig = new ElasticsearchPluginConfig(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("ElasticsearchPlugin config:{}", elasticsearchPluginConfig);
        }

        if (!elasticsearchPluginConfig.isEnabled()) {
            return;
        }

        addElasticsearchConnectorInterceptors();
        addElasticsearchExecutorInterceptors();
    }

    private void addElasticsearchConnectorInterceptors() {
        //v6.0.0 ~ v6.3.2 Connector
        transformTemplate.transform("org.elasticsearch.client.RestClient", ConnectorTransformCallback.class);
    }

    private void addElasticsearchExecutorInterceptors() {
        //v6.4.0 ~ v7.x.x Connector
        //v6.0.0 ~ v7.x.x Executor
        transformTemplate.transform("org.elasticsearch.client.RestHighLevelClient", ExecutorTransformCallback.class);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class ConnectorTransformCallback implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(HttpHostInfoAccessor.class);

            InstrumentMethod client = target.getConstructor("org.apache.http.impl.nio.client.CloseableHttpAsyncClient"
                    , "long"
                    , "org.apache.http.Header[]"
                    , "org.apache.http.HttpHost[]"
                    , "java.lang.String"
                    , "org.elasticsearch.client.RestClient$FailureListener"
            );
            if (client != null) {
                client.addScopedInterceptor(RestClientConnectInterceptor.class, ElasticsearchConstants.ELASTICSEARCH_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }
    }

    public static class ExecutorTransformCallback implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(EndPointAccessor.class);
            target.addField(ClusterInfoAccessor.class);

            InstrumentMethod client = target.getConstructor("org.elasticsearch.client.RestClient"
                    , "org.elasticsearch.common.CheckedConsumer"
                    , "java.util.List");
            if (client != null) {
                client.addScopedInterceptor(HighLevelConnectInterceptor.class, ElasticsearchConstants.ELASTICSEARCH_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(methodList())))) {
                method.addScopedInterceptor(ElasticsearchExecutorInterceptor.class, ElasticsearchConstants.ELASTICSEARCH_EXECUTOR_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.modifier(Modifier.PUBLIC), MethodFilters.name(methodAyncList())))) {
                method.addScopedInterceptor(ElasticsearchExecutorInterceptor.class, ElasticsearchConstants.ELASTICSEARCH_EXECUTOR_SCOPE, ExecutionPolicy.BOUNDARY);
            }

            return target.toBytecode();
        }
    }
}
