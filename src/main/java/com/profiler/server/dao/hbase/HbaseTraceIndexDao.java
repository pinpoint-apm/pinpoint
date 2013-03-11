package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.TRACE_INDEX_CF_TRACE;

import com.profiler.server.util.AcceptedTimeService;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.SpanUtils;
import com.profiler.server.dao.TraceIndexDao;

public class HbaseTraceIndexDao implements TraceIndexDao {

	String tableName = HBaseTables.TRACE_INDEX;

	@Autowired
	private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public void insert(final Span span) {

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        byte[] agentIdTraceIndexRowKey = SpanUtils.getAgentIdTraceIndexRowKey(span.getAgentId(), acceptedTime);

        Put put = new Put(agentIdTraceIndexRowKey);
        put.add(TRACE_INDEX_CF_TRACE, SpanUtils.getTraceId(span), acceptedTime, null);

		hbaseTemplate.put(tableName, put);
	}
}
