package com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid;

import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UidMetadataEncoderTest {
    private static final ServiceUid SERVICE = ServiceUid.of(100_001);
    private final UidMetadataEncoder encoder = new UidMetadataEncoder(
            new DistributorConfiguration().metadataRowKeyDistributor2());
    private final UidMetadataDecoder decoder = new UidMetadataDecoder();

    @Test
    void defaultServiceUidIsByteCompatibleWithLegacyHelper() {
        byte[] uid = new byte[UidMetaDataRowKey.UID_LENGTH];
        byte[] legacy = UidMetadataEncoder.encodeMetaDataRowKey(1, "agent", 10L, uid);
        byte[] explicit = UidMetadataEncoder.encodeMetaDataRowKey(
                1, "agent", 10L, uid, ServiceUid.DEFAULT);

        assertThat(explicit).isEqualTo(legacy).hasSize(49);
        assertThat(UidMetadataEncoder.encodeMetaDataRowKey(
                0, "agent", 10L, uid, ServiceUid.DEFAULT))
                .isEqualTo(UidMetadataEncoder.encodeMetaDataRowKey(
                        0, "agent", 10L, uid))
                .hasSize(48);
    }

    @Test
    void nonDefaultServiceUidFollowsTheFixedUid() {
        byte[] uid = new byte[UidMetaDataRowKey.UID_LENGTH];
        Arrays.fill(uid, (byte) 3);
        byte[] defaultRow = encoder.encodeRowKey(
                new DefaultUidMetaDataRowKey(ServiceUid.DEFAULT, "agent", 10L, uid));
        byte[] serviceRow = encoder.encodeRowKey(
                new DefaultUidMetaDataRowKey(SERVICE, "agent", 10L, uid));
        UidMetaDataRowKey decoded = decoder.decodeRowKey(serviceRow);

        assertThat(serviceRow).hasSize(defaultRow.length + 4);
        assertThat(decoded.getUid()).isEqualTo(uid);
        assertThat(decoded.getServiceUid()).isEqualTo(SERVICE);
        assertThat(serviceRow[0]).isEqualTo(defaultRow[0]);
    }

    @Test
    void encoderRejectsEveryNonMurmur128Length() {
        assertThatThrownBy(() -> UidMetadataEncoder.encodeMetaDataRowKey(
                1, "agent", 10L, new byte[15]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uid length");
        assertThatThrownBy(() -> UidMetadataEncoder.encodeMetaDataRowKey(
                1, "agent", 10L, new byte[17]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uid length");
    }

    @Test
    void decoderRejectsUnexpectedLengthAndInvalidSuffix() {
        assertThatThrownBy(() -> decoder.decodeRowKey(new byte[50]))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uid metadata rowkey length");

        byte[] invalidOwner = encoder.encodeRowKey(new DefaultUidMetaDataRowKey(
                SERVICE, "agent", 10L, new byte[UidMetaDataRowKey.UID_LENGTH]));
        ByteArrayUtils.writeInt(ServiceUid.ERROR.getUid(), invalidOwner,
                invalidOwner.length - BytesUtils.INT_BYTE_LENGTH);
        assertThatThrownBy(() -> decoder.decodeRowKey(invalidOwner))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("serviceUid");
    }
}
