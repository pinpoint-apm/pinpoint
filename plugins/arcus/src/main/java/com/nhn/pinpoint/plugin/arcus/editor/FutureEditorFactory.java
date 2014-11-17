package com.nhn.pinpoint.plugin.arcus.editor;

import java.util.concurrent.TimeUnit;

import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorFactory;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.nhn.pinpoint.plugin.arcus.Commons;
import com.nhn.pinpoint.plugin.arcus.accessor.OperationAccessor;
import com.nhn.pinpoint.plugin.arcus.interceptor.FutureGetInterceptor;
import com.nhn.pinpoint.plugin.arcus.interceptor.FutureSetOperationInterceptor;

public class FutureEditorFactory implements ClassEditorFactory {

    @Override
    public ClassEditor get(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();

        builder.inject(OperationAccessor.class);
        
        builder.intercept("setOperation", net.spy.memcached.ops.Operation.class).with(FutureSetOperationInterceptor.class);
        
        builder.intercept("get", long.class, TimeUnit.class).with(FutureGetInterceptor.class).in(Commons.ARCUS_SCOPE);
        
        return builder.build();
    }

}
