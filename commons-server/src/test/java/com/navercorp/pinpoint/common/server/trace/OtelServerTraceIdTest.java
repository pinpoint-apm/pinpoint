package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class OtelServerTraceIdTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    void getId() {

        UUID uuid = UUID.randomUUID();
        byte[] uuidBytes = new byte[16];
        BytesUtils.writeLong(uuid.getMostSignificantBits(), uuidBytes, 0);
        BytesUtils.writeLong(uuid.getLeastSignificantBits(), uuidBytes, 8);

        OtelServerTraceId otelServerTraceId = new OtelServerTraceId(uuidBytes);

        String id = otelServerTraceId.toString();
        logger.info("{}", id);
    }
}