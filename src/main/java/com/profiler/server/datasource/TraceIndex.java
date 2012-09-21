package com.profiler.server.datasource;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseClient;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.SpanUtils;

public class TraceIndex {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private final byte[] COLFAM_TRACE = Bytes.toBytes("Trace");
	private final byte[] COLNAME_ID = Bytes.toBytes("ID");

	private final HBaseClient client = HBaseClient.getInstance();

	public boolean insert(Span span) {
		try {
			Put put = new Put(SpanUtils.getTraceIndexRowKey(span), span.getTimestamp());
			put.add(COLFAM_TRACE, COLNAME_ID, SpanUtils.getTraceId(span));

			client.insert(HBaseTables.TRACE_INDEX, put);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
}
