package com.navercorp.pinpoint.plugin.loggingevent;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class LoggingEventConfig {
    private final boolean loggingEventEnable;
    public LoggingEventConfig(ProfilerConfig config){
        this.loggingEventEnable = config.readBoolean("profiler.loggingevent.enable", true);
    }
    public boolean isLoggingEventEnable(){
        return this.loggingEventEnable;
    }
}
