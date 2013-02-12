package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.BUSINESS_TRANSACTION_STATISTICS;
import static com.profiler.common.hbase.HBaseTables.BUSINESS_TRANSACTION_STATISTICS_CF_ERROR;
import static com.profiler.common.hbase.HBaseTables.BUSINESS_TRANSACTION_STATISTICS_CF_NORMAL;

import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.dto.thrift.Span;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.BytesUtils;
import com.profiler.common.util.TimeSlot;
import com.profiler.server.dao.BusinessTransactionStatisticsDao;

/**
 * @author netspider
 */
public class HbaseBusinessTransactionStatisticsDao implements BusinessTransactionStatisticsDao {

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Override
	public void update(String applicationName, Span span) {
		// TODO : collector가 데이터를 받은 시간으로 변경
		long rowTimeSlot = TimeSlot.getStatisticsRowSlot(System.currentTimeMillis());
		
		byte[] rowKey = BytesUtils.merge(BytesUtils.toFixedLengthBytes(applicationName, 24), Bytes.toBytes(rowTimeSlot));
		byte[] cf;
        if (span.getErr() == 0) {
            // 에러 없음.
            cf = BUSINESS_TRANSACTION_STATISTICS_CF_NORMAL;
        } else {
            // 에러 있음.
            cf = BUSINESS_TRANSACTION_STATISTICS_CF_ERROR;
        }
        byte[] columnName = Bytes.toBytes(span.getRpc()); // application url

		hbaseTemplate.incrementColumnValue(BUSINESS_TRANSACTION_STATISTICS, rowKey, cf, columnName, 1L);
	}
}