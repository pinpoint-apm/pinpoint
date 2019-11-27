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
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.AttachEndPointInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.LettuceMethodInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RedisClientConstructorInterceptor;

import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public class LettucePlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final LettucePluginConfig config = new LettucePluginConfig(context.getConfig());

        if (!config.isEnable()) {
            logger.info("{} disabled", this.getClass().getSimpleName());
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("{} version range=[5.0.0.RELEASE, 5.2.1.RELEASE], config={}", this.getClass().getSimpleName(), config);
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
        transformTemplate.transform("io.lettuce.core.RedisClient", RedisClientTransform.class);
    }

    public static class RedisClientTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(EndPointAccessor.class);

            // Set endpoint
            final InstrumentMethod constructor = target.getConstructor("io.lettuce.core.resource.ClientResources", "io.lettuce.core.RedisURI");
            if (constructor != null) {
                constructor.addInterceptor(RedisClientConstructorInterceptor.class);
            }

            // Attach endpoint
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("connect", "connectAsync", "connectPubSub", "connectPubSubAsync", "connectSentinel", "connectSentinelAsync"))) {
                method.addScopedInterceptor(AttachEndPointInterceptor.class, LettuceConstants.REDIS_SCOPE);
            }

            return target.toBytecode();
        }
    }

    private void addDefaultConnectionFuture() {
        transformTemplate.transform("io.lettuce.core.DefaultConnectionFuture", DefaultConnectionFutureTransform.class);
    }

    public static class DefaultConnectionFutureTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(EndPointAccessor.class);

            // Attach endpoint
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("get", "join"))) {
                method.addScopedInterceptor(AttachEndPointInterceptor.class, LettuceConstants.REDIS_SCOPE);
            }

            return target.toBytecode();
        }
    }

    private void addStatefulRedisConnection() {
        addStatefulRedisConnection("io.lettuce.core.StatefulRedisConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.pubsub.StatefulRedisPubSubConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.pubsub.StatefulRedisClusterPubSubConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.masterslave.StatefulRedisMasterSlaveConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.sentinel.StatefulRedisSentinelConnectionImpl");
    }

    private void addStatefulRedisConnection(final String className) {
        transformTemplate.transform(className, AddEndPointAccessorTransform.class);
    }

    public static class AddEndPointAccessorTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            target.addField(EndPointAccessor.class);
            return target.toBytecode();
        }
    }

    private void addRedisCommands(final LettucePluginConfig config) {
        // Commands
        addAbstractRedisCommands("io.lettuce.core.AbstractRedisAsyncCommands", AbstractRedisCommandsTransform.class, true);

        addAbstractRedisCommands("io.lettuce.core.RedisAsyncCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.cluster.RedisAdvancedClusterAsyncCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.cluster.RedisClusterPubSubAsyncCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.pubsub.RedisPubSubAsyncCommandsImpl", AbstractRedisCommandsTransform.class, false);

        // Reactive
        addAbstractRedisCommands("io.lettuce.core.AbstractRedisReactiveCommands", AbstractRedisCommandsTransform.class, true);

        addAbstractRedisCommands("io.lettuce.core.cluster.RedisAdvancedClusterReactiveCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.cluster.RedisClusterPubSubReactiveCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.pubsub.RedisPubSubReactiveCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.RedisReactiveCommandsImpl", AbstractRedisCommandsTransform.class, false);
        addAbstractRedisCommands("io.lettuce.core.sentinel.RedisSentinelReactiveCommandsImpl", AbstractRedisCommandsTransform.class, false);
    }

    private void addAbstractRedisCommands(final String className, Class<? extends TransformCallback> transformCallback, boolean getter) {
        transformTemplate.transform(className, transformCallback, new Object[]{getter}, new Class[] {boolean.class});
    }

    public static class AbstractRedisCommandsTransform implements TransformCallback {
        private final boolean getter;

        public AbstractRedisCommandsTransform(boolean getter) {
            this.getter = getter;
        }

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            if (getter) {
                target.addGetter(StatefulConnectionGetter.class, "connection");
            }
            final LettuceMethodNameFilter lettuceMethodNameFilter = new LettuceMethodNameFilter();
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(lettuceMethodNameFilter, MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                try {
                    method.addScopedInterceptor(LettuceMethodInterceptor.class, LettuceConstants.REDIS_SCOPE);
                } catch (Exception e) {
                    final PLogger logger = PLoggerFactory.getLogger(this.getClass());
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method {}", method, e);
                    }
                }
            }
            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}