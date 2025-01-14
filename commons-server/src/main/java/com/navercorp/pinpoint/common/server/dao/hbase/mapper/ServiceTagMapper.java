package com.navercorp.pinpoint.common.server.dao.hbase.mapper;

import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.hbase.RowMapper;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.client.Result;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;

@Component
public class ServiceTagMapper implements RowMapper<Map<String, String>> {

    private static final HbaseColumnFamily.ServiceTag TAG = HbaseColumnFamily.SERVICE_TAG;

    @Override
    public Map<String, String> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyMap();
        }
        NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(TAG.getName());

        Map<String, String> resultMap = new HashMap<>(familyMap.size());
        for (Map.Entry<byte[], byte[]> entry : familyMap.entrySet()) {
            String qualifierString = BytesUtils.toString(entry.getKey());
            String valueString = BytesUtils.toString(entry.getValue());
            resultMap.put(qualifierString, valueString);
        }

        return resultMap;
    }
}
