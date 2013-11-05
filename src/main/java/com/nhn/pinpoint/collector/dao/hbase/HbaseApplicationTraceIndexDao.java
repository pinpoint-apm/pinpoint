package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.AGENT_NAME_MAX_LEN;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_TRACE_INDEX;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE;

import com.nhn.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.thrift.dto.TSpan;
import com.sematext.hbase.wd.AbstractRowKeyDistributor;
import org.apache.hadoop.hbase.client.Put;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.SpanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

/**
 * find traceids by application name
 * 
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseApplicationTraceIndexDao implements ApplicationTraceIndexDao {

	@Autowired
	private HbaseOperations2 hbaseTemplate;

    @Autowired
    private AcceptedTimeService acceptedTimeService;

    @Autowired
    @Qualifier("rowKeyDistributor")
    private AbstractRowKeyDistributor rowKeyDistributor;

	@Override
	public void insert(final TSpan span) {

        final Buffer buffer = new AutomaticBuffer(10 + AGENT_NAME_MAX_LEN);
        buffer.putVar(span.getElapsed());
        buffer.putSVar(span.getErr());
        buffer.putPrefixedString(span.getAgentId());
        final byte[] value = buffer.getBuffer();

        long acceptedTime = acceptedTimeService.getAcceptedTime();
        final byte[] distributedKey = crateRowKey(span, acceptedTime);
        Put put = new Put(distributedKey);

        put.add(APPLICATION_TRACE_INDEX_CF_TRACE, SpanUtils.getTransactionId(span), acceptedTime, value);

		hbaseTemplate.put(APPLICATION_TRACE_INDEX, put);
	}

    private byte[] crateRowKey(TSpan span, long acceptedTime) {
        // key를 n빵한다.
        byte[] applicationTraceIndexRowKey = SpanUtils.getApplicationTraceIndexRowKey(span.getApplicationName(), acceptedTime);
        return rowKeyDistributor.getDistributedKey(applicationTraceIndexRowKey);
    }
}
