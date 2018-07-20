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
        transformTemplate.transform("net.spy.memcached.ArcusClient", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (target.hasMethod("addOp", "java.lang.String", "net.spy.memcached.ops.Operation")) {
                    boolean traceKey = config.isArcusKeyTrace();

                    final InstrumentMethod setCacheManagerMethod = InstrumentUtils.findMethod(target, "setCacheManager", "net.spy.memcached.CacheManager");
                    setCacheManagerMethod.addInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.SetCacheManagerInterceptor");

                    for (InstrumentMethod m : target.getDeclaredMethods(new ArcusMethodFilter())) {
                        try {
                            m.addScopedInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", va(traceKey), ArcusConstants.ARCUS_SCOPE);
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
        });
    }

    private void addCacheManagerEditor() {
        transformTemplate.transform("net.spy.memcached.CacheManager", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField("com.navercorp.pinpoint.plugin.arcus.ServiceCodeAccessor");

                final InstrumentMethod constructorMethod = InstrumentUtils.findConstructor(target, "java.lang.String", "java.lang.String", "net.spy.memcached.ConnectionFactoryBuilder", "java.util.concurrent.CountDownLatch", "int", "int");
                constructorMethod.addInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.CacheManagerConstructInterceptor");

                return target.toBytecode();
            }

        });
    }

    private void addBaseOperationImplEditor() {
        transformTemplate.transform("net.spy.memcached.protocol.BaseOperationImpl", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                target.addField("com.navercorp.pinpoint.plugin.arcus.ServiceCodeAccessor");
                return target.toBytecode();
            }

        });
    }

    private void addFrontCacheGetFutureEditor() {
        transformTemplate.transform("net.spy.memcached.plugin.FrontCacheGetFuture", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                target.addField("com.navercorp.pinpoint.plugin.arcus.CacheNameAccessor");
                target.addField("com.navercorp.pinpoint.plugin.arcus.CacheKeyAccessor");

                final InstrumentMethod constructorMethod = InstrumentUtils.findConstructor(target, "net.sf.ehcache.Element");
                constructorMethod.addInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureConstructInterceptor");

                final InstrumentMethod get0 = InstrumentUtils.findMethod(target, "get", "long", "java.util.concurrent.TimeUnit");
                get0.addScopedInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor", ArcusConstants.ARCUS_SCOPE);

                final InstrumentMethod get1 = InstrumentUtils.findMethod(target, "get");
                get1.addScopedInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor", ArcusConstants.ARCUS_SCOPE);

                return target.toBytecode();
            }

        });
    }

    private void addFrontCacheMemcachedClientEditor(final ArcusPluginConfig config) {
        transformTemplate.transform("net.spy.memcached.plugin.FrontCacheMemcachedClient", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
                boolean traceKey = config.isMemcachedKeyTrace();

                for (InstrumentMethod m : target.getDeclaredMethods(new FrontCacheMemcachedMethodFilter())) {
                    try {
                        m.addScopedInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", va(traceKey), ArcusConstants.ARCUS_SCOPE);
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + className + "." + m.getName(), e);
                        }
                    }
                }

                return target.toBytecode();
            }

        });
    }

    private void addMemcachedClientEditor(final ArcusPluginConfig config) {
        transformTemplate.transform("net.spy.memcached.MemcachedClient", new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

                if (target.hasDeclaredMethod("addOp", "java.lang.String", "net.spy.memcached.ops.Operation")) {
                    target.addField("com.navercorp.pinpoint.plugin.arcus.ServiceCodeAccessor");
                    final InstrumentMethod addOpMethod = InstrumentUtils.findMethod(target, "addOp", "java.lang.String", "net.spy.memcached.ops.Operation");
                    addOpMethod.addInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.AddOpInterceptor");
                }

                boolean traceKey = config.isMemcachedKeyTrace();

                for (InstrumentMethod m : target.getDeclaredMethods(new FrontCacheMemcachedMethodFilter())) {
                    try {
                        m.addScopedInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", va(traceKey), ArcusConstants.ARCUS_SCOPE);
                    } catch (Exception e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + className + "." + m.getName(), e);
                        }
                    }
                }

                return target.toBytecode();
            }

        });
    }

    private static final TransformCallback FUTURE_TRANSFORMER = new TransformCallback() {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField("com.navercorp.pinpoint.plugin.arcus.OperationAccessor");
            target.addField(AsyncContextAccessor.class.getName());

            // setOperation
            InstrumentMethod setOperation = target.getDeclaredMethod("setOperation", "net.spy.memcached.ops.Operation");
            if (setOperation != null) {
                setOperation.addInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor");
            }

            // cancel, get, set
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("cancel", "get", "set", "signalComplete"))) {
                m.addScopedInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureGetInterceptor", ArcusConstants.ARCUS_FUTURE_SCOPE);
            }

            return target.toBytecode();
        }
    };

    private static final TransformCallback FUTURE_SET_OPERATION_TRANSFORMER = new TransformCallback() {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            target.addField("com.navercorp.pinpoint.plugin.arcus.OperationAccessor");

            // setOperation
            InstrumentMethod setOperation = target.getDeclaredMethod("setOperation", "net.spy.memcached.ops.Operation");
            if (setOperation != null) {
                setOperation.addInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor");
            }

            return target.toBytecode();
        }
    };

    private static final TransformCallback INTERNAL_FUTURE_TRANSFORMER = new TransformCallback() {

        @Override
        public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
            InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);

            target.addField(AsyncContextAccessor.class.getName());
            
            // cancel, get, set
            for (InstrumentMethod m : target.getDeclaredMethods(MethodFilters.name("cancel", "get"))) {
                m.addScopedInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureInternalMethodInterceptor", ArcusConstants.ARCUS_FUTURE_SCOPE);
            }

            return target.toBytecode();
        }
    };


    private void addCollectionFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.CollectionFuture", FUTURE_TRANSFORMER);
        } else {
            transformTemplate.transform("net.spy.memcached.internal.CollectionFuture", FUTURE_SET_OPERATION_TRANSFORMER);
        }
    }

    private void addGetFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.GetFuture", FUTURE_TRANSFORMER);
        } else {
            transformTemplate.transform("net.spy.memcached.internal.GetFuture", FUTURE_SET_OPERATION_TRANSFORMER);
        }
    }

    private void addOperationFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.OperationFuture", FUTURE_TRANSFORMER);
        } else {
            transformTemplate.transform("net.spy.memcached.internal.OperationFuture", FUTURE_SET_OPERATION_TRANSFORMER);
        }
    }

    private void addImmediateFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.ImmediateFuture", INTERNAL_FUTURE_TRANSFORMER);
        }
    }

    private void addBulkGetFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.BulkGetFuture", INTERNAL_FUTURE_TRANSFORMER);
        }
    }

    private void addBTreeStoreGetFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.BTreeStoreAndGetFuture", INTERNAL_FUTURE_TRANSFORMER);
        }
    }

    private void addCollectionGetBulkFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.CollectionGetBulkFuture", INTERNAL_FUTURE_TRANSFORMER);
        }
    }

    private void addSMGetFutureFutureEditor(boolean async) {
        if (async) {
            transformTemplate.transform("net.spy.memcached.internal.SMGetFuture", INTERNAL_FUTURE_TRANSFORMER);
        }
    }

    @Override
    public void setTransformTemplate(TransformTemplate transformTemplate) {
        this.transformTemplate = transformTemplate;
    }
}