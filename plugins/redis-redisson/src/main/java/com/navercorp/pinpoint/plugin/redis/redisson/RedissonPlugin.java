/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.redis.redisson;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.plugin.redis.redisson.interceptor.CommandAsyncServiceMethodInterceptor;
import com.navercorp.pinpoint.plugin.redis.redisson.interceptor.ReactiveMethodInterceptor;
import com.navercorp.pinpoint.plugin.redis.redisson.interceptor.RedissonMethodInterceptor;

import java.security.ProtectionDomain;

/**
 * @author jaehong.kim
 */
public class RedissonPlugin implements ProfilerPlugin, TransformTemplateAware {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TransformTemplate transformTemplate;


    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final RedissonPluginConfig config = new RedissonPluginConfig(context.getConfig());
        if (!config.isEnable()) {
            logger.info("Disable RedissonPlugin");
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Enable RedissonPlugin. version range=[3.10.0, 3.10.4], config={}", config);
        }

        this.transformTemplate.transform("org.redisson.Redisson", RedissionMethodTransform.class);

        this.transformTemplate.transform("org.redisson.RedissonStream", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonBinaryStream", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonGeo", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonBucket", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonRateLimiter", RedissionMethodTransform.class);

        this.transformTemplate.transform("org.redisson.RedissonBuckets", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonHyperLogLog", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonList", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonListMultimap", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonLocalCachedMap", RedissionMethodTransform.class);

        this.transformTemplate.transform("org.redisson.RedissonMap", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonSetMultimap", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonSetMultimapCache", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonListMultimapCache", RedissionMethodTransform.class);

        this.transformTemplate.transform("org.redisson.RedissonSetCache", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonMapCache", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonLock", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonFairLock", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonReadWriteLock", RedissionMethodTransform.class);

        this.transformTemplate.transform("org.redisson.RedissonScript", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonExecutorService", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonRemoteService", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonSortedSet", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonScoredSortedSet", RedissionMethodTransform.class);

        this.transformTemplate.transform("org.redisson.RedissonLexSortedSet", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonTopic", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonPatternTopic", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonDelayedQueue", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonQueue", RedissionMethodTransform.class);

        this.transformTemplate.transform("org.redisson.RedissonBlockingQueue", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonDeque", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonBlockingDeque", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonAtomicLong", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonLongAdder", RedissionMethodTransform.class);

        this.transformTemplate.transform("org.redisson.RedissonDoubleAdder", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonAtomicDouble", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonCountDownLatch", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonBitSet", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonSemaphore", RedissionMethodTransform.class);

        this.transformTemplate.transform("org.redisson.RedissonPermitExpirableSemaphore", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonBloomFilter", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonKeys", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonBatch", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonLiveObjectService", RedissionMethodTransform.class);


        this.transformTemplate.transform("org.redisson.RedissonPriorityQueue", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonPriorityBlockingQueue", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonPriorityBlockingDeque", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.RedissonPriorityDeque", RedissionMethodTransform.class);

        // Rx
        this.transformTemplate.transform("org.redisson.RedissonRx", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.rx.RxProxyBuilder$1$1", RedissionMethodTransform.class);

        // Reactive
        this.transformTemplate.transform("org.redisson.RedissonReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.ReactiveProxyBuilder$1", ReactiveProxyBuilderTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.ReactiveProxyBuilder$1$1", ReactiveProxyBuilderTransform.class);

        this.transformTemplate.transform("org.redisson.reactive.RedissonReadWriteLockReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonMapCacheReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonListReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonListMultimapReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonSetMultimapReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonMapReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonSetReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonScoredSortedSetReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonLexSortedSetReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonTopicReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonBlockingQueueReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonSetCacheReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonBatchReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonKeysReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonTransactionReactive", RedissionMethodTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.RedissonBlockingDequeReactive", RedissionMethodTransform.class);

        // Command
        this.transformTemplate.transform("org.redisson.command.CommandAsyncService", RedissonCommandTransform.class);
        this.transformTemplate.transform("org.redisson.command.CommandBatchService", RedissonCommandTransform.class);
        this.transformTemplate.transform("org.redisson.command.CommandSyncService", RedissonCommandTransform.class);

        this.transformTemplate.transform("org.redisson.reactive.CommandReactiveService", RedissonCommandTransform.class);
        this.transformTemplate.transform("org.redisson.reactive.CommandReactiveBatchService", RedissonCommandTransform.class);

        this.transformTemplate.transform("org.redisson.rx.CommandRxBatchService", RedissonCommandTransform.class);
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }

    public static class RedissionMethodTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.chain(RedissonMethodNameFilters.exclude("getEvictionScheduler", "getCommandExecutor", "getConnectionManager", "create", "createRx", "createReactive", "getConfig", "getNodesGroup", "getClusterNodesGroup", "isShutdown", "isShuttingDown", "enableRedissonReferenceSupport"), MethodFilters.modifierNot(MethodFilters.SYNTHETIC)))) {
                method.addScopedInterceptor(RedissonMethodInterceptor.class, RedissonConstants.REDISSON_SCOPE);
            }

            return target.toBytecode();
        }
    }

    public static class RedissonCommandTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.name("async"))) {
                method.addInterceptor(CommandAsyncServiceMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

    public static class ReactiveProxyBuilderTransform implements TransformCallback {
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            final InstrumentClass target = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

            final InstrumentMethod executeMethod = target.getDeclaredMethod("execute", "java.lang.reflect.Method", "java.lang.Object", "java.lang.Object[]");
            if (executeMethod != null) {
                executeMethod.addInterceptor(ReactiveMethodInterceptor.class);
            }

            return target.toBytecode();
        }
    }

}