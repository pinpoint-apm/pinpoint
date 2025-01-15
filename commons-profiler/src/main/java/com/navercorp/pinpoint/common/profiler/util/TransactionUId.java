package com.navercorp.pinpoint.common.profiler.util;

import java.util.UUID;

/**
 * pinpoint V4 spec
 */
public interface TransactionUId {

    UUID getUuid();

    String getUuidString();

    String getBase64String();

    static TransactionUId of(UUID uuid) {
        return new TransactionUIdV4(uuid);
    }
}
