package com.navercorp.pinpoint.common.server.bo.serializer.metadata;

import org.junit.Assert;
import org.junit.Test;


public class MetadataEncoderTest {
    private final MetadataDecoder decoder = new MetadataDecoder();
    private final MetadataEncoder encoder = new MetadataEncoder();

    @Test
    public void encodeRowKey() {
        long startTime = System.currentTimeMillis();
        MetaDataRowKey metaData = new DefaultMetaDataRowKey("agent", startTime, 1);
        byte[] rowKey = encoder.encodeRowKey(metaData);
        MetaDataRowKey decodeRowKey = decoder.decodeRowKey(rowKey);

        Assert.assertEquals("agent", decodeRowKey.getAgentId());
        Assert.assertEquals(startTime, decodeRowKey.getAgentStartTime());
        Assert.assertEquals(1, decodeRowKey.getId());
    }
}