package com.nhn.pinpoint.bootstrap.plugin;

import java.util.List;

public interface ProfilerPlugin {
    public List<ClassEditor> getClassEditors(ProfilerPluginContext context);
}
