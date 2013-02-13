package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.ROOT_TRACE_INDEX;
import static com.profiler.common.hbase.HBaseTables.ROOT_TRACE_INDEX_CF_TRACE;

import com.profiler.server.util.AcceptedTime;
import org.apache.hadoop.hbase.client.Put;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.SpanUtils;
import com.profiler.server.dao.RootTraceIndexDaoDao;

@Deprecated
public class HbaseRootTraceIndexDao implements RootTraceIndexDaoDao {
    // TODO 소스가 HbaeTraceIndexDao 동일함. 중복이므로 향후 합치는 방안강구.
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HbaseOperations2 hbaseTemplate;

    @Override
    public void insert(final Span rootSpan) {
        if (rootSpan.getParentSpanId() != -1) {
            logger.debug("invalid root span{}", rootSpan);
            return;
        }

        long acceptedTime = AcceptedTime.getAcceptedTime();
        byte[] agentIdTraceIndexRowKey = SpanUtils.getAgentIdTraceIndexRowKey(rootSpan.getAgentId(), acceptedTime);
        Put put = new Put(agentIdTraceIndexRowKey);
        put.add(ROOT_TRACE_INDEX_CF_TRACE, SpanUtils.getTraceId(rootSpan), acceptedTime, null);

        hbaseTemplate.put(ROOT_TRACE_INDEX, put);
    }
}
