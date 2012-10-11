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

	public boolean insert(final Span span, final byte[] spanBytes) {
        try {
            Put put = new Put(SpanUtils.getTracesRowkey(span), span.getTimestamp());
            put.add(COLFAM_SPAN, Bytes.toBytes(span.getSpanID()), spanBytes);
            hbaseTemplate.put(HBaseTables.TRACES, put);

            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

}
