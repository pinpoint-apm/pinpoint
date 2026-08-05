/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.bo.serializer.metadata;

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.hbase.wd.OneByteSimpleHash;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MetadataEncoderTest {
    private static final ServiceUid SERVICE = ServiceUid.of(100_001);
    private final MetadataDecoder decoder = new MetadataDecoder();
    private final MetadataEncoder encoder = new MetadataEncoder(new RowKeyDistributorByHashPrefix(new OneByteSimpleHash(16)));

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

    @Test
    void defaultServiceUidIsByteCompatibleWithLegacyHelper() {
        long startTime = 1_234_567_890L;
        byte[] legacy = MetadataEncoder.readMetaDataRowKey(1, "agent", startTime, 7);
        byte[] explicit = MetadataEncoder.readMetaDataRowKey(
                1, "agent", startTime, 7, ServiceUid.DEFAULT);

        assertThat(explicit).isEqualTo(legacy).hasSize(37);
        assertThat(MetadataEncoder.readMetaDataRowKey(
                0, "agent", startTime, 7, ServiceUid.DEFAULT))
                .isEqualTo(MetadataEncoder.readMetaDataRowKey(
                        0, "agent", startTime, 7))
                .hasSize(36);
    }

    @Test
    void nonDefaultServiceUidIsFourByteSuffix() {
        long startTime = 1_234_567_890L;
        MetadataEncoder configured = new MetadataEncoder(
                new DistributorConfiguration().metadataRowKeyDistributor());
        byte[] defaultRow = configured.encodeRowKey(
                new DefaultMetaDataRowKey(ServiceUid.DEFAULT, "agent", startTime, 7));
        byte[] serviceRow = configured.encodeRowKey(
                new DefaultMetaDataRowKey(SERVICE, "agent", startTime, 7));
        MetaDataRowKey decoded = decoder.decodeRowKey(serviceRow);

        assertThat(serviceRow).hasSize(defaultRow.length + 4);
        assertThat(Arrays.copyOf(serviceRow, defaultRow.length)).isEqualTo(defaultRow);
        assertThat(decoded.getServiceUid()).isEqualTo(SERVICE);
        assertThat(decoded.getId()).isEqualTo(7);
    }

    @Test
    void configuredMetadataHashRangeKeepsSaltStable() {
        MetadataEncoder configured = new MetadataEncoder(
                new DistributorConfiguration().metadataRowKeyDistributor());
        byte[] defaultRow = configured.encodeRowKey(
                new DefaultMetaDataRowKey(ServiceUid.DEFAULT, "agent", 10L, 7));
        byte[] serviceRow = configured.encodeRowKey(
                new DefaultMetaDataRowKey(SERVICE, "agent", 10L, 7));

        assertThat(serviceRow[0]).isEqualTo(defaultRow[0]);
    }

    @Test
    void decoderRejectsUnexpectedLengthAndInvalidSuffix() {
        assertThatThrownBy(() -> decoder.decodeRowKey(new byte[38]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("metadata rowkey length");

        MetadataEncoder configured = new MetadataEncoder(
                new DistributorConfiguration().metadataRowKeyDistributor());
        byte[] invalidOwner = configured.encodeRowKey(
                new DefaultMetaDataRowKey(SERVICE, "agent", 10L, 7));
        ByteArrayUtils.writeInt(ServiceUid.ERROR.getUid(), invalidOwner,
                invalidOwner.length - BytesUtils.INT_BYTE_LENGTH);
        assertThatThrownBy(() -> decoder.decodeRowKey(invalidOwner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("serviceUid");
    }
}