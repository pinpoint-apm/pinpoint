package com.navercorp.pinpoint.web.mapper;

import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

@Component
public class AgentIdMapperV2 implements RowMapper<String> {

    @Override
    public String mapRow(Result result, int rowNum) throws Exception {
        byte[] rowKey = result.getRow();
        Buffer buffer = new FixedBuffer(rowKey);
        buffer.readInt(); //serviceUid
        buffer.readPrefixedString(); //applicationName
        buffer.readInt(); //serviceTypeCode

        return buffer.readPrefixedString();
    }
}
