package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.server.util.pair.LongPair;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.util.Bytes;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@Disabled // disabled until test pass GitHub action
public class TraceIndexFilterBuilderTest {

    private final String testApplicationName = "testApp";
    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();

    @Test
    public void applicationNameRowFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        Filter filter = builder.build(false, false);

        byte[] testApplicationRowKey = createTestFuzzyRowKey(testApplicationName);
        byte[] otherApplicationRowKey = createTestFuzzyRowKey("TESTAPP");

        Assertions.assertThat(getFilterReturnCode(filter, new KeyValue(testApplicationRowKey, 0L))).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, new KeyValue(otherApplicationRowKey, 0L))).isEqualTo(Filter.ReturnCode.SEEK_NEXT_USING_HINT); // fuzzyRowFilter returnCode
    }

    @Test
    public void elapsedByteRowFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        long high = 200;
        long low = 100;
        builder.setElapsedMinMax(new LongPair(low, high));
        Filter filter = builder.build(true, false);

        int elapsed1 = 150;
        Byte elapsedByte1 = fuzzyRowKeyFactory.getKey(elapsed1);
        byte[] rowKey1 = createTestFuzzyRowKey(testApplicationName, elapsedByte1);
        int elapsed2 = 1000;
        Byte elapsedByte2 = fuzzyRowKeyFactory.getKey(elapsed2);
        byte[] rowKey2 = createTestFuzzyRowKey(testApplicationName, elapsedByte2);

        Assertions.assertThat(elapsedByte1).isIn(fuzzyRowKeyFactory.getRangeKey(low, high));
        Assertions.assertThat(getFilterReturnCode(filter, new KeyValue(rowKey1, 0L))).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(elapsedByte2).isNotIn(fuzzyRowKeyFactory.getRangeKey(low, high));
        Assertions.assertThat(getFilterReturnCode(filter, new KeyValue(rowKey2, 0L))).isEqualTo(Filter.ReturnCode.SEEK_NEXT_USING_HINT);
    }

    @Test
    public void successValueFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        builder.setSuccess(true);
        Filter filter = builder.build(false, true);

        byte[] rowKey = createTestFuzzyRowKey(testApplicationName);
        byte[] successValue = createTestValue(false, 100, "testAgentId");
        byte[] failureValue = createTestValue(true, 100, "testAgentId");
        // first byte 0 means success, 1 means failure
        KeyValue successKeyValue = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), successValue);
        KeyValue failureKeyValue = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), failureValue);

        Assertions.assertThat(getFilterReturnCode(filter, successKeyValue)).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, failureKeyValue)).isEqualTo(Filter.ReturnCode.NEXT_ROW); // singleColumnValueFilter returnCode
    }

    @Test
    public void agentIdValueFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        String testAgentId = "testAgent";
        builder.setAgentId(testAgentId);
        Filter filter = builder.build(false, true);

        byte[] testAgentIdValue = createTestValue(true, 100, testAgentId);
        byte[] otherAgentIdValue = createTestValue(true, 100, "otherAgent");

        byte[] rowKey = createTestFuzzyRowKey(testApplicationName);
        KeyValue testAgentKeyValue = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), testAgentIdValue);
        KeyValue otherAgentKeyValue = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), otherAgentIdValue);

        Assertions.assertThat(getFilterReturnCode(filter, testAgentKeyValue)).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, otherAgentKeyValue)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
    }

    @Test
    public void successAgentIdValueFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        String testAgentId = "testAgent";
        builder.setAgentId(testAgentId);
        builder.setSuccess(true);
        Filter filter = builder.build(false, true);

        byte[] value1 = createTestValue(false, 100, testAgentId);
        byte[] value2 = createTestValue(true, 100, testAgentId);
        byte[] value3 = createTestValue(false, 100, "TESTAGENT");

        byte[] rowKey = createTestFuzzyRowKey(testApplicationName);
        KeyValue keyValue1 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), value1);
        KeyValue keyValue2 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), value2);
        KeyValue keyValue3 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), value3);

        Assertions.assertThat(getFilterReturnCode(filter, keyValue1)).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, keyValue2)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
        Assertions.assertThat(getFilterReturnCode(filter, keyValue3)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
    }

    @Test
    public void elapsedTimeValueFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        String testAgentId = "testAgent";
        builder.setElapsedMinMax(new LongPair(50, 200));
        Filter filter = builder.build(false, true);

        byte[] value1 = createTestValue(true, 100, testAgentId);
        byte[] value2 = createTestValue(true, 10, testAgentId);
        byte[] value3 = createTestValue(true, 300, testAgentId);

        byte[] rowKey = createTestFuzzyRowKey(testApplicationName);
        KeyValue keyValue1 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), value1);
        KeyValue keyValue2 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), value2);
        KeyValue keyValue3 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), value3);

        Assertions.assertThat(getFilterReturnCode(filter, keyValue1)).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, keyValue2)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
        Assertions.assertThat(getFilterReturnCode(filter, keyValue3)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
    }

    @Test
    public void rpcRegexValueFilterTest() throws IOException {
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        builder.setRpcRegex("/test.*");
        Filter filter = builder.build(false, true);

        byte[] rowKey = createTestFuzzyRowKey(testApplicationName);
        KeyValue keyValue1 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX_META.getName(), HbaseTables.TRACE_INDEX_META_QUALIFIER_RPC, Bytes.toBytes("/test/include"));
        KeyValue keyValue2 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX_META.getName(), HbaseTables.TRACE_INDEX_META_QUALIFIER_RPC, Bytes.toBytes("/test/include2"));
        KeyValue keyValue3 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX_META.getName(), HbaseTables.TRACE_INDEX_META_QUALIFIER_RPC, Bytes.toBytes("/other/exclude"));

        Assertions.assertThat(getFilterReturnCode(filter, keyValue1)).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, keyValue2)).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, keyValue3)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
    }

    private Filter.ReturnCode getFilterReturnCode(Filter filter, KeyValue testAgentKeyValue) throws IOException {
        Filter.ReturnCode returnCode1 = filter.filterCell(testAgentKeyValue);
        filter.reset();
        return returnCode1;
    }

    private byte[] createTestValue(boolean hasError, int elapsedTime, String agentId) {
        int errorCode = hasError ? 1 : 0;
        return TraceIndexValue.Index.encode(agentId, elapsedTime, errorCode);
    }

    private byte[] createTestFuzzyRowKey(String applicationName) {
        Byte elapsedByte = fuzzyRowKeyFactory.getKey(100);
        return TraceIndexRowKeyUtils.createFuzzyRowKey(1, 0, applicationName, ServiceType.TEST.getCode(), 1000, elapsedByte, -1);
    }

    private byte[] createTestFuzzyRowKey(String applicationName, byte elapsedByte) {
        return TraceIndexRowKeyUtils.createFuzzyRowKey(1, 0, applicationName, ServiceType.TEST.getCode(), 1000, elapsedByte, -1);
    }
}
