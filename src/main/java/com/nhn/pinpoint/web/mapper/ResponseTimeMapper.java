package com.nhn.pinpoint.web.mapper;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.web.vo.RawResponseTime;
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
        final byte[] row = result.getRow();
        final Buffer rowBuffer = new FixedBuffer(row);
        rowBuffer.readPrefixedString();

    }
}
