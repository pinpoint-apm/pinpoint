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

package com.navercorp.pinpoint.plugin.redis.lettuce;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.redis.LettuceMethodNameFilter;
import com.navercorp.pinpoint.plugin.redis.RedisConstants;
import com.navercorp.pinpoint.plugin.redis.RedisPluginConfig;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author jaehong.kim
 */
public class LettucePlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final LettuceMethodNameFilter lettuceMethodNameFilter = new LettuceMethodNameFilter();
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final RedisPluginConfig config = new RedisPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            if (logger.isInfoEnabled()) {
                logger.info("Disable RedisPlugin.");
            }
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Enable RedisPlugin. version range=[5.0.0.RELEASE, 5.1.2.RELEASE]");
        }

        // Set endpoint
        addRedisClient();

        // Attach endpoint
        addDefaultConnectionFuture();
        addStatefulRedisConnection();

        // Commands
        addRedisCommands(config);
    }

    private void addRedisClient() {
        transformTemplate.transform("io.lettuce.core.RedisClient", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(RedisConstants.END_POINT_ACCESSOR);

                // Set endpoint
                final InstrumentMethod constructor = target.getConstructor("io.lettuce.core.resource.ClientResources", "io.lettuce.core.RedisURI");
                if (constructor != null) {
                    constructor.addInterceptor("com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RedisClientConstructorInterceptor");
                }

                // Attach endpoint
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("connect", "connectAsync", "connectPubSub", "connectPubSubAsync", "connectSentinel", "connectSentinelAsync"))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.AttachEndPointInterceptor", RedisConstants.REDIS_SCOPE);
                }

                return target.toBytecode();
            }
        });
    }

    private void addDefaultConnectionFuture() {
        transformTemplate.transform("io.lettuce.core.DefaultConnectionFuture", new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(RedisConstants.END_POINT_ACCESSOR);

                // Attach endpoint
                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("get", "join"))) {
                    method.addScopedInterceptor("com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.AttachEndPointInterceptor", RedisConstants.REDIS_SCOPE);
                }

                return target.toBytecode();
            }
        });
    }

    private void addStatefulRedisConnection() {
        addStatefulRedisConnection("io.lettuce.core.StatefulRedisConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.pubsub.StatefulRedisPubSubConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.pubsub.StatefulRedisClusterPubSubConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.masterslave.StatefulRedisMasterSlaveConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.sentinel.StatefulRedisSentinelConnectionImpl");
    }

    private void addStatefulRedisConnection(final String className) {
        transformTemplate.transform(className, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                target.addField(RedisConstants.END_POINT_ACCESSOR);
                return target.toBytecode();
            }
        });
    }

    private void addRedisCommands(final RedisPluginConfig config) {
        // Commands
        addAbstractRedisCommands("io.lettuce.core.AbstractRedisAsyncCommands", true, config);

        addAbstractRedisCommands("io.lettuce.core.RedisAsyncCommandsImpl", false, config);
        addAbstractRedisCommands("io.lettuce.core.cluster.RedisAdvancedClusterAsyncCommandsImpl", false, config);
        addAbstractRedisCommands("io.lettuce.core.cluster.RedisClusterPubSubAsyncCommandsImpl", false, config);
        addAbstractRedisCommands("io.lettuce.core.pubsub.RedisPubSubAsyncCommandsImpl", false, config);

        // Reactive
        addAbstractRedisCommands("io.lettuce.core.AbstractRedisReactiveCommands", true, config);

        addAbstractRedisCommands("io.lettuce.core.cluster.RedisAdvancedClusterReactiveCommandsImpl", false, config);
        addAbstractRedisCommands("io.lettuce.core.cluster.RedisClusterPubSubReactiveCommandsImpl", false, config);
        addAbstractRedisCommands("io.lettuce.core.pubsub.RedisPubSubReactiveCommandsImpl", false, config);
        addAbstractRedisCommands("io.lettuce.core.RedisReactiveCommandsImpl", false, config);
        addAbstractRedisCommands("io.lettuce.core.sentinel.RedisSentinelReactiveCommandsImpl", false, config);
    }

    private void addAbstractRedisCommands(final String className, final boolean getter, final RedisPluginConfig config) {
        transformTemplate.transform(className, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
                if (getter) {
                    target.addGetter("com.navercorp.pinpoint.plugin.redis.lettuce.StatefulConnectionGetter", "connection");
                }

                for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(lettuceMethodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                    try {
                        method.addScopedInterceptor("com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.LettuceMethodInterceptor", va(config.isIo()), RedisConstants.REDIS_SCOPE);
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

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}