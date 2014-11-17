package com.nhn.pinpoint.plugin.arcus.editor;

import com.nhn.pinpoint.bootstrap.plugin.ClassEditor;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorBuilder;
import com.nhn.pinpoint.bootstrap.plugin.ClassEditorFactory;
import com.nhn.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.nhn.pinpoint.plugin.arcus.accessor.ServiceCodeAccessor;

public class BaseOperationImplEditorFactory implements ClassEditorFactory {

    @Override
    public ClassEditor get(ProfilerPluginContext context) {
        ClassEditorBuilder builder = context.newClassEditorBuilder();
        builder.inject(ServiceCodeAccessor.class);

        return builder.build();
    }

}
