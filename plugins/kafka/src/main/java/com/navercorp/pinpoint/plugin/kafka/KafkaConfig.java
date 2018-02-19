package com.navercorp.pinpoint.plugin.kafka;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class KafkaConfig {
    private boolean enable = true;
    private String caller;

    public KafkaConfig(ProfilerConfig config) {
        /*
         * kafka
         */
        this.enable = config.readBoolean("profiler.kafka.enable", false);
        this.caller = config.readString("profiler.kafka.caller", "CALLER");
    }

    public boolean isEnable() {
        return enable;
    }

    public String getCaller() {
        return caller;
    }
}
