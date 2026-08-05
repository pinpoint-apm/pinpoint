package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.SqlMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.SqlUidMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.StringMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.uid.UidMetaDataRowKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.SqlMetaDataDao;
import com.navercorp.pinpoint.web.dao.SqlUidMetaDataDao;
import com.navercorp.pinpoint.web.dao.StringMetaDataDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HbaseMetadataDaoServiceUidTest {
    private static final ServiceUid SERVICE_A = ServiceUid.of(100_001);
    private static final ServiceUid SERVICE_B = ServiceUid.of(100_002);

    @Test
    void sqlSingleAndBatchPreserveServiceUid() {
        HbaseOperations operations = mock(HbaseOperations.class);
        TableNameProvider names = mock(TableNameProvider.class);
        RowKeyEncoder<MetaDataRowKey> encoder = mock(RowKeyEncoder.class);
        RowMapper<List<SqlMetaDataBo>> mapper = mock(RowMapper.class);
        HbaseSqlMetaDataDao dao = new HbaseSqlMetaDataDao(
                operations, encoder, names, mapper);
        TableName table = TableName.valueOf("SqlMetaData_Ver2");
        when(names.getTableName(any(HbaseTable.class))).thenReturn(table);
        when(encoder.encodeRowKey(any())).thenReturn(new byte[]{1});
        when(operations.get(eq(table), any(Get.class), eq(mapper))).thenReturn(List.of());

        dao.getSqlMetaData(SERVICE_A, "agent", 10L, 7);

        ArgumentCaptor<MetaDataRowKey> single = ArgumentCaptor.forClass(MetaDataRowKey.class);
        verify(encoder).encodeRowKey(single.capture());
        assertThat(single.getValue().getServiceUid()).isEqualTo(SERVICE_A);
        verify(operations).get(eq(table), any(Get.class), eq(mapper));

        clearInvocations(encoder, operations);
        List<List<SqlMetaDataBo>> canned = List.of(List.of(), List.of());
        when(operations.get(eq(table), any(List.class), eq(mapper))).thenReturn(canned);
        List<List<SqlMetaDataBo>> result = dao.getSqlMetaData(List.of(
                new SqlMetaDataDao.SqlMetaDataKey(SERVICE_A, "agent", 10L, 7),
                new SqlMetaDataDao.SqlMetaDataKey(SERVICE_B, "agent", 10L, 7)));

        ArgumentCaptor<MetaDataRowKey> batch = ArgumentCaptor.forClass(MetaDataRowKey.class);
        verify(encoder, times(2)).encodeRowKey(batch.capture());
        assertThat(batch.getAllValues()).extracting(MetaDataRowKey::getServiceUid)
                .containsExactly(SERVICE_A, SERVICE_B);
        ArgumentCaptor<List<Get>> gets = ArgumentCaptor.forClass(List.class);
        verify(operations).get(eq(table), gets.capture(), eq(mapper));
        assertThat(gets.getValue()).hasSize(2);
        assertThat(result).isSameAs(canned).hasSize(2);
    }

    @Test
    void sqlUidSingleAndBatchPreserveServiceUid() {
        HbaseOperations operations = mock(HbaseOperations.class);
        TableNameProvider names = mock(TableNameProvider.class);
        RowKeyEncoder<UidMetaDataRowKey> encoder = mock(RowKeyEncoder.class);
        RowMapper<List<SqlUidMetaDataBo>> mapper = mock(RowMapper.class);
        HbaseSqlUidMetaDataDao dao = new HbaseSqlUidMetaDataDao(
                operations, encoder, names, mapper);
        TableName table = TableName.valueOf("SqlUidMetaData");
        when(names.getTableName(any(HbaseTable.class))).thenReturn(table);
        when(encoder.encodeRowKey(any())).thenReturn(new byte[]{1});
        when(operations.get(eq(table), any(Get.class), eq(mapper))).thenReturn(List.of());

        dao.getSqlUidMetaData(SERVICE_A, "agent", 10L, new byte[16]);

        ArgumentCaptor<UidMetaDataRowKey> single = ArgumentCaptor.forClass(UidMetaDataRowKey.class);
        verify(encoder).encodeRowKey(single.capture());
        assertThat(single.getValue().getServiceUid()).isEqualTo(SERVICE_A);
        verify(operations).get(eq(table), any(Get.class), eq(mapper));

        clearInvocations(encoder, operations);
        List<List<SqlUidMetaDataBo>> canned = List.of(List.of(), List.of());
        when(operations.get(eq(table), any(List.class), eq(mapper))).thenReturn(canned);
        List<List<SqlUidMetaDataBo>> result = dao.getSqlUidMetaData(List.of(
                new SqlUidMetaDataDao.SqlUidMetaDataKey(
                        SERVICE_A, "agent", 10L, new byte[16]),
                new SqlUidMetaDataDao.SqlUidMetaDataKey(
                        SERVICE_B, "agent", 10L, new byte[16])));

        ArgumentCaptor<UidMetaDataRowKey> batch = ArgumentCaptor.forClass(UidMetaDataRowKey.class);
        verify(encoder, times(2)).encodeRowKey(batch.capture());
        assertThat(batch.getAllValues()).extracting(UidMetaDataRowKey::getServiceUid)
                .containsExactly(SERVICE_A, SERVICE_B);
        ArgumentCaptor<List<Get>> gets = ArgumentCaptor.forClass(List.class);
        verify(operations).get(eq(table), gets.capture(), eq(mapper));
        assertThat(gets.getValue()).hasSize(2);
        assertThat(result).isSameAs(canned).hasSize(2);
    }

    @Test
    void stringSingleAndBatchPreserveServiceUid() {
        HbaseOperations operations = mock(HbaseOperations.class);
        TableNameProvider names = mock(TableNameProvider.class);
        RowKeyEncoder<MetaDataRowKey> encoder = mock(RowKeyEncoder.class);
        RowMapper<List<StringMetaDataBo>> mapper = mock(RowMapper.class);
        HbaseStringMetaDataDao dao = new HbaseStringMetaDataDao(
                operations, encoder, names, mapper);
        TableName table = TableName.valueOf("StringMetaData");
        when(names.getTableName(any(HbaseTable.class))).thenReturn(table);
        when(encoder.encodeRowKey(any())).thenReturn(new byte[]{1});
        when(operations.get(eq(table), any(Get.class), eq(mapper))).thenReturn(List.of());

        dao.getStringMetaData(SERVICE_A, "agent", 10L, 7);

        ArgumentCaptor<MetaDataRowKey> single = ArgumentCaptor.forClass(MetaDataRowKey.class);
        verify(encoder).encodeRowKey(single.capture());
        assertThat(single.getValue().getServiceUid()).isEqualTo(SERVICE_A);
        verify(operations).get(eq(table), any(Get.class), eq(mapper));

        clearInvocations(encoder, operations);
        List<List<StringMetaDataBo>> canned = List.of(List.of(), List.of());
        when(operations.get(eq(table), any(List.class), eq(mapper))).thenReturn(canned);
        List<List<StringMetaDataBo>> result = dao.getStringMetaData(List.of(
                new StringMetaDataDao.StringMetaDataKey(SERVICE_A, "agent", 10L, 7),
                new StringMetaDataDao.StringMetaDataKey(SERVICE_B, "agent", 10L, 7)));

        ArgumentCaptor<MetaDataRowKey> batch = ArgumentCaptor.forClass(MetaDataRowKey.class);
        verify(encoder, times(2)).encodeRowKey(batch.capture());
        assertThat(batch.getAllValues()).extracting(MetaDataRowKey::getServiceUid)
                .containsExactly(SERVICE_A, SERVICE_B);
        ArgumentCaptor<List<Get>> gets = ArgumentCaptor.forClass(List.class);
        verify(operations).get(eq(table), gets.capture(), eq(mapper));
        assertThat(gets.getValue()).hasSize(2);
        assertThat(result).isSameAs(canned).hasSize(2);
    }
}
