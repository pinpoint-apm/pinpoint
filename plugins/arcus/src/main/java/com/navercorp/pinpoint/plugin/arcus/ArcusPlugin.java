/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.arcus;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplate;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformTemplateAware;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginSetupContext;
import com.navercorp.pinpoint.bootstrap.plugin.util.InstrumentUtils;
import com.navercorp.pinpoint.plugin.arcus.filter.ArcusMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.FrontCacheMemcachedMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.interceptor.AddOpInterceptor;
import com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor;
import com.navercorp.pinpoint.plugin.arcus.interceptor.CacheManagerConstructInterceptor;
import com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureConstructInterceptor;
import com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor;
import com.navercorp.pinpoint.plugin.arcus.interceptor.FutureGetInterceptor;
import com.navercorp.pinpoint.plugin.arcus.interceptor.FutureInternalMethodInterceptor;
import com.navercorp.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor;
import com.navercorp.pinpoint.plugin.arcus.interceptor.SetCacheManagerInterceptor;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author jaehong.kim
 */
public class ArcusPlugin implements ProfilerPlugin, TransformTemplateAware {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private TransformTemplate transformTemplate;

    @Override
    public void setup(ProfilerPluginSetupContext context) {
        final ArcusPluginConfig config = new ArcusPluginConfig(context.getConfig());
        logger.info("{} config:{}", this.getClass().getSimpleName(), config);

        final boolean arcus = config.isArcus();
        final boolean arcusAsync = config.isArcusAsync();
        final boolean memcached = config.isMemcached();
        final boolean memcachedAsync = config.isMemcachedAsync();

        if (arcus) {
            addArcusClientEditor(config);
            addCollectionFutureEditor(arcusAsync);
            addFrontCacheGetFutureEditor();
            addFrontCacheMemcachedClientEditor(config);
            addCacheManagerEditor();

            // add none operation future. over 1.5.4
            addBTreeStoreGetFutureEditor(arcusAsync);
            addCollectionGetBulkFutureEditor(arcusAsync);
            addSMGetFutureFutureEditor(arcusAsync);
        }

        if (arcus || memcached) {
            addMemcachedClientEditor(config);

            addBaseOperationImplEditor();
            final boolean async = arcusAsync || memcachedAsync;
            addGetFutureEditor(async);
            addOperationFutureEditor(async);
            // add none operation future.
            addImmediateFutureEditor(async);
            addBulkGetFutureEditor(async);
        }
    }

    private void addArcusClientEditor(final ArcusPluginConfig config) {
        transformTemplate.transform("net.spy.memcached.ArcusClient", ArcusClientTransform.class);
    }

    public static class ArcusClientTransform implements TransformCallback {
        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (target.hasMethod("addOp", "java.lang.String", "net.spy.memcached.ops.Operation")) {
                final ArcusPluginConfig config = new ArcusPluginConfig(instrumentor.getProfilerConfig());
                boolean traceKey = config.isArcusKeyTrace();

                final InstrumentMethod setCacheManagerMethod = InstrumentUtils.findMethod(target, "setCacheManager", "net.spy.memcached.CacheManager");
                setCacheManagerMethod.addInterceptor(SetCacheManagerInterceptor.class);

                for (InstrumentMethod m : target.getDeclaredMethods(new ArcusMethodFilter())) {
                    try {
                        m.addScopedInterceptor(ApiInterceptor.class, va(traceKey), ArcusConstants.ARCUS_SCOPE);
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + className + "." + m.getName(), e);
                        }
                    }
                }

