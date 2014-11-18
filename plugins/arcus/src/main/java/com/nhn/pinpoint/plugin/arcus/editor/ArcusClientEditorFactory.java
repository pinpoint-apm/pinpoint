package com.nhn.pinpoint.plugin.arcus.editor;

import net.spy.memcached.CacheManager;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorFactory;
import com.nhn.pinpoint.bootstrap.plugin.Condition;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.nhn.pinpoint.plugin.arcus.Commons;
import com.nhn.pinpoint.plugin.arcus.filter.ArcusMethodFilter;
import com.nhn.pinpoint.plugin.arcus.interceptor.ApiInterceptor;
import com.nhn.pinpoint.plugin.arcus.interceptor.SetCacheManagerInterceptor;

public class ArcusClientEditorFactory implements ClassEditorFactory {

    @Override
    public ClassEditor get(ProfilerPluginContext context) {
        boolean traceArcusKey = context.getConfig().isArucsKeyTrace();
        ClassEditorBuilder builder = context.newClassEditorBuilder();

        builder.editWhen(new Condition() {
            
            @Override
            public boolean check(InstrumentClass target) {
                return target.hasMethod("addOp", new String[] {"java.lang.String", "net.spy.memcached.ops.Operation"}, "net.spy.memcached.ops.Operation");
            }
            
        });
        
        builder.intercept("setCacheManager", CacheManager.class).with(SetCacheManagerInterceptor.class);
        
        builder.interceptMethodsFilteredBy(new ArcusMethodFilter())
                .with(ApiInterceptor.class)
                .in(Commons.ARCUS_SCOPE)
                .using(traceArcusKey ? Commons.ARCUS_KEY_EXTRACTOR_FACTORY : null);
        
        return builder.build();
    }
}
