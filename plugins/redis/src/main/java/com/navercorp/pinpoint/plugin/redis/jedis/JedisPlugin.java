/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.redis.jedis;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.redis.jedis.interceptor.AttachEndPointInterceptor;
import com.navercorp.pinpoint.plugin.redis.jedis.interceptor.ProtocolSendCommandAndReadMethodInterceptor;


/**
 * @author jaehong.kim
 */
public class JedisPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final JedisPluginConfig config = new JedisPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            if (logger.isInfoEnabled()) {
                logger.info("Disable JedisPlugin. config={}", config);
            }
            return;
        }
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        final boolean pipeline = config.isPipeline();
        // jedis & jedis cluster
        addJedis(config);
        addProtocol();
        if (pipeline) {
            // jedis pipeline
            addClient();
            addPipeline(config);
        }
    }

    // Jedis & BinaryJedis
    private void addJedis(JedisPluginConfig config) {
        addBinaryJedisExtends(config, "redis.clients.jedis.BinaryJedis", BinaryJedisTransform.class);

        // Jedis extends BinaryJedis
        addBinaryJedisExtends(config, "redis.clients.jedis.Jedis", BinaryJedisExtendsTransform.class);
    }


    public static class BinaryJedisTransform extends BinaryJedisExtendsTransform {
        @Override
        public void handle(InstrumentClass target) throws InstrumentException {
            target.addField(EndPointAccessor.class);
        }
    }

    private void addBinaryJedisExtends(final JedisPluginConfig config, final String targetClassName, Class<? extends TransformCallback> transformCallback) {
        transformTemplate.transform(targetClassName, transformCallback);
    }

    public static class BinaryJedisExtendsTransform implements TransformCallback {

        public BinaryJedisExtendsTransform() {
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            handle(target);


            // Set endpoint
            // host
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String");
            // host, port
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int");
            // host, port, ssl
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int", "boolean");
            // host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int", "boolean", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");
            // host, port, timeout
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int", "int");
            // host, port, timeout, ssl
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int", "int", "boolean");
            // host, port, timeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int", "int", "boolean", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");
            // host, port, connectionTimeout, soTimeout
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int", "int", "int");
            // host, port, connectionTimeout, soTimeout, ssl
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int", "int", "int", "boolean");
            // host, port, connectionTimeout, soTimeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int", "int", "int", "boolean", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");
            // shardInfo
            JedisUtils.addSetEndPointInterceptor(target, "redis.clients.jedis.JedisShardInfo");
            // uri
            JedisUtils.addSetEndPointInterceptor(target, "java.net.URI");
            // uri, sslSocketFactory, sslParameters, hostnameVerifier
            JedisUtils.addSetEndPointInterceptor(target, "java.net.URI", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");
            // uri, timeout
            JedisUtils.addSetEndPointInterceptor(target, "java.net.URI", "int");
            // uri, timeout, sslSocketFactory, sslParameters, hostnameVerifier
            JedisUtils.addSetEndPointInterceptor(target, "java.net.URI", "int", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");
            // uri, connectionTimeout, soTimeout
            JedisUtils.addSetEndPointInterceptor(target, "java.net.URI", "int", "int");
            // uri, connectionTimeout, soTimeout, sslSocketFactory, sslParameters, hostnameVerifier
            JedisUtils.addSetEndPointInterceptor(target, "java.net.URI", "int", "int", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");

            // methods(commands)
            final JedisPluginConfig config = new JedisPluginConfig(instrumentor.getProfilerConfig());
            JedisUtils.addJedisMethodInterceptor(target, config, JedisConstants.REDIS_SCOPE);

            return target.toBytecode();
        }

        protected void handle(InstrumentClass target) throws InstrumentException {
            ;
        }
    }

    // Client
    private void addClient() {
        transformTemplate.transform("redis.clients.jedis.Client", ClientTransform.class);
    }

    public static class ClientTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(EndPointAccessor.class);

            // Set endpoint
            // host
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String");
            // host, port
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int");
            // host, port, ssl
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int", "boolean");
            // host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier
            JedisUtils.addSetEndPointInterceptor(target, "java.lang.String", "int", "boolean", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");

            return target.toBytecode();
        }
    }


    private void addProtocol() {
        transformTemplate.transform("redis.clients.jedis.Protocol", ProtocolTransform.class);
    }

    public static class ProtocolTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name("sendCommand", "read"), MethodFilters.modifierNot(Modifier.PRIVATE)))) {
                method.addScopedInterceptor(ProtocolSendCommandAndReadMethodInterceptor.class, JedisConstants.REDIS_SCOPE, ExecutionPolicy.INTERNAL);
            }

            return target.toBytecode();
        }
    }

    // Pipeline
    private void addPipeline(JedisPluginConfig config) {
        addPipelineBaseExtends("redis.clients.jedis.PipelineBase", PipelineBaseExtendsTransform.class);

        // MultikeyPipellineBase extends PipelineBase
        addPipelineBaseExtends("redis.clients.jedis.MultiKeyPipelineBase", PipelineBaseExtendsTransform.class);

        // Pipeline extends PipelineBase
        addPipelineBaseExtends("redis.clients.jedis.Pipeline", PipelineTransform.class);
    }

    private void addPipelineBaseExtends(String targetClassName, final Class<? extends TransformCallback> transformCallback) {
        transformTemplate.transform(targetClassName, transformCallback);
    }

    public static class PipelineTransform extends PipelineBaseExtendsTransform {
        @Override
        protected void handle(InstrumentClass target) throws InstrumentException {
            target.addField(EndPointAccessor.class);

            final InstrumentMethod setClientMethod = target.getDeclaredMethod("setClient", "redis.clients.jedis.Client");
            if (setClientMethod != null) {
                setClientMethod.addInterceptor(AttachEndPointInterceptor.class);
            }

            final InstrumentMethod constructor = target.getConstructor("redis.clients.jedis.Client");
            if (constructor != null) {
                constructor.addInterceptor(AttachEndPointInterceptor.class);
            }
        }
    }

    public static class PipelineBaseExtendsTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            handle(target);

            final JedisPluginConfig config = new JedisPluginConfig(instrumentor.getProfilerConfig());
            // methods(commands)
            JedisUtils.addJedisMethodInterceptor(target, config, JedisConstants.REDIS_SCOPE);

            return target.toBytecode();
        }

        protected void handle(InstrumentClass target) throws InstrumentException {
            ;
        }
    }


    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}