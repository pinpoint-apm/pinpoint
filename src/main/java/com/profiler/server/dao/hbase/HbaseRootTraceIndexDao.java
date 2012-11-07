package com.profiler.server.dao.hbase;

import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.SpanUtils;
import com.profiler.server.dao.RootTraceIndexDao;

public class HbaseRootTraceIndexDao implements RootTraceIndexDao {
	// 소스가 HbaeTraceIndexDao 동일함. 중복이므로 향후 합치는 방안강구.
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	String TABLE_NAME = HBaseTables.ROOT_TRACE_INDEX;
	byte[] COLFAM_TRACE = HBaseTables.ROOT_TRACE_INDEX_CF_TRACE;
	byte[] COLNAME_ID = HBaseTables.ROOT_TRACE_INDEX_CN_ID;

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Override
	public void insert(final Span rootSpan) {
		if (rootSpan.getParentSpanId() != -1) {
			logger.debug("invalid root span{}", rootSpan);
			return;
		}
		Put put = new Put(SpanUtils.getTraceIndexRowKey(rootSpan), rootSpan.getTimestamp());
		put.add(COLFAM_TRACE, COLNAME_ID, SpanUtils.getTraceId(rootSpan));

		hbaseTemplate.put(TABLE_NAME, put);
	}
}
