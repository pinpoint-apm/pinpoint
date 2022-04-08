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
package com.navercorp.pinpoint.plugin.elasticsearch8;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.elasticsearch8.accessor.ClusterInfoAccessor;
import com.navercorp.pinpoint.plugin.elasticsearch8.accessor.EndPointAccessor;
import com.navercorp.pinpoint.plugin.elasticsearch8.interceptor.ElasticsearchClientMethodInterceptor;
import com.navercorp.pinpoint.plugin.elasticsearch8.interceptor.ElasticsearchClientConstructorInterceptor;

import java.security.ProtectionDomain;

public class Elasticsearch8Plugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        if (context == null) {
            return;
        }

        final Elasticsearch8PluginConfig elasticsearchPluginConfig = new Elasticsearch8PluginConfig(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("Elasticsearch8Plugin config:{}", elasticsearchPluginConfig);
        }

        if (!elasticsearchPluginConfig.isEnabled()) {
            return;
        }

        addElasticsearchExecutorInterceptors();
    }

    private void addElasticsearchExecutorInterceptors() {
        transformTemplate.transform("co.elastic.clients.elasticsearch.ElasticsearchClient", ElasticsearchClientTransformCallback.class);
        transformTemplate.transform("co.elastic.clients.elasticsearch.ElasticsearchAsyncClient", ElasticsearchClientTransformCallback.class);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class ElasticsearchClientTransformCallback implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            target.addField(EndPointAccessor.class);
            target.addField(ClusterInfoAccessor.class);

            InstrumentMethod constructorMethod1 = target.getConstructor("co.elastic.clients.transport.ElasticsearchTransport");
            if (constructorMethod1 != null) {
                constructorMethod1.addInterceptor(ElasticsearchClientConstructorInterceptor.class);
            }
            InstrumentMethod constructorMethod2 = target.getConstructor("co.elastic.clients.transport.ElasticsearchTransport", "co.elastic.clients.transport.TransportOptions");
            if (constructorMethod2 != null) {
                constructorMethod2.addInterceptor(ElasticsearchClientConstructorInterceptor.class);
            }

            InstrumentMethod indexMethod = target.getDeclaredMethod("index", "co.elastic.clients.elasticsearch.core.IndexRequest");
            if (indexMethod != null) {
                indexMethod.addInterceptor(ElasticsearchClientMethodInterceptor.class);
            }
            InstrumentMethod getMethod = target.getDeclaredMethod("get", "co.elastic.clients.elasticsearch.core.GetRequest");
            if (getMethod != null) {
                getMethod.addInterceptor(ElasticsearchClientMethodInterceptor.class);
            }
            InstrumentMethod existsMethod = target.getDeclaredMethod("exists", "co.elastic.clients.elasticsearch.core.ExistsRequest");
            if (existsMethod != null) {
                existsMethod.addInterceptor(ElasticsearchClientMethodInterceptor.class);
            }
            InstrumentMethod deleteMethod = target.getDeclaredMethod("delete", "co.elastic.clients.elasticsearch.core.DeleteRequest");
            if (deleteMethod != null) {
                deleteMethod.addInterceptor(ElasticsearchClientMethodInterceptor.class);
            }
            InstrumentMethod updateMethod = target.getDeclaredMethod("update", "co.elastic.clients.elasticsearch.core.UpdateRequest");
            if (updateMethod != null) {
                updateMethod.addInterceptor(ElasticsearchClientMethodInterceptor.class);
            }
            InstrumentMethod searchMethod = target.getDeclaredMethod("search", "co.elastic.clients.elasticsearch.core.SearchRequest");
            if (searchMethod != null) {
                searchMethod.addInterceptor(ElasticsearchClientMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }
}
