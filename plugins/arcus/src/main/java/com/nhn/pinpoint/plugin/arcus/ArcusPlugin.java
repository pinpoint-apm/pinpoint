package com.nhn.pinpoint.plugin.arcus;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder;
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
        boolean arcus = context.getConfig().isArucs();
        boolean memcached = context.getConfig().isMemcached();

        List<ClassEditor> editors = new ArrayList<ClassEditor>();

         if (arcus) {
            editors.add(getArcusClientEditor(context));
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
            editors.add(getFrontCacheMemcachedClientEditor(context));
            editors.add(getMemcachedClientEditor(context));
        }

        return editors;
    }
    
    private ClassEditor getArcusClientEditor(ProfilerPluginContext context) {
        boolean traceArcusKey = context.getConfig().isArucsKeyTrace();
        ClassEditorBuilder builder = context.newClassEditorBuilder();

        builder.edit("net.spy.memcached.ArcusClient");
        builder.when(new Condition() {
                @Override
                public boolean check(InstrumentClass target) {
                    return target.hasMethod("addOp", new String[] {"java.lang.String", "net.spy.memcached.ops.Operation"}, "net.spy.memcached.ops.Operation");
                }
        });
        
        builder.intercept("setCacheManager", "net.spy.memcached.CacheManager")
                .with("com.nhn.pinpoint.plugin.arcus.interceptor.SetCacheManagerInterceptor");
        
        builder.interceptMethodsFilteredBy(new ArcusMethodFilter())
                .with("com.nhn.pinpoint.plugin.arcus.interceptor.ApiInterceptor")
                .in(Commons.ARCUS_SCOPE)
                .using(traceArcusKey ? Commons.ARCUS_KEY_EXTRACTOR_FACTORY : null);
        
        return builder.build();
    }
    
    private ClassEditor getCacheManagerEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();

        builder.edit("net.spy.memcached.CacheManager");
        
        builder.inject("com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor");
        
        builder.interceptConstructor("java.lang.String", "java.lang.String", "net.spy.memcached.ConnectionFactoryBuilder", "java.util.concurrent.CountDownLatch", "int", "int")
                .with("com.nhn.pinpoint.plugin.arcus.interceptor.CacheManagerConstructInterceptor");
        
        return builder.build();
    }

    
    private ClassEditor getBaseOperationImplEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        
        builder.edit("net.spy.memcached.protocol.BaseOperationImpl");
        
        builder.inject("com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor");

        return builder.build();
    }
    
    private ClassEditor getFrontCacheGetFutureEditor(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        
        builder.edit("net.spy.memcached.plugin.FrontCacheGetFuture");

        builder.inject("com.nhn.pinpoint.plugin.arcus.accessor.CacheNameAccessor");
        builder.inject("com.nhn.pinpoint.plugin.arcus.accessor.CacheKeyAccessor");
        
        builder.interceptConstructor("net.sf.ehcache.Element")
                .with("com.nhn.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureConstructInterceptor");
        builder.intercept("get", "long", "java.util.concurrent.TimeUnit")
                .with("com.nhn.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor")
                .in(Commons.ARCUS_SCOPE);
        builder.intercept("get")
                .with("com.nhn.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor")
                .in(Commons.ARCUS_SCOPE);
        
        return builder.build();
    }
    
    private ClassEditor getFrontCacheMemcachedClientEditor(ProfilerPluginContext context) {
        boolean traceArcusKey = context.getConfig().isArucsKeyTrace();
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        
        builder.edit("net.spy.memcached.plugin.FrontCacheMemcachedClient");
        builder.when(new Condition() {

            @Override
            public boolean check(InstrumentClass target) {
                return target.hasDeclaredMethod("putFrontCache", new String[] { "java.lang.String", "java.util.concurrent.Future", "long" });
            }
            
        });
        
        builder.interceptMethodsFilteredBy(new FrontCacheMemcachedMethodFilter())
            .with("com.nhn.pinpoint.plugin.arcus.interceptor.ApiInterceptor")
            .in(Commons.ARCUS_SCOPE)
            .using(traceArcusKey ? Commons.ARCUS_KEY_EXTRACTOR_FACTORY : null);
                        
        return builder.build();
    }


    private ClassEditor getMemcachedClientEditor(ProfilerPluginContext context) {
        boolean traceArcusKey = context.getConfig().isArucsKeyTrace();
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        
        builder.edit("net.spy.memcached.MemcachedClient");
        builder.when(new Condition() {

            @Override
            public boolean check(InstrumentClass target) {
                return target.hasDeclaredMethod("addOp", new String[] { "java.lang.String", "net.spy.memcached.ops.Operation" });
            }
            
        });
        
        
        builder.inject("com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor");
        
        builder.intercept("addOp", "java.lang.String", "net.spy.memcached.ops.Operation")
            .with("com.nhn.pinpoint.plugin.arcus.interceptor.AddOpInterceptor");
        
        builder.interceptMethodsFilteredBy(new MemcachedMethodFilter())
            .with("com.nhn.pinpoint.plugin.arcus.interceptor.ApiInterceptor")
            .in(Commons.ARCUS_SCOPE)
            .using(traceArcusKey ? Commons.ARCUS_KEY_EXTRACTOR_FACTORY : null);
                        
        return builder.build();
    }

    private ClassEditor getFutureEditor(ClassEditorBuilder builder) {
        builder.inject("com.nhn.pinpoint.plugin.arcus.accessor.OperationAccessor");
        
        builder.intercept("setOperation", "net.spy.memcached.ops.Operation")
                .with("com.nhn.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor");
        
        builder.intercept("get", "long", "java.util.concurrent.TimeUnit")
                .with("com.nhn.pinpoint.plugin.arcus.interceptor.FutureGetInterceptor")
                .in(Commons.ARCUS_SCOPE);
        
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