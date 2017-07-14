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
 * 
 * @author jaehong.kim
 *
 */
public class RedisPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final RedisPluginConfig config = new RedisPluginConfig(context.getConfig());
        if (logger.isInfoEnabled()) {
            logger.info("RedisPlugin config:{}", config);
        }
        final boolean pipelineEnabled = config.isPipelineEnabled();

        // jedis
        addJedisClassEditors(config);
        addProtocolClassEditor();

        if (pipelineEnabled) {
            // jedis pipeline
            addJedisClientClassEditor();
            addJedisPipelineClassEditors(config);
        }
    }

    // Jedis & BinaryJedis
    private void addJedisClassEditors(RedisPluginConfig config) {
        addJedisExtendedClassEditor(config, "redis.clients.jedis.BinaryJedis", new TransformHandler() {

            @Override
            public void handle(InstrumentClass target) throws InstrumentException {
                target.addField(RedisConstants.END_POINT_ACCESSOR);
            }
        });

        // Jedis extends BinaryJedis
        addJedisExtendedClassEditor(config, "redis.clients.jedis.Jedis", null);
    }

    private void addJedisExtendedClassEditor(final RedisPluginConfig config, final String targetClassName, final TransformHandler handler) {
       transformTemplate.transform(targetClassName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (handler != null) {
                    handler.handle(target);
                }

                final InstrumentMethod constructorEditorBuilderArg1 = target.getConstructor("java.lang.String");
                if (constructorEditorBuilderArg1 != null) {
                    constructorEditorBuilderArg1.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisConstructorInterceptor");
                }

                final InstrumentMethod constructorEditorBuilderArg2 = target.getConstructor("java.lang.String", "int");
                if (constructorEditorBuilderArg2 != null) {
                    constructorEditorBuilderArg2.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisConstructorInterceptor");
                }

                final InstrumentMethod constructorEditorBuilderArg3 = target.getConstructor("java.lang.String", "int", "int");
                if (constructorEditorBuilderArg3 != null) {
                    constructorEditorBuilderArg3.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisConstructorInterceptor");
                }

                final InstrumentMethod constructorEditorBuilderArg4 = target.getConstructor("java.net.URI");
                if (constructorEditorBuilderArg4 != null) {
                    constructorEditorBuilderArg4.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisConstructorInterceptor");
                }

                final InstrumentMethod constructorEditorBuilderArg5 = target.getConstructor("redis.clients.jedis.JedisShardInfo");
                if (constructorEditorBuilderArg5 != null) {
                    constructorEditorBuilderArg5.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisConstructorInterceptor");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name(JedisMethodNames.get()), MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                    try {
                        method.addScopedInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisMethodInterceptor", va(config.isIo()), RedisConstants.REDIS_SCOPE);
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method {}", method, e);
                        }
                    }
                }

                return target.toBytecode();
            }
        });
    }

    // Client
    private void addJedisClientClassEditor() {
       transformTemplate.transform("redis.clients.jedis.Client", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(RedisConstants.END_POINT_ACCESSOR);

                final InstrumentMethod constructorEditorBuilderArg1 = target.getConstructor("java.lang.String");
                if (constructorEditorBuilderArg1 != null) {
                    constructorEditorBuilderArg1.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisClientConstructorInterceptor");
                }

                final InstrumentMethod constructorEditorBuilderArg2 = target.getConstructor("java.lang.String", "int");
                if (constructorEditorBuilderArg2 != null) {
                    constructorEditorBuilderArg2.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisClientConstructorInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    private void addProtocolClassEditor() {
        transformTemplate.transform("redis.clients.jedis.Protocol", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name("sendCommand", "read"), MethodFilters.modifierNot(Modifier.PRIVATE)))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.ProtocolSendCommandAndReadMethodInterceptor", RedisConstants.REDIS_SCOPE, ExecutionPolicy.INTERNAL);
                }

                return target.toBytecode();
            }
        });
    }

    // Pipeline
    private void addJedisPipelineClassEditors(RedisPluginConfig config) {
        addJedisPipelineBaseExtendedClassEditor(config, "redis.clients.jedis.PipelineBase", null);

        // MultikeyPipellineBase extends PipelineBase
        addJedisPipelineBaseExtendedClassEditor(config, "redis.clients.jedis.MultiKeyPipelineBase", null);

        // Pipeline extends PipelineBase
        addJedisPipelineBaseExtendedClassEditor(config, "redis.clients.jedis.Pipeline", new TransformHandler() {

            @Override
            public void handle(InstrumentClass target) throws InstrumentException {
                target.addField(RedisConstants.END_POINT_ACCESSOR);

                final InstrumentMethod setClientMethodEditorBuilder = target.getDeclaredMethod("setClient", "redis.clients.jedis.Client");
                if (setClientMethodEditorBuilder != null) {
                    setClientMethodEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisPipelineSetClientMethodInterceptor");
                }

                final InstrumentMethod constructorEditorBuilder = target.getConstructor("redis.clients.jedis.Client");
                if (constructorEditorBuilder != null) {
                    constructorEditorBuilder.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisPipelineConstructorInterceptor");
                }
            }
        });
    }

    private void addJedisPipelineBaseExtendedClassEditor(final RedisPluginConfig config, String targetClassName, final TransformHandler handler) {
        transformTemplate.transform(targetClassName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (handler != null) {
                    handler.handle(target);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name(JedisPipelineMethodNames.get()), MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                    try {
                        method.addScopedInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisPipelineMethodInterceptor", va(config.isIo()), RedisConstants.REDIS_SCOPE);
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method {}", method, e);
                        }
                    }
                }

                return target.toBytecode();
            }
        });
    }

    private interface TransformHandler {
        void handle(InstrumentClass target) throws InstrumentException;
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}