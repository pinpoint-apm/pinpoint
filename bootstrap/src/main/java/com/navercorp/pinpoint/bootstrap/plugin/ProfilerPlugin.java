package com.navercorp.pinpoint.bootstrap.plugin;

import java.util.List;

public interface ProfilerPlugin {
    List<ClassEditor> getClassEditors(ProfilerPluginContext context);
}
