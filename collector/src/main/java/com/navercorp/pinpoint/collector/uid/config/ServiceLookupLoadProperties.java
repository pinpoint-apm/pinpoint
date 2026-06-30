package com.navercorp.pinpoint.collector.uid.config;

import org.springframework.beans.factory.annotation.Value;

public class ServiceLookupLoadProperties {

    @Value("${collector.service.lookup.cache.load.warmup.enabled:true}")
    private boolean warmupEnabled = true;
    @Value("${collector.service.lookup.cache.load.refresh.enabled:false}")
    private boolean refreshEnabled;
    @Value("${collector.service.lookup.cache.load.refresh.interval:60000}")
    private long refreshInterval = 60000;
    @Value("${collector.service.lookup.cache.load.limit:-1}")
    private long limit = -1;

    public boolean isWarmupEnabled() {
        return warmupEnabled;
    }

    public void setWarmupEnabled(boolean warmupEnabled) {
        this.warmupEnabled = warmupEnabled;
    }

    public boolean isRefreshEnabled() {
        return refreshEnabled;
    }

    public void setRefreshEnabled(boolean refreshEnabled) {
        this.refreshEnabled = refreshEnabled;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
