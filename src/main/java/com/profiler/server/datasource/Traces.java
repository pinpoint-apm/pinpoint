package com.profiler.server.datasource;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.apache.thrift.TSerializer;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseClient;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.SpanUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class Traces {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private final byte[] COLFAM_SPAN = Bytes.toBytes("Span");

    @Autowired
	private HBaseClient client;

	public boolean insert(Span span) {
		try {
			byte[] value = new TSerializer().serialize(span);

			Put put = new Put(SpanUtils.getTracesRowkey(span), span.getTimestamp());
			put.add(COLFAM_SPAN, Bytes.toBytes(span.getSpanID()), value);

			client.insert(HBaseTables.TRACES, put);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}
}
