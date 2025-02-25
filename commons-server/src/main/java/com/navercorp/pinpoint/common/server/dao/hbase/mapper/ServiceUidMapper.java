package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.stereotype.Component;

@Component
public class ServiceUidMapper implements RowMapper<ServiceUid> {

    private static final HbaseColumnFamily.ServiceUid DESCRIPTOR = HbaseColumnFamily.SERVICE_UID;

    @Override
    public ServiceUid mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return null;
        }
        byte[] family = DESCRIPTOR.getName();
        byte[] qualifier = DESCRIPTOR.getName();

        byte[] serializedServiceUid = result.getValue(family, qualifier);
        int serviceUid = Bytes.toInt(serializedServiceUid);
        return new ServiceUid(serviceUid);
    }
}
