package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;

import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.vo.ResponseTime;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author emeroad
 */
@Component
public class ResponseTimeMapper implements RowMapper<ResponseTime> {
    @Override
    public ResponseTime mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
        final byte[] rowKey = result.getRow();
        ResponseTime responseTime = createResponseTime(rowKey);

        for (KeyValue keyValue : result.raw()) {
            if (!Bytes.equals(keyValue.getFamily(), HBaseTables.MAP_STATISTICS_SELF_CF_COUNTER)) {
                continue;
            }
            byte[] qualifier = keyValue.getQualifier();

            recordColumn(responseTime, qualifier, keyValue.getBuffer(), keyValue.getValueOffset());
        }
        return responseTime;
    }



    void recordColumn(ResponseTime responseTime, byte[] qualifier, byte[] value, int valueOffset) {
        short slotNumber = Bytes.toShort(qualifier);
        // agentId도 데이터로 같이 엮어야 함.
        String agentId = Bytes.toString(qualifier, 2, qualifier.length - 2);
        long count = Bytes.toLong(value, valueOffset);
        responseTime.addResponseTime(agentId, slotNumber, count);
    }

    private ResponseTime createResponseTime(byte[] rowKey) {
        final Buffer row = new FixedBuffer(rowKey);
        String applicationName = row.read2PrefixedString();
        short serviceType = row.readShort();
        final long timestamp = TimeUtils.recoveryCurrentTimeMillis(row.readLong());
        return new ResponseTime(applicationName, serviceType, timestamp);
    }

}
