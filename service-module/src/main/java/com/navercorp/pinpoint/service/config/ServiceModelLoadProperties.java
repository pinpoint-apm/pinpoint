package com.navercorp.pinpoint.service.config;

import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;

public class ServiceModelLoadProperties {

    @Value("${web.service.registry.cache.load.warmup.enabled:true}")
    private boolean warmupEnabled = true;
    @Value("${web.service.registry.cache.load.refresh.enabled:true}")
    private boolean refreshEnabled = true;
    @Value("${web.service.registry.cache.load.refresh.interval:5m}")
    private Duration refreshInterval = Duration.ofMinutes(5);
    @Value("${web.service.registry.cache.load.limit:-1}")
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

    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
}
