/**
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

import static com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassConditions.*;

import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.BaseClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.ConditionalClassFileTransformerSetup;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerBuilder;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerExceptionHandler;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MethodTransformerProperty;
import com.navercorp.pinpoint.plugin.arcus.filter.ArcusMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.FrontCacheMemcachedMethodFilter;
import com.navercorp.pinpoint.plugin.arcus.filter.MemcachedMethodFilter;

/**
 * 
 * @author jaehong.kim
 *
 */
public class ArcusPlugin implements ProfilerPlugin, ArcusConstants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    @Override
    public void setup(ProfilerPluginContext context) {
        ArcusPluginConfig config = new ArcusPluginConfig(context.getConfig());
        context.setAttribute(ArcusConstants.ATTRIBUTE_CONFIG, config);

        boolean arcus = config.isArcus();
        boolean memcached = config.isMemcached();

        if (arcus) {
            addArcusClientEditor(context, config);
            addCollectionFutureEditor(context);
            addFrontCacheGetFutureEditor(context);
            addFrontCacheMemcachedClientEditor(context, config);
            addCacheManagerEditor(context);

            // add none operation future. over 1.5.4
            addBTreeStoreGetFutureEditor(context);
            addCollectionGetBulkFutureEditor(context);
            addSMGetFutureFutureEditor(context);
        }

        if (arcus || memcached) {
            addMemcachedClientEditor(context, config);

            addBaseOperationImplEditor(context);
            addGetFutureEditor(context);
            addOperationFutureEditor(context);
            // add none operation future.
            addImmediateFutureEditor(context);
            addBulkGetFutureEditor(context);
        }
    }

    private void addArcusClientEditor(ProfilerPluginContext context, final ArcusPluginConfig config) {
        final ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.ArcusClient");
        builder.conditional(hasMethod("addOp", "net.spy.memcached.ops.Operation", "java.lang.String", "net.spy.memcached.ops.Operation"), new ConditionalClassFileTransformerSetup() {
            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                boolean traceKey = config.isArcusKeyTrace();

                conditional.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.SetCacheManagerInterceptor");

                MethodTransformerBuilder mb = conditional.editMethods(new ArcusMethodFilter());
                mb.exceptionHandler(new MethodTransformerExceptionHandler() {
                    public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Throwable {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                        }
                    }
                });
                mb.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);
            }
        });

        context.addClassFileTransformer(builder.build());
    }

    private void addCacheManagerEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.CacheManager");
        builder.injectMetadata(METADATA_SERVICE_CODE);
        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.CacheManagerConstructInterceptor");

        context.addClassFileTransformer(builder.build());
    }

    private void addBaseOperationImplEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.protocol.BaseOperationImpl");
        builder.injectMetadata(METADATA_SERVICE_CODE);

        context.addClassFileTransformer(builder.build());
    }

    private void addFrontCacheGetFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.plugin.FrontCacheGetFuture");
        builder.injectMetadata(MEATDATA_CACHE_NAME);
        builder.injectMetadata(METADATA_CACHE_KEY);

        builder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureConstructInterceptor");

        MethodTransformerBuilder mb2 = builder.editMethod("get", "long", "java.util.concurrent.TimeUnit");
        mb2.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");

        MethodTransformerBuilder mb3 = builder.editMethod("get");
        mb3.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");

        context.addClassFileTransformer(builder.build());
    }

    private void addFrontCacheMemcachedClientEditor(ProfilerPluginContext context, final ArcusPluginConfig config) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.plugin.FrontCacheMemcachedClient");

        boolean traceKey = config.isMemcachedKeyTrace();
        MethodTransformerBuilder mb = builder.editMethods(new FrontCacheMemcachedMethodFilter());
        mb.exceptionHandler(new MethodTransformerExceptionHandler() {
            public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Throwable {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                }
            }
        });
        mb.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);

        context.addClassFileTransformer(builder.build());
    }

    private void addMemcachedClientEditor(ProfilerPluginContext context, final ArcusPluginConfig config) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.MemcachedClient");
        builder.conditional(hasDeclaredMethod("addOp", "java.lang.String", "net.spy.memcached.ops.Operation"), new ConditionalClassFileTransformerSetup() {
            @Override
            public void setup(ConditionalClassFileTransformerBuilder conditional) {
                conditional.injectMetadata(METADATA_SERVICE_CODE);
                conditional.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.AddOpInterceptor");
            }
        });

        boolean traceKey = config.isMemcachedKeyTrace();
        MethodTransformerBuilder mb2 = builder.editMethods(new MemcachedMethodFilter());
        mb2.exceptionHandler(new MethodTransformerExceptionHandler() {
            public void handle(String targetClassName, String targetMethodName, String[] targetMethodParameterTypes, Throwable exception) throws Throwable {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unsupported method " + targetClassName + "." + targetMethodName, exception);
                }
            }
        });
        mb2.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.ApiInterceptor", traceKey);

        context.addClassFileTransformer(builder.build());
    }

    private void injectFutureInterceptor(ProfilerPluginContext context, BaseClassFileTransformerBuilder builder) {
        builder.injectMetadata(ArcusConstants.METADATA_OPERATION);
        builder.injectMetadata(ArcusConstants.METADATA_ASYNC_TRACE_ID);

        // setOperation
        final MethodTransformerBuilder setOperationMethodBuilder = builder.editMethod("setOperation", "net.spy.memcached.ops.Operation");
        setOperationMethodBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        setOperationMethodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor");

        // cancel, get, set
        final MethodTransformerBuilder methodBuilder = builder.editMethods(MethodFilters.name("cancel", "get", "set", "signalComplete"));
        methodBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureGetInterceptor");
    }

    private void addCollectionFutureEditor(ProfilerPluginContext context) {
        final ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.CollectionFuture");
        injectFutureInterceptor(context, builder);
        context.addClassFileTransformer(builder.build());
    }

    private void addGetFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.GetFuture");
        injectFutureInterceptor(context, builder);

        context.addClassFileTransformer(builder.build());
    }

    private void addOperationFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.OperationFuture");
        injectFutureInterceptor(context, builder);

        context.addClassFileTransformer(builder.build());
    }

    private void addImmediateFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.ImmediateFuture");
        injectFutureInternalInterceptor(context, builder);
        context.addClassFileTransformer(builder.build());
    }

    private void addBulkGetFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.BulkGetFuture");
        injectFutureInternalInterceptor(context, builder);
        context.addClassFileTransformer(builder.build());
    }

    private void addBTreeStoreGetFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.BTreeStoreAndGetFuture");
        injectFutureInternalInterceptor(context, builder);
        context.addClassFileTransformer(builder.build());
    }

    private void addCollectionGetBulkFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.CollectionGetBulkFuture");
        injectFutureInternalInterceptor(context, builder);
        context.addClassFileTransformer(builder.build());
    }

    private void addSMGetFutureFutureEditor(ProfilerPluginContext context) {
        ClassFileTransformerBuilder builder = context.getClassFileTransformerBuilder("net.spy.memcached.internal.SMGetFuture");
        injectFutureInternalInterceptor(context, builder);
        context.addClassFileTransformer(builder.build());
    }

    private void injectFutureInternalInterceptor(ProfilerPluginContext context, BaseClassFileTransformerBuilder builder) {
        builder.injectMetadata(ArcusConstants.METADATA_ASYNC_TRACE_ID);

        // cancel, get
        final MethodTransformerBuilder methodBuilder = builder.editMethods(MethodFilters.name("cancel", "get"));

        methodBuilder.property(MethodTransformerProperty.IGNORE_IF_NOT_EXIST);
        methodBuilder.injectInterceptor("com.navercorp.pinpoint.plugin.arcus.interceptor.FutureInternalMethodInterceptor");
    }
}