package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TraceIndexFilterBuilderTest {

    private final String testApplicationName = "testApp";
    private final String testAgentId = "testAgent";

    @Test
    public void successRowFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder();
        builder.setSuccess(true);
        Filter filter = builder.build(true, false);

        byte[] rowKey1 = createTestRowKey(testApplicationName, 100, false, testAgentId);
        byte[] rowKey2 = createTestRowKey(testApplicationName, 100, true, testAgentId);
        byte[] value = createTestValue();

        Assertions.assertThat(isFilterOutRow(filter, createIndexKeyValue(rowKey1, value))).isEqualTo(false);
        Assertions.assertThat(isFilterOutRow(filter, createIndexKeyValue(rowKey2, value))).isEqualTo(true);
    }

    @Test
    public void agentIdHashRowFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder();
        builder.setAgentId(testAgentId);
        Filter filter = builder.build(true, false);

        byte[] rowKey1 = createTestRowKey(testApplicationName, 100, false, testAgentId);
        byte[] rowKey2 = createTestRowKey(testApplicationName, 100, false, "otherAgentId");
        byte[] value = createTestValue();

        Assertions.assertThat(isFilterOutRow(filter, createIndexKeyValue(rowKey1, value))).isEqualTo(false);
        Assertions.assertThat(isFilterOutRow(filter, createIndexKeyValue(rowKey2, value))).isEqualTo(true);
    }

    @Test
    public void elapsedByteRowFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder();
        builder.setElapsedMin(250L);
        builder.setElapsedMax(350L);
        Filter filter = builder.build(true, false);

        // elapsed time with different byte slots
        int elapsed1 = 300; // 200 < elapsed1 <= 400
        int elapsed2 = 10; // elapsed2 <= 100
        int elapsed3 = 1000; // 800 < elapsed3 <= 1600
        byte[] rowKey1 = createTestRowKey(testApplicationName, elapsed1, false, testAgentId);
        byte[] rowKey2 = createTestRowKey(testApplicationName, elapsed2, false, testAgentId);
        byte[] rowKey3 = createTestRowKey(testApplicationName, elapsed3, false, testAgentId);
        byte[] value = createTestValue();

        Assertions.assertThat(isFilterOutRow(filter, createIndexKeyValue(rowKey1, value))).isEqualTo(false);
        Assertions.assertThat(isFilterOutRow(filter, createIndexKeyValue(rowKey2, value))).isEqualTo(true);
        Assertions.assertThat(isFilterOutRow(filter, createIndexKeyValue(rowKey3, value))).isEqualTo(true);
    }

    @Test
    public void elapsedByteRowFilterMinTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder();
        builder.setElapsedMin(0L);
        FilterList filter = builder.build(true, false);

        Assertions.assertThat(filter.getFilters()).hasSize(0);
    }

    @Test
    public void elapsedTimeValueFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder();
        builder.setElapsedMin(50L);
        builder.setElapsedMax(200L);
        Filter filter = builder.build(false, true);

        byte[] rowKey = createTestRowKey();
        byte[] value1 = createTestValue(100, testAgentId);
        byte[] value2 = createTestValue(10, testAgentId);
        byte[] value3 = createTestValue(300, testAgentId);

        Assertions.assertThat(getFilterReturnCode(filter, createIndexKeyValue(rowKey, value1))).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, createIndexKeyValue(rowKey, value2))).isEqualTo(Filter.ReturnCode.NEXT_ROW);
        Assertions.assertThat(getFilterReturnCode(filter, createIndexKeyValue(rowKey, value3))).isEqualTo(Filter.ReturnCode.NEXT_ROW);
    }

    @Test
    public void elapsedTimeValueFilterMinMaxTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder();
        builder.setElapsedMin(0L);
        builder.setElapsedMax(Long.MAX_VALUE); // integer max is enough
        FilterList filter = builder.build(false, true);

        Assertions.assertThat(filter.getFilters()).hasSize(0);
    }

    @Test
    public void rpcRegexValueFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder();
        builder.setRpcRegex("/test.*");
        Filter filter = builder.build(false, true);

        byte[] rowKey = createTestRowKey();
        KeyValue keyValue1 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX_META.getName(), HbaseTables.TRACE_INDEX_META_QUALIFIER_RPC, Bytes.toBytes("/test/include"));
        KeyValue keyValue2 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX_META.getName(), HbaseTables.TRACE_INDEX_META_QUALIFIER_RPC, Bytes.toBytes("/exclude"));
        KeyValue keyValue3 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX_META.getName(), HbaseTables.TRACE_INDEX_META_QUALIFIER_RPC, Bytes.toBytes("/other/exclude"));

        Assertions.assertThat(getFilterReturnCode(filter, keyValue1)).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, keyValue2)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
        Assertions.assertThat(getFilterReturnCode(filter, keyValue3)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
    }

    private boolean isFilterOutRow(Filter filter, KeyValue testKeyValue) throws IOException {
        filter.reset();
        return filter.filterRowKey(testKeyValue);
    }

    private Filter.ReturnCode getFilterReturnCode(Filter filter, KeyValue testKeyValue) throws IOException {
        filter.reset();
        return filter.filterCell(testKeyValue);
    }

    private byte[] createTestValue() {
        return TraceIndexValue.Index.encode(testAgentId, 100, 0);
    }

    private byte[] createTestValue(int elapsedTime, String agentId) {
        return TraceIndexValue.Index.encode(agentId, elapsedTime, 0);
    }

    private byte[] createTestRowKey() {
        return TraceIndexRowKeyUtils.createRowKeyWithSaltSize(1, 0, testApplicationName, ServiceType.TEST.getCode(), 1000, -1, 100, 0, testAgentId);
    }

    private byte[] createTestRowKey(String applicationName, int elapsed, boolean hasError, String agentId) {
        int errorCode = hasError ? 1 : 0;
        return TraceIndexRowKeyUtils.createRowKeyWithSaltSize(1, 0, applicationName, ServiceType.TEST.getCode(), 1000, -1, elapsed, errorCode, agentId);
    }

    private KeyValue createIndexKeyValue(byte[] row, byte[] value) {
        return new KeyValue(row, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), value);
    }
}
