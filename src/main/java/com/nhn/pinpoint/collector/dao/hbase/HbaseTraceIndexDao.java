package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.TRACE_INDEX_CF_TRACE;

import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.dto2.thrift.Span;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.SpanUtils;
import com.nhn.pinpoint.collector.dao.TraceIndexDao;

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
