package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void of_hexString_eitherCase() {
        // OTel trace IDs are case-insensitive hex; a request may carry either spelling.
        String lower = "0102030405060708090a0b0c0d0e0f10";
        OtelServerTraceId fromLower = OtelServerTraceId.of(lower);
        OtelServerTraceId fromUpper = OtelServerTraceId.of(lower.toUpperCase());

        assertThat(fromUpper).isEqualTo(fromLower);
        assertThat(fromLower.toString()).isEqualTo(lower);
        assertThat(fromUpper.toString()).isEqualTo(lower);
    }

    @Test
    void of_invalidHex_rejected() {
        assertThatThrownBy(() -> OtelServerTraceId.of("0102030405060708090a0b0c0d0e0f1z"))
                .isInstanceOf(IllegalArgumentException.class);
        // Valid hex of the wrong byte length is rejected by the constructor, not the decoder.
        assertThatThrownBy(() -> OtelServerTraceId.of("0102"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("length");
    }
}
