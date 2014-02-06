package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.hbase.HBaseTables;

import com.nhn.pinpoint.common.util.ApplicationStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.vo.RawResponseTime;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author emeroad
 */
@Component
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
            byte[] qualifier = keyValue.getQualifier();
            byte[] value = keyValue.getValue();
            recordColumn(rawResponseTime, qualifier, value);
        }
        return rawResponseTime;
    }

    void recordColumn(RawResponseTime rawResponseTime, byte[] qualifier, byte[] value) {
        short slotNumber = Bytes.toShort(qualifier);
        // agentId도 데이터로 같이 엮어야 함.
        String agentId = Bytes.toString(qualifier, 2, qualifier.length - 2);
        long count = Bytes.toLong(value);
        rawResponseTime.getHistogram(agentId).addSample(slotNumber, count);
    }

    private RawResponseTime createRawResponseTime(byte[] rowKey) {

        String applicationName = ApplicationStatisticsUtils.getApplicationNameFromRowKey(rowKey);
        short serviceType = ApplicationStatisticsUtils.getApplicationTypeFromRowKey(rowKey);
        long time = TimeUtils.recoveryCurrentTimeMillis(ApplicationStatisticsUtils.getTimestampFromRowKey(rowKey));
        return new RawResponseTime(applicationName, serviceType, time);
    }
}
