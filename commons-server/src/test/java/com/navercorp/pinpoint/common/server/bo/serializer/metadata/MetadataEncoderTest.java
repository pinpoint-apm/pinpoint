package com.navercorp.pinpoint.common.server.bo.serializer.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetadataEncoderTest {
    private final MetadataDecoder decoder = new MetadataDecoder();
    private final MetadataEncoder encoder = new MetadataEncoder();

    @Test
    public void encodeRowKey() {
        long startTime = System.currentTimeMillis();
        MetaDataRowKey metaData = new DefaultMetaDataRowKey("agent", startTime, 1);
        byte[] rowKey = encoder.encodeRowKey(metaData);
        MetaDataRowKey decodeRowKey = decoder.decodeRowKey(rowKey);

        Assertions.assertEquals("agent", decodeRowKey.getAgentId());
        Assertions.assertEquals(startTime, decodeRowKey.getAgentStartTime());
        Assertions.assertEquals(1, decodeRowKey.getId());
    }
}