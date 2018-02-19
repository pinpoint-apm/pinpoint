package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;


public class OpenwhiskConfig {

    /**
     * openwhisk
     */
    private boolean enable = true;

    private String caller;

    public OpenwhiskConfig(ProfilerConfig config) {
        /*
         * openwhisk
         */
        this.enable = config.readBoolean("profiler.openwhisk.enable", false);
        this.caller = config.readString("profiler.openwhisk.caller", "CALLER");
    }

    public boolean isEnable() {
        return enable;
    }

    public String getCaller() {
        return caller;
    }
}
