package com.nhn.hippo.web.mapper;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class TraceIndexMapper implements RowMapper<byte[]> {

    private final byte[] COLFAM_TRACE = Bytes.toBytes("Trace");
    private final byte[] COLNAME_ID = Bytes.toBytes("ID");

    @Override
    public byte[] mapRow(Result result, int rowNum) throws Exception {

        // TODO null처리 해야 될듯.
        return result.getValue(COLFAM_TRACE, COLNAME_ID);
    }
}
