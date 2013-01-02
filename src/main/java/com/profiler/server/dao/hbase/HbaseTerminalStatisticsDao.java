package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.TERMINAL_STATISTICS;
import static com.profiler.common.hbase.HBaseTables.TERMINAL_STATISTICS_CF_COUNTER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.TerminalSpanUtils;
import com.profiler.common.util.TimeSlot;
import com.profiler.server.dao.TerminalStatisticsDao;

/**
 * 
 * @author netspider
 * 
 */
public class HbaseTerminalStatisticsDao implements TerminalStatisticsDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Override
	public void update(String sourceApplicationName, String destApplicationName, short destServiceType) {
		if (logger.isDebugEnabled()) {
			logger.debug("[WritingTerminalStatistics] " + sourceApplicationName + " -> " + destApplicationName + " (" + ServiceType.parse(destServiceType) + ")");
		}

		byte[] columnName = TerminalSpanUtils.makeColumnName(destServiceType, destApplicationName);

		// TODO collector시간으로 변경??
		long timeSlot = TimeSlot.getSlot(System.currentTimeMillis());
		final byte[] rowKey = TerminalSpanUtils.makeRowKey(sourceApplicationName, timeSlot);

//		final Put put = new Put(rowKey);
//		put.add(TERMINAL_STATISTICS_CF_COUNTER, columnName, Bytes.toBytes(1));
		
//		Get get = new Get(rowKey);
//		Long count = hbaseTemplate.get(TERMINAL_STATISTICS, get, new RowMapper<Long>() {
//			@Override
//			public Long mapRow(Result result, int rowNum) throws Exception {
//				if (result != null) {
//					KeyValue[] kv = result.raw();
//					if (kv.length > 0) {
//						return Bytes.toLong(kv[0].getValue());
//					}
//				}
//				return null;
//			}
//		});
//		
//		System.out.println("before = " + count);
		
		hbaseTemplate.incrementColumnValue(TERMINAL_STATISTICS, rowKey, TERMINAL_STATISTICS_CF_COUNTER, columnName, 1L);
		
//		System.out.println("increment value," + sourceApplicationName + " => " + destApplicationName);
		
//		Get get2 = new Get(rowKey);
//		Long count2 = hbaseTemplate.get(TERMINAL_STATISTICS, get2, new RowMapper<Long>() {
//			@Override
//			public Long mapRow(Result result, int rowNum) throws Exception {
//				if (result != null) {
//					KeyValue[] kv = result.raw();
//					if (kv.length > 0) {
//						return Bytes.toLong(kv[0].getValue());
//					}
//				}
//				return null;
//			}
//		});
//		
//		System.out.println("after = " + count2);		
		
		
//		hbaseTemplate.put(TERMINAL_STATISTICS, put);

		// TODO respose time histogram, agentId list가 추가되어야 함.
	}
}
