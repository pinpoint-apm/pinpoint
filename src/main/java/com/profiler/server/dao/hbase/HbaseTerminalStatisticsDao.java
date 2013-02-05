package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.TERMINAL_STATISTICS;
import static com.profiler.common.hbase.HBaseTables.TERMINAL_STATISTICS_CF_COUNTER;
import static com.profiler.common.hbase.HBaseTables.TERMINAL_STATISTICS_CF_ERROR_COUNTER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.TerminalSpanUtils;
import com.profiler.common.util.TimeSlot;
import com.profiler.server.dao.TerminalStatisticsDao;

/**
 * @author netspider
 */
public class HbaseTerminalStatisticsDao implements TerminalStatisticsDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Override
	public void update(String sourceApplicationName, String destApplicationName, short destServiceType, int elapsed, boolean isError) {
		if (logger.isDebugEnabled()) {
			logger.debug("[UpdatingTerminalStatistics] " + sourceApplicationName + " -> " + destApplicationName + " (" + ServiceType.parse(destServiceType) + ")");
		}

		byte[] columnName = TerminalSpanUtils.makeColumnName(destServiceType, destApplicationName, elapsed);

		// TODO collector시간으로 변경
		long rowTimeSlot = TimeSlot.getStatisticsRowSlot(System.currentTimeMillis());
		final byte[] rowKey = TerminalSpanUtils.makeRowKey(sourceApplicationName, rowTimeSlot);

		byte[] cf = (isError ? TERMINAL_STATISTICS_CF_ERROR_COUNTER : TERMINAL_STATISTICS_CF_COUNTER);
		
		hbaseTemplate.incrementColumnValue(TERMINAL_STATISTICS, rowKey, cf, columnName, 1L);
	}
}
