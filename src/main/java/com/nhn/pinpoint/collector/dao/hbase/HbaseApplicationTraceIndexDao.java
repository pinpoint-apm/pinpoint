package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_TRACE_INDEX;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_TRACE_INDEX_CF_TRACE;

import com.nhn.pinpoint.collector.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
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
		int elapsedTime = span.getElapsed();

		byte[] value = new byte[8];
		BytesUtils.writeInt(elapsedTime, value, 0);
		BytesUtils.writeInt(span.getErr(), value, 4);


        long acceptedTime = acceptedTimeService.getAcceptedTime();

        byte[] distributedKey = crateRowKey(span, acceptedTime);
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
