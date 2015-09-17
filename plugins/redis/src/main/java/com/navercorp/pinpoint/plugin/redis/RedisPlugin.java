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
import com.navercorp.pinpoint.bootstrap.instrument.PinpointInstrument;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;

/**
 * 
 * @author jaehong.kim
 *
 */
public class RedisPlugin implements ProfilerPlugin, RedisConstants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final RedisPluginConfig config = new RedisPluginConfig(context.getConfig());
        final boolean pipelineEnabled = config.isPipelineEnabled();

        // jedis
        addJedisClassEditors(context, config);
        addProtocolClassEditor(context, config);

        if (pipelineEnabled) {
            // jedis pipeline
            addJedisClientClassEditor(context, config);
            addJedisPipelineClassEditors(context, config);
        }
    }

    // Jedis & BinaryJedis
    private void addJedisClassEditors(ProfilerPluginSetupContext context, RedisPluginConfig config) {
        addJedisExtendedClassEditor(context, config, "redis.clients.jedis.BinaryJedis", new TransformHandler() {

            @Override
            public void handle(InstrumentClass target) throws InstrumentException {
                target.addField(METADATA_END_POINT);
            }
        });

        // Jedis extends BinaryJedis
        addJedisExtendedClassEditor(context, config, "redis.clients.jedis.Jedis", null);
    }

    private void addJedisExtendedClassEditor(ProfilerPluginSetupContext context, final RedisPluginConfig config, final String targetClassName, final TransformHandler handler) {
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(PinpointInstrument instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
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
                        method.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisMethodInterceptor", config.isIo());
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
                        }
                    }
                }

                return target.toBytecode();
            }
        });
    }

    // Client
    private void addJedisClientClassEditor(ProfilerPluginSetupContext context, RedisPluginConfig config) {
        context.addClassFileTransformer("redis.clients.jedis.Client", new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(PinpointInstrument instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(METADATA_END_POINT);

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

    private void addProtocolClassEditor(ProfilerPluginSetupContext context, RedisPluginConfig config) {
        context.addClassFileTransformer("redis.clients.jedis.Protocol", new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(PinpointInstrument instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name("sendCommand", "read"), MethodFilters.modifierNot(Modifier.PRIVATE)))) {
                    method.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.ProtocolSendCommandAndReadMethodInterceptor");
                }

                return target.toBytecode();
            }
        });
    }

    // Pipeline
    private void addJedisPipelineClassEditors(ProfilerPluginSetupContext context, RedisPluginConfig config) {
        addJedisPipelineBaseExtendedClassEditor(context, config, "redis.clients.jedis.PipelineBase", null);

        // MultikeyPipellineBase extends PipelineBase
        addJedisPipelineBaseExtendedClassEditor(context, config, "redis.clients.jedis.MultiKeyPipelineBase", null);

        // Pipeline extends PipelineBase
        addJedisPipelineBaseExtendedClassEditor(context, config, "redis.clients.jedis.Pipeline", new TransformHandler() {

            @Override
            public void handle(InstrumentClass target) throws InstrumentException {
                target.addField(METADATA_END_POINT);

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

    private void addJedisPipelineBaseExtendedClassEditor(ProfilerPluginSetupContext context, final RedisPluginConfig config, String targetClassName, final TransformHandler handler) {
        context.addClassFileTransformer(targetClassName, new PinpointClassFileTransformer() {

            @Override
            public byte[] transform(PinpointInstrument instrumentContext, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentContext.getInstrumentClass(classLoader, className, classfileBuffer);
                if (handler != null) {
                    handler.handle(target);
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(MethodFilters.name(JedisPipelineMethodNames.get()), MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                    try {
                        method.addInterceptor("com.navercorp.pinpoint.plugin.redis.interceptor.JedisPipelineMethodInterceptor", config.isIo());
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + method, e);
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
}