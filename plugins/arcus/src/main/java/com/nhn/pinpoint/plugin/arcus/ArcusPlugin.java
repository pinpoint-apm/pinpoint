package com.nhn.pinpoint.plugin.arcus;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder.InterceptorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder.MetadataBuilder;
import com.nhn.pinpoint.bootstrap.plugin.Condition;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.nhn.pinpoint.plugin.arcus.filter.ArcusMethodFilter;
import com.nhn.pinpoint.plugin.arcus.filter.FrontCacheMemcachedMethodFilter;
import com.nhn.pinpoint.plugin.arcus.filter.MemcachedMethodFilter;

// TODO split arcus plugin and memcached plugin
public class ArcusPlugin implements ProfilerPlugin {
    @Override
    public List<ClassEditor> getClassEditors(ProfilerPluginContext context) {
        ArcusPluginConfig config = new ArcusPluginConfig(context.getConfig());
        
        boolean arcus = config.isArcus();
        boolean memcached = config.isMemcached();

        List<ClassEditor> editors = new ArrayList<ClassEditor>();

         if (arcus) {
            editors.add(getArcusClientEditor(context, config));
            editors.add(getCollectionFutureEditor(context));
        }
        
        if (arcus || memcached) {
            editors.add(getBaseOperationImplEditor(context));        
            editors.add(getCacheManagerEditor(context));
            
            editors.add(getGetFutureEditor(context));
            // TODO ImmedateFuture doesn't have setOperation(Operation) method.
//            editors.add(getImmediateFutureEditor(context));
            editors.add(getOperationFutureEditor(context));
            
            editors.add(getFrontCacheGetFutureEditor(context));
            editors.add(getFrontCacheMemcachedClientEditor(context, config));
            editors.add(getMemcachedClientEditor(context, config));
        }

        return editors;
    }
    
    private ClassEditor getArcusClientEditor(ProfilerPluginContext context, ArcusPluginConfig config) {
        boolean traceKey = config.isArcusKeyTrace();
        ClassEditorBuilder builder = context.newClassEditorBuilder();

        builder.edit("net.spy.memcached.ArcusClient");
        builder.when(new Condition() {
                @Override
                public boolean check(InstrumentClass target) {
                    return target.hasMethod("addOp", new String[] {"java.lang.String", "net.spy.memcached.ops.Operation"}, "net.spy.memcached.ops.Operation");
                }
        });
        
        InterceptorBuilder ib = builder.newInterceptorBuilder();
        ib.intercept("setCacheManager", "net.spy.memcached.CacheManager");
        ib.with("com.nhn.pinpoint.plugin.arcus.interceptor.SetCacheManagerInterceptor");
        
        InterceptorBuilder ib2 = builder.newInterceptorBuilder();
        ib2.interceptMethodsFilteredBy(new ArcusMethodFilter());
        ib2.with("com.nhn.pinpoint.plugin.arcus.interceptor.ApiInterceptor");
        ib2.constructedWith(traceKey);
        ib2.in(Constants.ARCUS_SCOPE);
        
        return builder.build();
    }
    
    private ClassEditor getCacheManagerEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();

        builder.edit("net.spy.memcached.CacheManager");
        
        MetadataBuilder mb = builder.newMetadataBuilder();
        mb.inject("com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor");
        
        InterceptorBuilder ib = builder.newInterceptorBuilder();
        ib.interceptConstructor("java.lang.String", "java.lang.String", "net.spy.memcached.ConnectionFactoryBuilder", "java.util.concurrent.CountDownLatch", "int", "int");
        ib.with("com.nhn.pinpoint.plugin.arcus.interceptor.CacheManagerConstructInterceptor");
        
