package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;

import com.nhn.pinpoint.web.vo.RawResponseTime;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;

/**
 * @author emeroad
 */
public class ResponseTimeMapper implements RowMapper<RawResponseTime> {
    @Override
    public RawResponseTime mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
        final byte[] rowKey = result.getRow();
        RawResponseTime rawResponseTime = createRawResponseTime(rowKey);

        for (KeyValue keyValue : result.raw()) {
            if (!Bytes.equals(keyValue.getFamily(), HBaseTables.APPLICATION_MAP_STATISTICS_SELF_CF_COUNTER)) {
                return rawResponseTime;
            }
            final byte[] row = keyValue.getRow();
            final byte[] value = keyValue.getValue();
            recordColumn(rawResponseTime, row, value);
        }
        return rawResponseTime;
    }

    void recordColumn(RawResponseTime rawResponseTime, byte[] row, byte[] value) {
        short slotNumber = Bytes.toShort(row);
        // agentId도 데이터로 같이 엮어야 함.
        String agentId = Bytes.toString(row, 2, row.length - 2);
        long count = Bytes.toLong(value);
        rawResponseTime.getHistogram(agentId).addSample(slotNumber, count);
    }

    private RawResponseTime createRawResponseTime(byte[] rowKey) {
        final Buffer rowBuffer = new FixedBuffer(rowKey);

        String applicationName = rowBuffer.readPrefixedString();
        short serviceType = rowBuffer.readShort();
        long time = com.nhn.pinpoint.common.util.TimeUtils.recoveryCurrentTimeMillis(rowBuffer.readLong());

        return new RawResponseTime(applicationName, serviceType, time);
    }
}
