package com.navercorp.pinpoint.plugin.redis;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;

public class RedisPluginConfig {

    private boolean enabled = true;
    private boolean pipelineEnabled = true;

    public RedisPluginConfig(ProfilerConfig src) {
        enabled = src.readBoolean("profiler.redis", true);
        pipelineEnabled = src.readBoolean("profiler.redis.pipeline", true);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isPipelineEnabled() {
        return pipelineEnabled;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{enabled=");
        builder.append(enabled);
        builder.append(", pipelineEnabled=");
        builder.append(pipelineEnabled);
        builder.append("}");
        return builder.toString();
    }
}
