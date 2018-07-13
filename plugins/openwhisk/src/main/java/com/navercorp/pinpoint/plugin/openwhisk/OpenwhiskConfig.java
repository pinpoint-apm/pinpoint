package com.navercorp.pinpoint.plugin.openwhisk;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

/**
 * @author upgle (Seonghyun, Oh)
 */
public class OpenwhiskConfig {

    private boolean enable = false;

    public OpenwhiskConfig(ProfilerConfig config) {
        /*
         * openwhisk
         */
        this.enable = config.readBoolean("profiler.openwhisk.enable", false);
    }

    public boolean isEnable() {
        return enable;
    }
}
