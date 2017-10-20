/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.redis;

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

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author jaehong.kim
 */
public class RedisPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final JedisMethodNameFilter methodNameFilter = new JedisMethodNameFilter();
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final RedisPluginConfig config = new RedisPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            if (logger.isInfoEnabled()) {
                logger.info("Disable RedisPlugin. config={}", config);
            }
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Enable RedisPlugin. config={}", config);
        }

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
    private void addJedis(RedisPluginConfig config) {
        addBinaryJedisExtends(config, "redis.clients.jedis.BinaryJedis", new TransformHandler() {

            @Override
            public void handle(InstrumentClass target) throws InstrumentException {
                target.addField(RedisConstants.END_POINT_ACCESSOR);
            }
        });

        // Jedis extends BinaryJedis
        addBinaryJedisExtends(config, "redis.clients.jedis.Jedis", null);
    }

    private void addBinaryJedisExtends(final RedisPluginConfig config, final String targetClassName, final TransformHandler handler) {
        transformTemplate.transform(targetClassName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (handler != null) {
                    handler.handle(target);
                }

                // Set endpoint
                // host
                addSetEndPointInterceptor(target, "java.lang.String");
                // host, port
                addSetEndPointInterceptor(target, "java.lang.String", "int");
                // host, port, ssl
                addSetEndPointInterceptor(target, "java.lang.String", "int", "boolean");
                // host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier
                addSetEndPointInterceptor(target, "java.lang.String", "int", "boolean", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");
                // host, port, timeout
                addSetEndPointInterceptor(target, "java.lang.String", "int", "int");
                // host, port, timeout, ssl
                addSetEndPointInterceptor(target, "java.lang.String", "int", "int", "boolean");
                // host, port, timeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier
                addSetEndPointInterceptor(target, "java.lang.String", "int", "int", "boolean", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");
                // host, port, connectionTimeout, soTimeout
                addSetEndPointInterceptor(target, "java.lang.String", "int", "int", "int");
                // host, port, connectionTimeout, soTimeout, ssl
                addSetEndPointInterceptor(target, "java.lang.String", "int", "int", "int", "boolean");
                // host, port, connectionTimeout, soTimeout, ssl, sslSocketFactory, sslParameters, hostnameVerifier
                addSetEndPointInterceptor(target, "java.lang.String", "int", "int", "int", "boolean", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");
                // shardInfo
                addSetEndPointInterceptor(target, "redis.clients.jedis.JedisShardInfo");
                // uri
                addSetEndPointInterceptor(target, "java.net.URI");
                // uri, sslSocketFactory, sslParameters, hostnameVerifier
                addSetEndPointInterceptor(target, "java.net.URI", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");
                // uri, timeout
                addSetEndPointInterceptor(target, "java.net.URI", "int");
                // uri, timeout, sslSocketFactory, sslParameters, hostnameVerifier
                addSetEndPointInterceptor(target, "java.net.URI", "int", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");
                // uri, connectionTimeout, soTimeout
                addSetEndPointInterceptor(target, "java.net.URI", "int", "int");
                // uri, connectionTimeout, soTimeout, sslSocketFactory, sslParameters, hostnameVerifier
                addSetEndPointInterceptor(target, "java.net.URI", "int", "int", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");

                // methods(commands)
                addJedisMethodInterceptor(target, config, RedisConstants.REDIS_SCOPE);

                return target.toBytecode();
            }
        });
    }

    // Client
    private void addClient() {
        transformTemplate.transform("redis.clients.jedis.Client", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(RedisConstants.END_POINT_ACCESSOR);

                // Set endpoint
                // host
                addSetEndPointInterceptor(target, "java.lang.String");
                // host, port
                addSetEndPointInterceptor(target, "java.lang.String", "int");
                // host, port, ssl
                addSetEndPointInterceptor(target, "java.lang.String", "int", "boolean");
                // host, port, ssl, sslSocketFactory, sslParameters, hostnameVerifier
                addSetEndPointInterceptor(target, "java.lang.String", "int", "boolean", "javax.net.ssl.SSLSocketFactory", "javax.net.ssl.SSLParameters", "javax.net.ssl.HostnameVerifier");

                return target.toBytecode();
            }
        });
    }

    private void addSetEndPointInterceptor(final InstrumentClass target, final String... parameterTypes) throws InstrumentException {
        final InstrumentMethod method = target.getConstructor(parameterTypes);
        if (method != null) {
            method.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.SetEndPointInterceptor");
        }
    }

    private void addProtocol() {
        transformTemplate.transform("redis.clients.jedis.Protocol", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name("sendCommand", "read"), MethodFilters.modifierNot(Modifier.PRIVATE)))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.ProtocolSendCommandAndReadMethodInterceptor", RedisConstants.REDIS_SCOPE, ExecutionPolicy.INTERNAL);
                }

                return target.toBytecode();
            }
        });
    }

    // Pipeline
    private void addPipeline(RedisPluginConfig config) {
        addPipelineBaseExtends(config, "redis.clients.jedis.PipelineBase", null);

        // MultikeyPipellineBase extends PipelineBase
        addPipelineBaseExtends(config, "redis.clients.jedis.MultiKeyPipelineBase", null);

        // Pipeline extends PipelineBase
        addPipelineBaseExtends(config, "redis.clients.jedis.Pipeline", new TransformHandler() {

            @Override
            public void handle(InstrumentClass target) throws InstrumentException {
                target.addField(RedisConstants.END_POINT_ACCESSOR);

                final InstrumentMethod setClientMethod = target.getDeclaredMethod("setClient", "redis.clients.jedis.Client");
                if (setClientMethod != null) {
                    setClientMethod.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.AttachEndPointInterceptor");
                }

                final InstrumentMethod constructor = target.getConstructor("redis.clients.jedis.Client");
                if (constructor != null) {
                    constructor.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.AttachEndPointInterceptor");
                }
            }
        });
    }

    private void addPipelineBaseExtends(final RedisPluginConfig config, String targetClassName, final TransformHandler handler) {
        transformTemplate.transform(targetClassName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (handler != null) {
                    handler.handle(target);
                }

                // methods(commands)
                addJedisMethodInterceptor(target, config, RedisConstants.REDIS_SCOPE);

                return target.toBytecode();
            }
        });
    }

    private void addJedisMethodInterceptor(final InstrumentClass target, final RedisPluginConfig config, final String scope) {
        for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(this.methodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
            try {
                method.addScopedInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisMethodInterceptor", va(config.isIo()), scope);
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method {}", method, e);
                }
            }
        }
    }

    private interface TransformHandler {
        void handle(InstrumentClass target) throws InstrumentException;
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}