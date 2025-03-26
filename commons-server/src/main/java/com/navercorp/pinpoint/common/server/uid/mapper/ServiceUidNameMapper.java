package com.navercorp.pinpoint.common.server.uid.mapper;

import com.navercorp.pinpoint.common.hbase.RowMapper;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

@Component
public class ServiceUidNameMapper implements RowMapper<String> {

    @Override
    public String mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }

        byte[] rowKey = result.getRow();
        String serviceName = Bytes.toString(rowKey);

        return serviceName;
    }
}
