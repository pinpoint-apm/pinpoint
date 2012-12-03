package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.TERMINAL_STATISTICS;
import static com.profiler.common.hbase.HBaseTables.TERMINAL_STATISTICS_CF_COUNTER;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
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

		final Put put = new Put(rowKey);
		put.add(TERMINAL_STATISTICS_CF_COUNTER, columnName, Bytes.toBytes(1));

		hbaseTemplate.put(TERMINAL_STATISTICS, put);

		// TODO respose time histogram, agentId list가 추가되어야 함.
	}
}
