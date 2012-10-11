package com.nhn.hippo.web.dao;

import com.nhn.hippo.web.mapper.SpanMapper;
import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseTemplate2;
import com.profiler.common.util.BytesUtils;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 *
 */
@Repository
public class HbaseTraceDao implements TraceDao {

    private final byte[] COLFAM_SPAN = Bytes.toBytes("Span");
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseTemplate2 template2;

    @Autowired
    private SpanMapper<List<Span>> spanMapper;


    @Override
    public List<Span> readSpan(UUID uuid) {
        byte[] uuidBytes = BytesUtils.longLongToBytes(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
        return template2.get(HBaseTables.TRACES, uuidBytes, COLFAM_SPAN, spanMapper);
    }
}
