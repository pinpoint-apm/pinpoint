package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.hbase.HBaseTables;

import com.nhn.pinpoint.common.util.ApplicationStatisticsUtils;
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
        ResponseTime responseTime = createRawResponseTime(rowKey);

        for (KeyValue keyValue : result.raw()) {
            if (!Bytes.equals(keyValue.getFamily(), HBaseTables.MAP_STATISTICS_SELF_CF_COUNTER)) {
                continue;
            }
            byte[] qualifier = keyValue.getQualifier();

            recordColumn(responseTime, qualifier, keyValue.getBuffer(), keyValue.getValueOffset());
        }
        return responseTime;
    }

    void recordColumn(ResponseTime responseTime, byte[] qualifier, byte[] value) {
        recordColumn(responseTime, qualifier, value, 0);
    }
    void recordColumn(ResponseTime responseTime, byte[] qualifier, byte[] value, int valueOffset) {
        short slotNumber = Bytes.toShort(qualifier);
        // agentId도 데이터로 같이 엮어야 함.
        String agentId = Bytes.toString(qualifier, 2, qualifier.length - 2);
        long count = Bytes.toLong(value, valueOffset);
        responseTime.getHistogram(agentId).addSample(slotNumber, count);
    }

    private ResponseTime createRawResponseTime(byte[] rowKey) {

        String applicationName = ApplicationStatisticsUtils.getApplicationNameFromRowKey(rowKey);
        short serviceType = ApplicationStatisticsUtils.getApplicationTypeFromRowKey(rowKey);
        long time = TimeUtils.recoveryCurrentTimeMillis(ApplicationStatisticsUtils.getTimestampFromRowKey(rowKey));
        return new ResponseTime(applicationName, serviceType, time);
    }
}
