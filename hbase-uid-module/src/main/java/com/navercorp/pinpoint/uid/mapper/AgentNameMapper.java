package com.navercorp.pinpoint.uid.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.uid.utils.UidBytesParseUtils;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

@Component
public class AgentNameMapper implements RowMapper<String> {

    @Override
    public String mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        byte[] rowKey = result.getRow();
        return UidBytesParseUtils.parseAgentId(rowKey);
    }
}
