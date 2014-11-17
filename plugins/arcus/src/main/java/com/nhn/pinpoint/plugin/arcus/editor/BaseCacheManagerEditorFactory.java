package com.nhn.pinpoint.plugin.arcus.editor;

import java.util.concurrent.CountDownLatch;

import net.spy.memcached.ConnectionFactoryBuilder;

import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorFactory;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor;
import com.nhn.pinpoint.plugin.arcus.interceptor.CacheManagerConstructInterceptor;

public class BaseCacheManagerEditorFactory implements ClassEditorFactory {

    @Override
    public ClassEditor get(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();

        builder.inject(ServiceCodeAccessor.class);
        builder.interceptConstructor(String.class, String.class, ConnectionFactoryBuilder.class, CountDownLatch.class, int.class, int.class)
            .with(CacheManagerConstructInterceptor.class);
        
        return builder.build();

    }

}
