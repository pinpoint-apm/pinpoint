package com.profiler.server.dao;

import com.profiler.common.hbase.HbaseOperations2;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.SpanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


public class HbaseTraceIndex implements TraceIndex {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private String tableName = HBaseTables.TRACE_INDEX;
    private byte[] COLFAM_TRACE = Bytes.toBytes("Trace");
    private byte[] COLNAME_ID = Bytes.toBytes("ID");

    public HbaseTraceIndex() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Autowired
    private HbaseOperations2 hbaseTemplate;


    @Override
    public void insert(final Span span) {
        Put put = new Put(SpanUtils.getTraceIndexRowKey(span), span.getTimestamp());
        put.add(COLFAM_TRACE, COLNAME_ID, SpanUtils.getTraceId(span));

        hbaseTemplate.put(tableName, put);
    }
}
