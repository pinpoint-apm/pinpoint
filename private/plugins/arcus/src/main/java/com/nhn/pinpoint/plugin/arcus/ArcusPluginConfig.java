package com.nhn.pinpoint.plugin.arcus;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;

public class ArcusPluginConfig {
    private final boolean arcus;
    private final boolean arcusKeyTrace;
    private final boolean memcached;
    private final boolean memcachedKeyTrace;

    public ArcusPluginConfig(ProfilerConfig src) {
        this.arcus = src.readBoolean("profiler.arcus", true);
        this.arcusKeyTrace = src.readBoolean("profiler.arcus.keytrace", false);
        this.memcached = src.readBoolean("profiler.memcached", true);
        this.memcachedKeyTrace = src.readBoolean("profiler.memcached.keytrace", false);
    }
    
    public boolean isArcus() {
        return arcus;
    }

    public boolean isArcusKeyTrace() {
        return arcusKeyTrace;
    }

    public boolean isMemcached() {
        return memcached;
    }

    public boolean isMemcachedKeyTrace() {
        return memcachedKeyTrace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("ArcusPluginConfig={arcus=").append(arcus);
        sb.append(", arcusKeyTrace=").append(arcusKeyTrace);
        sb.append(", memcached=").append(memcached);
        sb.append(", memcachedKeyTrace=").append(memcachedKeyTrace);
        sb.append("}");
        
        return sb.toString();
    }
    
    
}
