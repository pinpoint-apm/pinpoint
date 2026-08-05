package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.hbase.config.DistributorConfiguration;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.DefaultMetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetadataDecoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetadataEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.DefaultUidMetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetadataEncoder;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellBuilderFactory;
import org.apache.hadoop.hbase.CellBuilderType;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetadataMapperServiceUidTest {
    @Test
    void sqlMapperRetainsDecodedServiceUid() throws Exception {
        ServiceUid uid = ServiceUid.of(100_001);
        MetadataEncoder encoder = new MetadataEncoder(
                new DistributorConfiguration().metadataRowKeyDistributor2());
        byte[] row = encoder.encodeRowKey(
                new DefaultMetaDataRowKey(uid, "agent", 10L, 7));
        Result result = result(row,
                HbaseTables.SQL_METADATA_VER2_SQL.QUALIFIER_SQLSTATEMENT,
                Bytes.toBytes("select 1"));

        SqlMetaDataBo mapped = new SqlMetaDataMapper(new MetadataDecoder())
                .mapRow(result, 0).get(0);
        assertThat(mapped.getServiceUid()).isEqualTo(uid);
    }

    @Test
    void stringMapperRetainsDecodedServiceUid() throws Exception {
        ServiceUid uid = ServiceUid.of(100_001);
        MetadataEncoder encoder = new MetadataEncoder(
                new DistributorConfiguration().metadataRowKeyDistributor());
        byte[] row = encoder.encodeRowKey(
                new DefaultMetaDataRowKey(uid, "agent", 10L, 7));
        Result result = result(row,
                HbaseTables.STRING_METADATA_STR.QUALIFIER_STRING,
                Bytes.toBytes("value"));

        StringMetaDataBo mapped = new StringMetaDataMapper(new MetadataDecoder())
                .mapRow(result, 0).get(0);
        assertThat(mapped.getServiceUid()).isEqualTo(uid);
    }

    @Test
    void sqlUidMapperRetainsDecodedServiceUid() throws Exception {
        ServiceUid uid = ServiceUid.of(100_001);
        UidMetadataEncoder encoder = new UidMetadataEncoder(
                new DistributorConfiguration().metadataRowKeyDistributor2());
        byte[] row = encoder.encodeRowKey(
                new DefaultUidMetaDataRowKey(uid, "agent", 10L, new byte[16]));
        Result result = result(row,
                HbaseTables.SQL_UID_METADATA_SQL.QUALIFIER_SQLSTATEMENT,
                Bytes.toBytes("select 1"));

        SqlUidMetaDataBo mapped = new SqlUidMetaDataMapper()
                .mapRow(result, 0).get(0);
        assertThat(mapped.getServiceUid()).isEqualTo(uid);
    }

    private static Result result(byte[] row, byte[] qualifier, byte[] value) {
        Cell cell = CellBuilderFactory.create(CellBuilderType.SHALLOW_COPY)
                .setRow(row)
                .setFamily(HConstants.EMPTY_BYTE_ARRAY)
                .setQualifier(qualifier)
                .setTimestamp(HConstants.LATEST_TIMESTAMP)
                .setType(Cell.Type.Put)
                .setValue(value)
                .build();
        Result result = mock(Result.class);
        when(result.isEmpty()).thenReturn(false);
        when(result.getRow()).thenReturn(row);
        when(result.rawCells()).thenReturn(new Cell[]{cell});
        when(result.size()).thenReturn(1);
        return result;
    }
}
