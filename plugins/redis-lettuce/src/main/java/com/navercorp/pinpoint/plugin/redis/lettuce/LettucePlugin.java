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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
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
import com.navercorp.pinpoint.bootstrap.plugin.reactor.CoreSubscriberConstructorInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoOperatorSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorContextAccessor;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.AttachEndPointInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.LettuceMethodInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RedisClientConstructorInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RedisCluserClientConnectStatefulInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RedisClusterClientConstructorInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RedisSubscriberInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RunnableNewInstanceInterceptor;
import com.navercorp.pinpoint.plugin.redis.lettuce.interceptor.RunnableRunInterceptor;

import java.security.ProtectionDomain;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

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
            logger.info("{} config:{}", this.getClass().getSimpleName(), config);
        }

        // Set endpoint
        addRedisClient();
        // Attach endpoint
        addStatefulRedisConnection();
        // Commands
        addRedisCommands(config);
        addReactive();
    }

    private void addRedisClient() {
        transformTemplate.transform("io.lettuce.core.RedisClient", RedisClientTransform.class);
        transformTemplate.transform("io.lettuce.core.cluster.RedisClusterClient", RedisClientTransform.class);
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

            // Set cluster endpoint
            final InstrumentMethod clusterConstructor = target.getConstructor("io.lettuce.core.resource.ClientResources", "java.lang.Iterable");
            if (clusterConstructor != null) {
                clusterConstructor.addInterceptor(RedisClusterClientConstructorInterceptor.class);
            }

            // 5.0
            InstrumentMethod newStatefulRedisConnectionMethod = target.getDeclaredMethod("newStatefulRedisConnection", "io.lettuce.core.RedisChannelWriter", "io.lettuce.core.codec.RedisCodec", "java.time.Duration");
            if (newStatefulRedisConnectionMethod == null) {
                // 6.x
                newStatefulRedisConnectionMethod = target.getDeclaredMethod("newStatefulRedisConnection", "io.lettuce.core.RedisChannelWriter", "io.lettuce.core.protocol.PushHandler", "io.lettuce.core.codec.RedisCodec", "java.time.Duration");
            }
            if (newStatefulRedisConnectionMethod != null) {
                newStatefulRedisConnectionMethod.addInterceptor(AttachEndPointInterceptor.class);
            }
            final InstrumentMethod newStatefulRedisPubSubConnectionMethod = target.getDeclaredMethod("newStatefulRedisPubSubConnection", "io.lettuce.core.pubsub.PubSubEndpoint", "io.lettuce.core.RedisChannelWriter", "io.lettuce.core.codec.RedisCodec", "java.time.Duration");
            if (newStatefulRedisPubSubConnectionMethod != null) {
                newStatefulRedisPubSubConnectionMethod.addInterceptor(AttachEndPointInterceptor.class);
            }
            final InstrumentMethod newStatefulRedisSentinelConnectionMethod = target.getDeclaredMethod("newStatefulRedisSentinelConnection", "io.lettuce.core.RedisChannelWriter", "io.lettuce.core.codec.RedisCodec", "java.time.Duration");
            if (newStatefulRedisSentinelConnectionMethod != null) {
                newStatefulRedisSentinelConnectionMethod.addInterceptor(AttachEndPointInterceptor.class);
            }

            // Cluster
            // 5.0
            // private <K, V, T extends RedisChannelHandler<K, V>, S> ConnectionFuture<S> connectStatefulAsync(T connection, DefaultEndpoint endpoint, RedisURI connectionSettings, Supplier<SocketAddress> socketAddressSupplier, Supplier<CommandHandler> commandHandlerSupplier)
            InstrumentMethod connectStatefulAsyncMethod = target.getDeclaredMethod("connectStatefulAsync", "io.lettuce.core.RedisChannelHandler", "io.lettuce.core.protocol.DefaultEndpoint", "io.lettuce.core.RedisURI", "java.util.function.Supplier", "java.util.function.Supplier");
            if (connectStatefulAsyncMethod == null) {
                // 5.1
                // private <K, V, T extends RedisChannelHandler<K, V>, S> ConnectionFuture<S> connectStatefulAsync(T connection, RedisCodec<K, V> codec, DefaultEndpoint endpoint, RedisURI connectionSettings, Mono<SocketAddress> socketAddressSupplier, Supplier<CommandHandler> commandHandlerSupplier)
                connectStatefulAsyncMethod = target.getDeclaredMethod("connectStatefulAsync", "io.lettuce.core.RedisChannelHandler", "io.lettuce.core.codec.RedisCodec", "io.lettuce.core.protocol.DefaultEndpoint", "io.lettuce.core.RedisURI", "reactor.core.publisher.Mono", "java.util.function.Supplier");
            }
            if (connectStatefulAsyncMethod == null) {
                // 6.0, StatefulRedisConnectionImpl
                // private <K, V, T extends StatefulRedisConnectionImpl<K, V>, S> ConnectionFuture<S> connectStatefulAsync(T connection, DefaultEndpoint endpoint, RedisURI connectionSettings, Mono<SocketAddress> socketAddressSupplier, Supplier<CommandHandler> commandHandlerSupplier)
                connectStatefulAsyncMethod = target.getDeclaredMethod("connectStatefulAsync", "io.lettuce.core.StatefulRedisConnectionImpl", "io.lettuce.core.protocol.DefaultEndpoint", "io.lettuce.core.RedisURI", "reactor.core.publisher.Mono", "java.util.function.Supplier");
            }
            if (connectStatefulAsyncMethod == null) {
                // 6.0, StatefulRedisClusterConnectionImpl
                // private <K, V, T extends StatefulRedisClusterConnectionImpl<K, V>, S> ConnectionFuture<S> connectStatefulAsync(T connection, DefaultEndpoint endpoint, RedisURI connectionSettings, Mono<SocketAddress> socketAddressSupplier, Supplier<CommandHandler> commandHandlerSupplier)
                connectStatefulAsyncMethod = target.getDeclaredMethod("connectStatefulAsync", "io.lettuce.core.cluster.StatefulRedisClusterConnectionImpl", "io.lettuce.core.protocol.DefaultEndpoint", "io.lettuce.core.RedisURI", "reactor.core.publisher.Mono", "java.util.function.Supplier");
            }
            if (connectStatefulAsyncMethod != null) {
                connectStatefulAsyncMethod.addInterceptor(RedisCluserClientConnectStatefulInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    private void addStatefulRedisConnection() {
        addStatefulRedisConnection("io.lettuce.core.StatefulRedisConnectionImpl");
        addStatefulRedisConnection("io.lettuce.core.cluster.StatefulRedisClusterConnectionImpl");
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
        transformTemplate.transform(className, transformCallback, new Object[]{getter}, new Class[]{boolean.class});
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

    private void addReactive() {
        transformTemplate.transform("io.lettuce.core.RedisPublisher", RedisPublisherTransform.class);
        transformTemplate.transform("io.lettuce.core.RedisPublisher$ImmediateSubscriber", RedisSubscriberTransform.class);
        transformTemplate.transform("io.lettuce.core.RedisPublisher$PublishOnSubscriber", RedisSubscriberTransform.class);
        transformTemplate.transform("io.lettuce.core.RedisPublisher$OnNext", RedisPublisherOnNextTransform.class);
        transformTemplate.transform("io.lettuce.core.RedisPublisher$OnComplete", RedisPublisherOnCompleteTransform.class);
    }

    public static class RedisPublisherTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            final InstrumentMethod subscribeMethod = target.getDeclaredMethod("subscribe", "org.reactivestreams.Subscriber");
            if (subscribeMethod != null) {
                subscribeMethod.addInterceptor(FluxAndMonoOperatorSubscribeInterceptor.class, va(LettuceConstants.REDIS_LETTUCE_INTERNAL));
            }

            return target.toBytecode();
        }
    }

    public static class RedisSubscriberTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);
            target.addField(ReactorContextAccessor.class);

            for (InstrumentMethod constructorMethod : target.getDeclaredConstructors()) {
                final String[] parameterTypes = constructorMethod.getParameterTypes();
                if (ArrayUtils.hasLength(parameterTypes)) {
                    constructorMethod.addInterceptor(CoreSubscriberConstructorInterceptor.class);
                }
            }

            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("onNext", "onError", "onComplete"))) {
                method.addInterceptor(RedisSubscriberInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class RedisPublisherOnNextTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            final InstrumentMethod newInstanceMethod = target.getDeclaredMethod("newInstance", "java.lang.Object", "org.reactivestreams.Subscriber");
            if (newInstanceMethod != null) {
                newInstanceMethod.addInterceptor(RunnableNewInstanceInterceptor.class);
            }

            final InstrumentMethod runMethod = target.getDeclaredMethod("run");
            if (runMethod != null) {
                runMethod.addInterceptor(RunnableRunInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class RedisPublisherOnCompleteTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            // Async Object
            target.addField(AsyncContextAccessor.class);

            // static OnComplete newInstance(Throwable signal, Subscriber<?> subscriber)
            final InstrumentMethod newInstanceMethod = target.getDeclaredMethod("newInstance", "java.lang.Throwable", "org.reactivestreams.Subscriber");
            if (newInstanceMethod != null) {
                newInstanceMethod.addInterceptor(RunnableNewInstanceInterceptor.class);
            }
            // static OnComplete newInstance(Subscriber<?> subscriber)
            final InstrumentMethod newInstanceMethod2 = target.getDeclaredMethod("newInstance", "org.reactivestreams.Subscriber");
            if (newInstanceMethod2 != null) {
                newInstanceMethod2.addInterceptor(RunnableNewInstanceInterceptor.class);
            }

            final InstrumentMethod runMethod = target.getDeclaredMethod("run");
            if (runMethod != null) {
                runMethod.addInterceptor(RunnableRunInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}