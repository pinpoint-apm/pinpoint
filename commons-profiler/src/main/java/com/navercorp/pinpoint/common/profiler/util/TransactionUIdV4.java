package com.navercorp.pinpoint.common.profiler.util;

import com.navercorp.pinpoint.common.profiler.name.Base64Utils;

import java.util.Objects;
import java.util.UUID;

public class TransactionUIdV4 implements TransactionUId {
    // UUID v7
    private final UUID uuid;
    private String uuidCache;
    private String base64Cache;

    public TransactionUIdV4(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUuidString() {
        if (uuidCache == null) {
            uuidCache = uuid.toString();
        }
        return uuidCache;
    }

    public String getBase64String() {
        if (base64Cache == null) {
            base64Cache = Base64Utils.encode(uuid);
        }
        return base64Cache;
    }

    @Override
    public String toString() {
        return getUuidString();
    }
}
