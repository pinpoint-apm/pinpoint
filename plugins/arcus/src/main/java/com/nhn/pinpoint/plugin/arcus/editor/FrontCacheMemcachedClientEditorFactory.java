package com.nhn.pinpoint.plugin.arcus.editor;

import java.util.concurrent.Future;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorFactory;
import com.nhn.pinpoint.bootstrap.plugin.Condition;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.nhn.pinpoint.bootstrap.plugin.TypeUtils;
import com.nhn.pinpoint.plugin.arcus.Commons;
import com.nhn.pinpoint.plugin.arcus.filter.FrontCacheMemcachedMethodFilter;
import com.nhn.pinpoint.plugin.arcus.interceptor.ApiInterceptor;

public class FrontCacheMemcachedClientEditorFactory implements ClassEditorFactory {

    @Override
    public ClassEditor get(ProfilerPluginContext context) {
        boolean traceArcusKey = context.getConfig().isArucsKeyTrace();
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        
        builder.editWhen(new Condition() {

            @Override
            public boolean check(InstrumentClass target) {
                return target.hasDeclaredMethod("putFrontCache", TypeUtils.toClassNames(String.class, Future.class, long.class));
            }
            
        });
        
        builder.interceptMethodsFilteredBy(new FrontCacheMemcachedMethodFilter())
            .with(ApiInterceptor.class)
            .in(Commons.ARCUS_SCOPE)
            .using(traceArcusKey ? Commons.ARCUS_KEY_EXTRACTOR_FACTORY : null);
                        
        return builder.build();
    }

}
