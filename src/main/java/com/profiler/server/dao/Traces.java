package com.profiler.server.dao;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.SpanUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class Traces {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final byte[] COLFAM_SPAN = Bytes.toBytes("Span");

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    public void insert(final Span span, final byte[] spanBytes) {
        Put put = new Put(SpanUtils.getTracesRowkey(span), span.getTimestamp());
        // TODO columName이 중복일 경우를 확인가능하면 span id 중복 발급을 알수 있음.
        put.add(COLFAM_SPAN, Bytes.toBytes(span.getSpanID()), spanBytes);
        hbaseTemplate.put(HBaseTables.TRACES, put);
    }

}
