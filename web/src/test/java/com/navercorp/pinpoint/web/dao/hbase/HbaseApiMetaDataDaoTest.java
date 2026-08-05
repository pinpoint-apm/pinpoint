package com.navercorp.pinpoint.web.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseOperations;
import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.ApiMetaDataBo;
import com.navercorp.pinpoint.common.server.bo.MethodTypeEnum;
import com.navercorp.pinpoint.common.server.bo.serializer.RowKeyEncoder;
import com.navercorp.pinpoint.common.server.bo.serializer.metadata.MetaDataRowKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.web.dao.ApiMetaDataDao;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HbaseApiMetaDataDaoTest {

    @Mock
    private HbaseOperations hbaseOperations;
    @Mock
    private TableNameProvider tableNameProvider;
    @Mock
    private RowKeyEncoder<MetaDataRowKey> rowKeyEncoder;
    @Mock
    private RowMapper<List<ApiMetaDataBo>> apiMetaDataMapper;

    private HbaseApiMetaDataDao newDao() {
        return new HbaseApiMetaDataDao(hbaseOperations, rowKeyEncoder, tableNameProvider, apiMetaDataMapper);
    }

    @Test
    public void getApiMetaDataCachable() {
        // cacheable key - spring expression language
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("serviceUid", ServiceUid.of(100_001));
        context.setVariable("agentId", "foo");
        context.setVariable("time", (long) 1);
        context.setVariable("apiId", (int) 2);

        String key = (String) parser.parseExpression(HbaseApiMetaDataDao.SPEL_KEY).getValue(context);
        assertEquals("100001.foo.1.2", key);
    }

    @Test
    public void getApiMetaData_batch_emptyKeys_shortCircuits() {
        HbaseApiMetaDataDao dao = newDao();

        List<List<ApiMetaDataBo>> result = dao.getApiMetaData(List.of());

        assertTrue(result.isEmpty());
        verifyNoInteractions(hbaseOperations);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void getApiMetaData_batch_delegatesSingleMultiGet() {
        HbaseApiMetaDataDao dao = newDao();

        lenient().when(rowKeyEncoder.encodeRowKey(any())).thenReturn(new byte[]{1});
        TableName tableName = TableName.valueOf("ApiMetaData");
        when(tableNameProvider.getTableName(any(HbaseTable.class))).thenReturn(tableName);

        ServiceUid serviceA = ServiceUid.of(100_001);
        ServiceUid serviceB = ServiceUid.of(100_002);
        List<ApiMetaDataDao.ApiMetaDataKey> keys = List.of(
                new ApiMetaDataDao.ApiMetaDataKey(serviceA, "agent-a", 100L, 1),
                new ApiMetaDataDao.ApiMetaDataKey(serviceB, "agent-a", 100L, 2));

        ApiMetaDataBo bo = new ApiMetaDataBo(serviceB, "agent-a", 100L, 2, 0, MethodTypeEnum.DEFAULT, "api");
        List<List<ApiMetaDataBo>> canned = List.of(List.of(), List.of(bo));
        when(hbaseOperations.get(eq(tableName), any(List.class), eq(apiMetaDataMapper))).thenReturn(canned);

        List<List<ApiMetaDataBo>> result = dao.getApiMetaData(keys);

        // one multiGet round-trip covering all keys (no per-key N+1)
        ArgumentCaptor<List<Get>> getsCaptor = ArgumentCaptor.forClass(List.class);
        verify(hbaseOperations).get(eq(tableName), getsCaptor.capture(), eq(apiMetaDataMapper));
        assertEquals(2, getsCaptor.getValue().size());

        ArgumentCaptor<MetaDataRowKey> rowKeys = ArgumentCaptor.forClass(MetaDataRowKey.class);
        verify(rowKeyEncoder, org.mockito.Mockito.times(2)).encodeRowKey(rowKeys.capture());
        assertEquals(List.of(serviceA, serviceB), rowKeys.getAllValues().stream()
                .map(MetaDataRowKey::getServiceUid).toList());

        // result is returned index-aligned with the input keys
        assertSame(canned, result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).isEmpty());
        assertEquals("api", result.get(1).get(0).getApiInfo());
    }

    @Test
    public void getApiMetaData_singleUsesExactServiceKey() {
        HbaseApiMetaDataDao dao = newDao();
        ServiceUid serviceUid = ServiceUid.of(100_001);
        when(rowKeyEncoder.encodeRowKey(any())).thenReturn(new byte[]{1});
        TableName table = TableName.valueOf("ApiMetaData");
        when(tableNameProvider.getTableName(any(HbaseTable.class))).thenReturn(table);
        when(hbaseOperations.get(eq(table), any(Get.class), eq(apiMetaDataMapper)))
                .thenReturn(List.of());

        dao.getApiMetaData(serviceUid, "agent", 10L, 7);

        ArgumentCaptor<MetaDataRowKey> key = ArgumentCaptor.forClass(MetaDataRowKey.class);
        verify(rowKeyEncoder).encodeRowKey(key.capture());
        assertEquals(serviceUid, key.getValue().getServiceUid());
        verify(hbaseOperations).get(eq(table), any(Get.class), eq(apiMetaDataMapper));
    }
}
