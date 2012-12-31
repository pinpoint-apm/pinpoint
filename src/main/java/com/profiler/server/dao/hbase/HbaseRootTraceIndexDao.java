package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.ROOT_TRACE_INDEX;
import static com.profiler.common.hbase.HBaseTables.ROOT_TRACE_INDEX_CF_TRACE;

import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.SpanUtils;
import com.profiler.server.dao.RootTraceIndexDaoDao;

public class HbaseRootTraceIndexDao implements RootTraceIndexDaoDao {
	// TODO 소스가 HbaeTraceIndexDao 동일함. 중복이므로 향후 합치는 방안강구.
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Override
	public void insert(final Span rootSpan) {
		if (rootSpan.getParentSpanId() != -1) {
			logger.debug("invalid root span{}", rootSpan);
			return;
		}

		// TODO 서버가 받은 시간을 키로 사용해야 될듯 함???
		Put put = new Put(SpanUtils.getTraceIndexRowKey(rootSpan), rootSpan.getStartTime());
		put.add(ROOT_TRACE_INDEX_CF_TRACE, SpanUtils.getTraceId(rootSpan), null);

		hbaseTemplate.put(ROOT_TRACE_INDEX, put);
	}
}
