package com.navercorp.pinpoint.bootstrap.plugin;

public interface ClassEditorFactory {
    ClassEditor get(ProfilerPluginContext context);
}
