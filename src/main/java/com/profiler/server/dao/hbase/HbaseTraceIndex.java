package com.profiler.server.dao.hbase;

import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.SpanUtils;
import com.profiler.server.dao.TraceIndex;

public class HbaseTraceIndex implements TraceIndex {

	String tableName = HBaseTables.TRACE_INDEX;
	byte[] COLFAM_TRACE = HBaseTables.TRACE_INDEX_CF_TRACE;
	byte[] COLNAME_ID = HBaseTables.TRACE_INDEX_CN_ID;

	@Autowired
	private HbaseOperations2 hbaseTemplate;
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public void insert(final Span span) {
		Put put = new Put(SpanUtils.getTraceIndexRowKey(span), span.getTimestamp());
		put.add(COLFAM_TRACE, COLNAME_ID, SpanUtils.getTraceId(span));

		hbaseTemplate.put(tableName, put);
	}
}