                return target.toBytecode();
            } else {
                return null;
            }
        }
    }

    private void addCacheManagerEditor() {
        transformTemplate.transform("net.spy.memcached.CacheManager", CacheManagerTransform.class);
    }

    public static class CacheManagerTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(ServiceCodeAccessor.class);

            final InstrumentMethod constructorMethod = InstrumentUtils.findConstructor(target, "java.lang.String", "java.lang.String", "net.spy.memcached.ConnectionFactoryBuilder", "java.util.concurrent.CountDownLatch", "int", "int");
            constructorMethod.addInterceptor(CacheManagerConstructInterceptor.class);

            return target.toBytecode();
        }

    }

    private void addBaseOperationImplEditor() {
        transformTemplate.transform("net.spy.memcached.protocol.BaseOperationImpl", BaseOperationImplTransform.class);
    }

    public static class BaseOperationImplTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(ServiceCodeAccessor.class);
            return target.toBytecode();
        }

    }

    private void addFrontCacheGetFutureEditor() {
        transformTemplate.transform("net.spy.memcached.plugin.FrontCacheGetFuture", FrontCacheGetFutureTransform.class);
    }

    public static class FrontCacheGetFutureTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(CacheNameAccessor.class);
            target.addField(CacheKeyAccessor.class);

            final InstrumentMethod constructorMethod = InstrumentUtils.findConstructor(target, "net.sf.ehcache.Element");
            constructorMethod.addInterceptor(FrontCacheGetFutureConstructInterceptor.class);

            final InstrumentMethod get0 = InstrumentUtils.findMethod(target, "get", "long", "java.util.concurrent.TimeUnit");
            get0.addScopedInterceptor(FrontCacheGetFutureGetInterceptor.class, ArcusConstants.ARCUS_SCOPE);

            final InstrumentMethod get1 = InstrumentUtils.findMethod(target, "get");
            get1.addScopedInterceptor(FrontCacheGetFutureGetInterceptor.class, ArcusConstants.ARCUS_SCOPE);

            return target.toBytecode();
        }

    }

    private void addFrontCacheMemcachedClientEditor(final ArcusPluginConfig config) {
        transformTemplate.transform("net.spy.memcached.plugin.FrontCacheMemcachedClient", FrontCacheMemcachedClientTransform.class);
    }

    public static class FrontCacheMemcachedClientTransform implements TransformCallback {

        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            final ArcusPluginConfig config = new ArcusPluginConfig(instrumentor.getProfilerConfig());
            boolean traceKey = config.isMemcachedKeyTrace();

            for (InstrumentMethod m : target.getDeclaredMethods(new FrontCacheMemcachedMethodFilter())) {
                try {
                    m.addScopedInterceptor(ApiInterceptor.class, va(traceKey), ArcusConstants.ARCUS_SCOPE);
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + className + "." + m.getName(), e);
                    }
                }
            }

            return target.toBytecode();
        }

    }

    private void addMemcachedClientEditor(final ArcusPluginConfig config) {
        transformTemplate.transform("net.spy.memcached.MemcachedClient", MemcachedClientTransform.class);
    }

    public static class MemcachedClientTransform implements TransformCallback {

        private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            if (target.hasDeclaredMethod("addOp", "java.lang.String", "net.spy.memcached.ops.Operation")) {
                target.addField(ServiceCodeAccessor.class);
                final InstrumentMethod addOpMethod = InstrumentUtils.findMethod(target, "addOp", "java.lang.String", "net.spy.memcached.ops.Operation");
                addOpMethod.addInterceptor(AddOpInterceptor.class);
            }
            final ArcusPluginConfig config = new ArcusPluginConfig(instrumentor.getProfilerConfig());
            boolean traceKey = config.isMemcachedKeyTrace();

            for (InstrumentMethod m : target.getDeclaredMethods(new FrontCacheMemcachedMethodFilter())) {
                try {
                    m.addScopedInterceptor(ApiInterceptor.class, va(traceKey), ArcusConstants.ARCUS_SCOPE);
                } catch (Exception e) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Unsupported method " + className + "." + m.getName(), e);
                    }
                }
            }

            return target.toBytecode();
        }

    }

    public static class FutureTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(OperationAccessor.class);
            target.addField(AsyncContextAccessor.class);

            // setOperation
            InstrumentMethod setOperation = target.getDeclaredMethod("setOperation", "net.spy.memcached.ops.Operation");
            if (setOperation != null) {
                setOperation.addInterceptor(FutureSetOperationInterceptor.class);
            }

            // cancel, get, set
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("cancel", "get", "set", "signalComplete"))) {
                m.addScopedInterceptor(FutureGetInterceptor.class, ArcusConstants.ARCUS_FUTURE_SCOPE);
            }

            return target.toBytecode();
        }
    };

    public static class FutureSetOperationTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField(OperationAccessor.class);

            // setOperation
            InstrumentMethod setOperation = target.getDeclaredMethod("setOperation", "net.spy.memcached.ops.Operation");
            if (setOperation != null) {
                setOperation.addInterceptor(FutureSetOperationInterceptor.class);
            }

            return target.toBytecode();
        }
    };

    public static class InternalFutureTransform implements TransformCallback {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(AsyncContextAccessor.class);
            
            // cancel, get, set
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("cancel", "get"))) {
                m.addScopedInterceptor(FutureInternalMethodInterceptor.class, ArcusConstants.ARCUS_FUTURE_SCOPE);
            }

            return target.toBytecode();
        }
    };


    private void addCollectionFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.CollectionFuture", FutureTransform.class);
        } else {
            transformTemplate.transform("net.spy.memcached.internal.CollectionFuture", FutureSetOperationTransform.class);
        }
    }

    private void addGetFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.GetFuture", FutureTransform.class);
        } else {
            transformTemplate.transform("net.spy.memcached.internal.GetFuture", FutureSetOperationTransform.class);
        }
    }

    private void addOperationFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.OperationFuture", FutureTransform.class);
        } else {
            transformTemplate.transform("net.spy.memcached.internal.OperationFuture", FutureSetOperationTransform.class);
        }
    }

    private void addImmediateFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.ImmediateFuture", InternalFutureTransform.class);
        }
    }

    private void addBulkGetFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.BulkGetFuture", InternalFutureTransform.class);
        }
    }

    private void addBTreeStoreGetFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.BTreeStoreAndGetFuture", InternalFutureTransform.class);
        }
    }

    private void addCollectionGetBulkFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.CollectionGetBulkFuture", InternalFutureTransform.class);
        }
    }

    private void addSMGetFutureFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.SMGetFuture", InternalFutureTransform.class);
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}