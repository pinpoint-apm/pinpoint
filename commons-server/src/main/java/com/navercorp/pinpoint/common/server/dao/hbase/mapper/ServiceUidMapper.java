package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.UuidUtils;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ServiceUidMapper implements RowMapper<UUID> {

    private static final HbaseColumnFamily.ServiceUid DESCRIPTOR = HbaseColumnFamily.SERVICE_UID;

    @Override
    public UUID mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
        byte[] family = DESCRIPTOR.getName();
        byte[] qualifier = DESCRIPTOR.getName();

        byte[] serializedServiceUid = result.getValue(family, qualifier);
        return UuidUtils.toUUID(serializedServiceUid);
    }
}
