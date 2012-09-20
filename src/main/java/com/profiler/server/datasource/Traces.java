package com.profiler.server.datasource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.thrift.TSerializer;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseClient;

public class Traces {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private final byte[] TABLE_TRACE = Bytes.toBytes("Traces");
	private final byte[] COLFAM_SPAN = Bytes.toBytes("Span");

	private final HBaseClient client = HBaseClient.getInstance();

	public boolean insert(Span span) {
		try {
			byte[] traceId = ArrayUtils.addAll(Bytes.toBytes(span.getMostTraceID()), Bytes.toBytes(span.getLeastTraceID()));
			byte[] value = new TSerializer().serialize(span);

			Put put = new Put(traceId, span.getTimestamp());
			put.add(COLFAM_SPAN, Bytes.toBytes(span.getSpanID()), value);
			client.insert(TABLE_TRACE, put);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
	
	
	
	
}
