package com.nhn.pinpoint.plugin.arcus.editor;

import java.util.concurrent.TimeUnit;

import net.sf.ehcache.Element;

import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorFactory;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.nhn.pinpoint.plugin.arcus.Commons;
import com.nhn.pinpoint.plugin.arcus.accessor.CacheKeyAccessor;
import com.nhn.pinpoint.plugin.arcus.accessor.CacheNameAccessor;
import com.nhn.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureConstructInterceptor;
import com.nhn.pinpoint.plugin.arcus.interceptor.FrontCacheGetFutureGetInterceptor;

public class FrontCacheGetFutureEditorFactory implements ClassEditorFactory {

    @Override
    public ClassEditor get(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();

        builder.inject(CacheNameAccessor.class);
        builder.inject(CacheKeyAccessor.class);
        
        builder.interceptConstructor(Element.class).with(FrontCacheGetFutureConstructInterceptor.class);
        builder.intercept("get", long.class, TimeUnit.class).with(FrontCacheGetFutureGetInterceptor.class).in(Commons.ARCUS_SCOPE);
        builder.intercept("get").with(FrontCacheGetFutureGetInterceptor.class).in(Commons.ARCUS_SCOPE);
        
        return builder.build();
    }

}
