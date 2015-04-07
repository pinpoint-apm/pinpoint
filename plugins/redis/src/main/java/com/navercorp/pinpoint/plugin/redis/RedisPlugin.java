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

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConstructorEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodEditorBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerExceptionHandler;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;
import com.navercorp.pinpoint.plugin.redis.filter.JedisMethodNames;
import com.navercorp.pinpoint.plugin.redis.filter.JedisPipelineMethodNames;
import com.navercorp.pinpoint.plugin.redis.filter.NameBasedMethodFilter;

/**
 * 
 * @author jaehong.kim
 *
 */
public class RedisPlugin implements ProfilerPlugin, RedisConstants {

    private static final String STRING = "java.lang.String";
    private static final String INT = "int";
    private static final String URI = "java.net.URI";

    private static final String JEDIS = "redis.clients.jedis.Jedis";
    private static final String BINARY_JEDIS = "redis.clients.jedis.BinaryJedis";
    private static final String JEDIS_CLIENT = "redis.clients.jedis.Client";
    private static final String JEDIS_PIPELINE_BASE = "redis.clients.jedis.PipelineBase";
    private static final String JEDIS_MULTIKEY_PIPELINE_BASE = "redis.clients.jedis.MultiKeyPipelineBase";
    private static final String JEDIS_PIPELINE = "redis.clients.jedis.Pipeline";
    private static final String JEDIS_SHARD_INFO = "redis.clients.jedis.JedisShardInfo";

    private static final String JEDIS_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisConstructorInterceptor";
    private static final String JEDIS_METHODS_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisMethodInterceptor";
    private static final String JEDIS_CLIENT_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisClientConstructorInterceptor";
    private static final String JEDIS_PIPELINE_METHODS_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisPipelineMethodInterceptor";
    private static final String JEDIS_PIPELINE_SET_CLIENT_METHOD_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisPipelineSetClientMethodInterceptor";
    private static final String JEDIS_PIPELINE_CONSTRUCTOR_INTERCEPTOR = "com.navercorp.pinpoint.plugin.redis.interceptor.JedisPipelineConstructorInterceptor";

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final RedisPluginConfig config = new RedisPluginConfig(context.getConfig());
        final boolean enabled = config.isEnabled();
        final boolean pipelineEnabled = config.isPipelineEnabled();

        if (enabled) {
            // jedis
            addJedisClassEditors(context, config);

            if (pipelineEnabled) {
                // jedis pipeline
                addJedisClientClassEditor(context, config);
                addJedisPipelineClassEditors(context, config);
            }
        }
    }

    // Jedis & BinaryJedis
    private void addJedisClassEditors(ProfilerPluginSetupContext context, RedisPluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = addJedisExtendedClassEditor(context, config, BINARY_JEDIS);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);
        context.addClassFileTransformer(classEditorBuilder.build());

        // Jedis extends BinaryJedis
        context.addClassFileTransformer(addJedisExtendedClassEditor(context, config, JEDIS).build());
        
    }

    private ClassFileTransformerBuilder addJedisExtendedClassEditor(ProfilerPluginSetupContext context, RedisPluginConfig config, final String targetClassName) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassEditorBuilder(targetClassName);

        final ConstructorEditorBuilder constructorEditorBuilderArg1 = classEditorBuilder.editConstructor(STRING);
        constructorEditorBuilderArg1.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg1.injectInterceptor(JEDIS_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg2 = classEditorBuilder.editConstructor(STRING, INT);
        constructorEditorBuilderArg2.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg2.injectInterceptor(JEDIS_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg3 = classEditorBuilder.editConstructor(STRING, INT, INT);
        constructorEditorBuilderArg3.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg3.injectInterceptor(JEDIS_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg4 = classEditorBuilder.editConstructor(URI);
        constructorEditorBuilderArg4.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg4.injectInterceptor(JEDIS_CONSTRUCTOR_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilderArg5 = classEditorBuilder.editConstructor(JEDIS_SHARD_INFO);
        constructorEditorBuilderArg5.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg5.injectInterceptor(JEDIS_CONSTRUCTOR_INTERCEPTOR);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(JedisMethodNames.get()));
        methodEditorBuilder.exceptionHandler(new MethodTransformerExceptionHandler() {
            @Override
            public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Exception {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                }
            }
        });
        methodEditorBuilder.injectInterceptor(JEDIS_METHODS_INTERCEPTOR);

        return classEditorBuilder;
    }

    // Client
    private void addJedisClientClassEditor(ProfilerPluginSetupContext context, RedisPluginConfig config) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassEditorBuilder(JEDIS_CLIENT);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);

        final ConstructorEditorBuilder constructorEditorBuilderArg1 = classEditorBuilder.editConstructor(STRING);
        constructorEditorBuilderArg1.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg1.injectInterceptor(JEDIS_CLIENT_CONSTRUCTOR_INTERCEPTOR);
        
        final ConstructorEditorBuilder constructorEditorBuilderArg2 = classEditorBuilder.editConstructor(STRING, INT);
        constructorEditorBuilderArg2.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilderArg2.injectInterceptor(JEDIS_CLIENT_CONSTRUCTOR_INTERCEPTOR);

        context.addClassFileTransformer(classEditorBuilder.build());
    }

    // Pipeline
    private void addJedisPipelineClassEditors(ProfilerPluginSetupContext context, RedisPluginConfig config) {
        context.addClassFileTransformer(addJedisPipelineBaseExtendedClassEditor(context, config, JEDIS_PIPELINE_BASE).build());

        // MultikeyPipellineBase extends PipelineBase
        context.addClassFileTransformer(addJedisPipelineBaseExtendedClassEditor(context, config, JEDIS_MULTIKEY_PIPELINE_BASE).build());

        // Pipeline extends PipelineBase
        final ClassFileTransformerBuilder classEditorBuilder = addJedisPipelineBaseExtendedClassEditor(context, config, JEDIS_PIPELINE);
        classEditorBuilder.injectMetadata(METADATA_END_POINT);
        final MethodEditorBuilder setClientMethodEditorBuilder = classEditorBuilder.editMethod("setClient", JEDIS_CLIENT);
        setClientMethodEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        setClientMethodEditorBuilder.injectInterceptor(JEDIS_PIPELINE_SET_CLIENT_METHOD_INTERCEPTOR);

        final ConstructorEditorBuilder constructorEditorBuilder = classEditorBuilder.editConstructor(JEDIS_CLIENT);
        constructorEditorBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        constructorEditorBuilder.injectInterceptor(JEDIS_PIPELINE_CONSTRUCTOR_INTERCEPTOR);
        context.addClassFileTransformer(classEditorBuilder.build());
    }

    private ClassFileTransformerBuilder addJedisPipelineBaseExtendedClassEditor(ProfilerPluginSetupContext context, RedisPluginConfig config, String targetClassName) {
        final ClassFileTransformerBuilder classEditorBuilder = context.getClassEditorBuilder(targetClassName);

        final MethodEditorBuilder methodEditorBuilder = classEditorBuilder.editMethods(new NameBasedMethodFilter(JedisPipelineMethodNames.get()));
        methodEditorBuilder.exceptionHandler(new MethodTransformerExceptionHandler() {
            @Override
            public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Exception {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                }
            }
        });
        methodEditorBuilder.injectInterceptor(JEDIS_PIPELINE_METHODS_INTERCEPTOR);

        return classEditorBuilder;
    }
}