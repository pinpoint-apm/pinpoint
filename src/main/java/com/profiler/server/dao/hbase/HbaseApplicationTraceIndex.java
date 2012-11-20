package com.profiler.server.dao.hbase;

import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.SpanUtils;
import com.profiler.server.dao.ApplicationTraceIndex;

/**
 * find traceids by application name
 *
 * @author netspider
 */
public class HbaseApplicationTraceIndex implements ApplicationTraceIndex {

    String TABLE_NAME = HBaseTables.APPLICATION_TRACE_INDEX;
    byte[] COLFAM_TRACE = HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE;
    byte[] COLNAME_ID = HBaseTables.APPLICATION_TRACE_INDEX_CN_ID;

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(String applicationName, final Span span) {
        // TODO 서버가 받은 시간으로 변경해야 될듯.
        Put put = new Put(SpanUtils.getApplicationTraceIndexRowKey(applicationName, span), span.getStartTime());
        put.add(COLFAM_TRACE, COLNAME_ID, SpanUtils.getTraceId(span));

        hbaseTemplate.put(TABLE_NAME, put);
    }
}
