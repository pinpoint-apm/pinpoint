package com.navercorp.pinpoint.common.server.uid.cache;

import com.github.benmanes.caffeine.cache.Caffeine;


public class UidCaffeineCacheBuilder {

    public Caffeine<Object, Object> build(UidCaffeineCacheProperties properties) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        if (properties.getInitialCapacity() != -1) {
            builder.initialCapacity(properties.getInitialCapacity());
        }
        if (properties.getMaximumSize() != -1L) {
            builder.maximumSize(properties.getMaximumSize());
        }
        if (properties.getMaximumWeight() != -1L) {
            builder.maximumWeight(properties.getMaximumWeight());
        }

        if (properties.getExpireAfterWriteSeconds() != -1L) {
            builder.expireAfterWrite(properties.getExpireAfterWriteSeconds(), java.util.concurrent.TimeUnit.SECONDS);
        }
        if (properties.getExpireAfterAccessSeconds() != -1L) {
            builder.expireAfterAccess(properties.getExpireAfterAccessSeconds(), java.util.concurrent.TimeUnit.SECONDS);
        }
        if (properties.getRefreshAfterWriteSeconds() != -1L) {
            builder.refreshAfterWrite(properties.getRefreshAfterWriteSeconds(), java.util.concurrent.TimeUnit.SECONDS);
        }

        if (properties.isRecordStats()) {
            builder.recordStats();
        }

        return builder;
    }
}
