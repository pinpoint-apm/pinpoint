package com.nhn.pinpoint.plugin.arcus.editor;

import net.spy.memcached.ops.Operation;

import com.nhn.pinpoint.bootstrap.instrument.InstrumentClass;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorFactory;
import com.nhn.pinpoint.bootstrap.plugin.Condition;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.nhn.pinpoint.bootstrap.plugin.TypeUtils;
import com.nhn.pinpoint.plugin.arcus.Commons;
import com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor;
import com.nhn.pinpoint.plugin.arcus.filter.MemcachedMethodFilter;
import com.nhn.pinpoint.plugin.arcus.interceptor.AddOpInterceptor;
import com.nhn.pinpoint.plugin.arcus.interceptor.ApiInterceptor;

public class MemcachedClientEditorFactory implements ClassEditorFactory {

    @Override
    public ClassEditor get(ProfilerPluginContext context) {
        boolean traceArcusKey = context.getConfig().isArucsKeyTrace();
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        
        builder.editWhen(new Condition() {

            @Override
            public boolean check(InstrumentClass target) {
                return target.hasDeclaredMethod("addOp", TypeUtils.toClassNames(String.class, Operation.class));
            }
            
        });
        
        
        builder.inject(ServiceCodeAccessor.class);
        
        builder.intercept("addOp", String.class, Operation.class).with(AddOpInterceptor.class);
        
        builder.interceptMethodsFilteredBy(new MemcachedMethodFilter())
            .with(ApiInterceptor.class)
            .in(Commons.ARCUS_SCOPE)
            .using(traceArcusKey ? Commons.ARCUS_KEY_EXTRACTOR_FACTORY : null);
                        
        return builder.build();
    }

}
