package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.server.util.pair.LongPair;
import com.navercorp.pinpoint.common.trace.ServiceType;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.filter.Filter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TraceIndexFilterBuilderTest {

    private final String testApplicationName = "testApp";
    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();

    @Test
    public void applicationNameRowFilterTest() throws IOException {
        Jdk17Utils.assumeFalse();
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        Filter filter = builder.build(false, false);

        byte[] testApplicationRowKey = createTestFuzzyRowKey(testApplicationName, 100);
        byte[] otherApplicationRowKey = createTestFuzzyRowKey("TESTAPP", 100);

        Assertions.assertThat(getFilterReturnCode(filter, new KeyValue(testApplicationRowKey, 0L))).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, new KeyValue(otherApplicationRowKey, 0L))).isEqualTo(Filter.ReturnCode.SEEK_NEXT_USING_HINT); // fuzzyRowFilter returnCode
    }

    @Test
    public void elapsedByteRowFilterTest() throws IOException {
        Jdk17Utils.assumeFalse();
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
        Jdk17Utils.assumeFalse();
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        builder.setSuccess(true);
        Filter filter = builder.build(false, true);

        byte[] rowKey = createTestFuzzyRowKey(testApplicationName, 100);
        // first byte 0 means success, 1 means failure
        KeyValue successKeyValue = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), new byte[]{0, 0, 0});
        KeyValue failureKeyValue = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), new byte[]{1, 0, 0});

        Assertions.assertThat(getFilterReturnCode(filter, successKeyValue)).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, failureKeyValue)).isEqualTo(Filter.ReturnCode.NEXT_ROW); // singleColumnValueFilter returnCode
    }

    @Test
    public void agentIdValueFilterTest() throws IOException {
        Jdk17Utils.assumeFalse();
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        String testAgentId = "testAgent";
        builder.setAgentId(testAgentId);
        Filter filter = builder.build(false, true);

        Buffer buffer1 = new AutomaticBuffer();
        buffer1.putByte((byte) 0);
        buffer1.putPrefixedString(testAgentId);
        byte[] testAgentIdValue = buffer1.getBuffer();
        Buffer buffer2 = new AutomaticBuffer();
        buffer2.putByte((byte) 0);
        buffer2.putPrefixedString("otherAgent");
        byte[] otherAgentIdValue = buffer2.getBuffer();

        byte[] rowKey = createTestFuzzyRowKey(testApplicationName, 100);
        KeyValue testAgentKeyValue = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), testAgentIdValue);
        KeyValue otherAgentKeyValue = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), otherAgentIdValue);

        Assertions.assertThat(getFilterReturnCode(filter, testAgentKeyValue)).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, otherAgentKeyValue)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
    }

    @Test
    public void successAgentIdValueFilterTest() throws IOException {
        Jdk17Utils.assumeFalse();
        TraceIndexFilterBuilder builder = new TraceIndexFilterBuilder(testApplicationName);
        String testAgentId = "testAgent";
        builder.setAgentId(testAgentId);
        builder.setSuccess(true);
        Filter filter = builder.build(false, true);

        Buffer buffer1 = new AutomaticBuffer();
        buffer1.putByte((byte) 0);
        buffer1.putPrefixedString(testAgentId);
        byte[] value1 = buffer1.getBuffer();
        Buffer buffer2 = new AutomaticBuffer();
        buffer2.putByte((byte) 1);
        buffer2.putPrefixedString(testAgentId);
        byte[] value2 = buffer2.getBuffer();
        Buffer buffer3 = new AutomaticBuffer();
        buffer3.putByte((byte) 0);
        buffer3.putPrefixedString("TESTAGENT");
        byte[] value3 = buffer3.getBuffer();

        byte[] rowKey = createTestFuzzyRowKey(testApplicationName, 100);
        KeyValue keyValue1 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), value1);
        KeyValue keyValue2 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), value2);
        KeyValue keyValue3 = new KeyValue(rowKey, HbaseTables.TRACE_INDEX.getName(), HbaseTables.TRACE_INDEX.getName(), value3);

        Assertions.assertThat(getFilterReturnCode(filter, keyValue1)).isEqualTo(Filter.ReturnCode.INCLUDE);
        Assertions.assertThat(getFilterReturnCode(filter, keyValue2)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
        Assertions.assertThat(getFilterReturnCode(filter, keyValue3)).isEqualTo(Filter.ReturnCode.NEXT_ROW);
    }

    private Filter.ReturnCode getFilterReturnCode(Filter filter, KeyValue testAgentKeyValue) throws IOException {
        Filter.ReturnCode returnCode1 = filter.filterCell(testAgentKeyValue);
        filter.reset();
        return returnCode1;
    }

    private byte[] createTestFuzzyRowKey(String applicationName, int elapsed) {
        Byte elapsedByte = fuzzyRowKeyFactory.getKey(elapsed);
        return TraceIndexRowKey.createFuzzyRowKey(1, 0, applicationName, ServiceType.TEST.getCode(), 1000, elapsedByte, -1);
    }

    private byte[] createTestFuzzyRowKey(String applicationName, byte elapsedByte) {
        return TraceIndexRowKey.createFuzzyRowKey(1, 0, applicationName, ServiceType.TEST.getCode(), 1000, elapsedByte, -1);
    }
}
