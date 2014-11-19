package com.nhn.pinpoint.plugin.arcus;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.instrument.MethodInfo;
import com.nhn.pinpoint.bootstrap.interceptor.ParameterExtractor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorFactoryMapping;
import com.nhn.pinpoint.bootstrap.plugin.Condition;
import com.nhn.pinpoint.bootstrap.plugin.ParameterExtractorFactory;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPlugin;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPluginContext;

// TODO split arcus plugin and memcached plugin
public class ArcusPlugin implements ProfilerPlugin {
    @Override
    public List<ClassEditorFactoryMapping> getClassEditorMappings(ProfilerPluginContext context) {
        boolean arcus = context.getConfig().isArucs();
        boolean memcached = context.getConfig().isMemcached();

        List<ClassEditorFactoryMapping> editors = new ArrayList<ClassEditorFactoryMapping>();

        if (arcus) {
            editors.add(new ClassEditorFactoryMapping("net.spy.memcached.ArcusClient", "com.nhn.pinpoint.plugin.arcus.ArcusClientEditorFactory"));
            editors.add(new ClassEditorFactoryMapping("net.spy.memcached.internal.CollectionFuture", "com.nhn.pinpoint.plugin.arcus.FutureEditorFactory"));
        }
        
        if (arcus || memcached) {
            editors.add(new ClassEditorFactoryMapping("net.spy.memcached.protocol.BaseOperationImpl", "com.nhn.pinpoint.plugin.arcus.BaseOperationImplEditorFactory"));        
            editors.add(new ClassEditorFactoryMapping("net.spy.memcached.CacheManager", "com.nhn.pinpoint.plugin.arcus.BaseCacheManagerEditorFactory"));
            
            editors.add(new ClassEditorFactoryMapping("net.spy.memcached.internal.GetFuture", "com.nhn.pinpoint.plugin.arcus.FutureEditorFactory"));
            editors.add(new ClassEditorFactoryMapping("net.spy.memcached.internal.ImmediateFuture", "com.nhn.pinpoint.plugin.arcus.FutureEditorFactory"));
            editors.add(new ClassEditorFactoryMapping("net.spy.memcached.internal.OperationFuture", "com.nhn.pinpoint.plugin.arcus.FutureEditorFactory"));
            
            editors.add(new ClassEditorFactoryMapping("net.spy.memcached.plugin.FrontCacheGetFuture", "com.nhn.pinpoint.plugin.arcus.FrontCacheGetFutureEditorFactory"));
            editors.add(new ClassEditorFactoryMapping("net.spy.memcached.plugin.FrontCacheMemcachedClient", "com.nhn.pinpoint.plugin.arcus.FrontCacheMemcachedClientEditorFactory"));
            editors.add(new ClassEditorFactoryMapping("net.spy.memcached.MemcachedClient", "com.nhn.pinpoint.plugin.arcus.MemcachedClientEditorFactory"));
        }

        return editors;
    }
}