        return builder.build();
    }

    
    private ClassEditor getBaseOperationImplEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        
        builder.edit("net.spy.memcached.protocol.BaseOperationImpl");
        
        MetadataBuilder mb = builder.newMetadataBuilder();
        mb.inject("com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor");

        return builder.build();
    }
    
    private ClassEditor getFrontCacheGetFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        
        builder.edit("net.spy.memcached.plugin.FrontCacheGetFuture");

        MetadataBuilder mb = builder.newMetadataBuilder();
        mb.inject("com.nhn.pinpoint.plugin.arcus.accessor.CacheNameAccessor");
        
        MetadataBuilder mb2 = builder.newMetadataBuilder();
        mb2.inject("com.nhn.pinpoint.plugin.arcus.accessor.CacheKeyAccessor");
        
        InterceptorBuilder ib = builder.newInterceptorBuilder();
        ib.interceptConstructor("net.sf.ehcache.Element");
        ib.with("com.nhn.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureConstructInterceptor");
        
        InterceptorBuilder ib2 = builder.newInterceptorBuilder();
        ib2.intercept("get", "long", "java.util.concurrent.TimeUnit");
        ib2.with("com.nhn.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");
        ib2.in(Constants.ARCUS_SCOPE);
        
        InterceptorBuilder ib3 = builder.newInterceptorBuilder();
        ib3.intercept("get");
        ib3.with("com.nhn.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor");
        ib3.in(Constants.ARCUS_SCOPE);
        
        return builder.build();
    }
    
    private ClassEditor getFrontCacheMemcachedClientEditor(ProfilerPluginContext context, ArcusPluginConfig config) {
        boolean traceKey = config.isMemcachedKeyTrace();
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        
        builder.edit("net.spy.memcached.plugin.FrontCacheMemcachedClient");
        builder.when(new Condition() {

            @Override
            public boolean check(InstrumentClass target) {
                return target.hasDeclaredMethod("putFrontCache", new String[] { "java.lang.String", "java.util.concurrent.Future", "long" });
            }
            
        });
        
        InterceptorBuilder ib = builder.newInterceptorBuilder();
        ib.interceptMethodsFilteredBy(new FrontCacheMemcachedMethodFilter());
        ib.with("com.nhn.pinpoint.plugin.arcus.interceptor.ApiInterceptor");
        ib.in(Constants.ARCUS_SCOPE);
        ib.constructedWith(traceKey);
                        
        return builder.build();
    }


    private ClassEditor getMemcachedClientEditor(ProfilerPluginContext context, ArcusPluginConfig config) {
        boolean traceKey = config.isMemcachedKeyTrace();
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        
        builder.edit("net.spy.memcached.MemcachedClient");
        builder.when(new Condition() {

            @Override
            public boolean check(InstrumentClass target) {
                return target.hasDeclaredMethod("addOp", new String[] { "java.lang.String", "net.spy.memcached.ops.Operation" });
            }
            
        });
        
        MetadataBuilder mb = builder.newMetadataBuilder();
        mb.inject("com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor");
        
        InterceptorBuilder ib = builder.newInterceptorBuilder();
        ib.intercept("addOp", "java.lang.String", "net.spy.memcached.ops.Operation");
        ib.with("com.nhn.pinpoint.plugin.arcus.interceptor.AddOpInterceptor");
        
        InterceptorBuilder ib2 = builder.newInterceptorBuilder();
        ib2.interceptMethodsFilteredBy(new MemcachedMethodFilter());
        ib2.with("com.nhn.pinpoint.plugin.arcus.interceptor.ApiInterceptor");
        ib2.in(Constants.ARCUS_SCOPE);
        ib2.constructedWith(traceKey);
                        
        return builder.build();
    }

    private ClassEditor getFutureEditor(ClassEditorBuilder builder) {
        MetadataBuilder mb = builder.newMetadataBuilder();
        mb.inject("com.nhn.pinpoint.plugin.arcus.accessor.OperationAccessor");
        
        InterceptorBuilder ib = builder.newInterceptorBuilder();
        ib.intercept("setOperation", "net.spy.memcached.ops.Operation");
        ib.with("com.nhn.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor");
        
        InterceptorBuilder ib2 = builder.newInterceptorBuilder();
        ib2.intercept("get", "long", "java.util.concurrent.TimeUnit");
        ib2.with("com.nhn.pinpoint.plugin.arcus.interceptor.FutureGetInterceptor");
        ib2.in(Constants.ARCUS_SCOPE);
        
        return builder.build();
    }
        
    private ClassEditor getCollectionFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.edit("net.spy.memcached.internal.CollectionFuture");
        return getFutureEditor(builder);
    }
    
    private ClassEditor getGetFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.edit("net.spy.memcached.internal.GetFuture");
        return getFutureEditor(builder);
    }

    private ClassEditor getOperationFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.edit("net.spy.memcached.internal.OperationFuture");
        return getFutureEditor(builder);
    }

    private ClassEditor getImmediateFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.edit("net.spy.memcached.internal.ImmediateFuture");
        return getFutureEditor(builder);
    }

}