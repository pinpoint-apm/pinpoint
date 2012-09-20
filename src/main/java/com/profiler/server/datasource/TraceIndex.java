package com.profiler.server.datasource;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseClient;

public class TraceIndex {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private final byte[] TABLE_TRACE_INDEX = Bytes.toBytes("TraceIndex");
	private final byte[] COLFAM_TRACE = Bytes.toBytes("Trace");
	private final byte[] COLNAME_ID = Bytes.toBytes("ID");

	private final HBaseClient client = HBaseClient.getInstance();

	public boolean insert(Span span) {
		try {
			byte[] agentId = Bytes.toBytes(span.getAgentID());
			byte[] time = Bytes.toBytes(span.getTimestamp());
			byte[] mostTid = Bytes.toBytes(span.getMostTraceID());
			byte[] leastTid = Bytes.toBytes(span.getLeastTraceID());

			Put put = new Put(ArrayUtils.addAll(agentId, time), span.getTimestamp());
			put.add(COLFAM_TRACE, COLNAME_ID, ArrayUtils.addAll(mostTid, leastTid));
			client.insert(TABLE_TRACE_INDEX, put);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
}
