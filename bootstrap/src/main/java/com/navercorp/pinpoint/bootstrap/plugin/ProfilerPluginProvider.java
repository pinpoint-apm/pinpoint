package com.nhn.pinpoint.bootstrap.plugin;

import com.nhn.pinpoint.bootstrap.context.TraceContext;

public interface ProfilerPluginProvider {
    public ProfilerPlugin getPlugin(TraceContext context);
}
