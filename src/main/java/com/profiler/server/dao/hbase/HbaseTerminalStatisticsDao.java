package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.TERMINAL_STATISTICS;
import static com.profiler.common.hbase.HBaseTables.TERMINAL_STATISTICS_CF_COUNTER;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.RowLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.bo.TerminalStatisticsBo;
import com.profiler.common.hbase.HBaseBatchJob;
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
	public void update(String sourceApplicationName, String destApplicationName, short destServiceType, final String agentId, final int elapsed) {
		if (logger.isDebugEnabled()) {
			logger.debug("[WritingTerminalStatistics] " + sourceApplicationName + " -> " + destApplicationName + " (" + ServiceType.parse(destServiceType) + ")");
		}

		final byte[] columnName = TerminalSpanUtils.makeColumnName(destServiceType, destApplicationName);

		// TODO collector시간으로 변경??
		long timeSlot = TimeSlot.getSlot(System.currentTimeMillis());
		final byte[] rowKey = TerminalSpanUtils.makeRowKey(sourceApplicationName, timeSlot);

		hbaseTemplate.doUserBatchJob(TERMINAL_STATISTICS, new HBaseBatchJob() {
			@Override
			public void doBatch(HTable htable) {
				RowLock lock = null;
				try {
					// Locking the row
					lock = htable.lockRow(rowKey);

					if (logger.isDebugEnabled()) {
						logger.debug("The row {} is locked. lock={}", Arrays.toString(rowKey), lock.getLockId());
					}

					// Get existing value
					Get get = new Get(rowKey, lock);
					get.addColumn(TERMINAL_STATISTICS_CF_COUNTER, columnName);
					Result result = htable.get(get);
					KeyValue[] raw = result.raw();

					TerminalStatisticsBo statistics = null;
					if (raw != null && raw.length > 0) {
						logger.debug("raw len = {}", raw.length);
						statistics = TerminalStatisticsBo.parse(raw[0].getValue());
						statistics.addAgentId(agentId);
						statistics.sampleElapsedTime(elapsed);
					} else {
						logger.debug("value is not exists. create new one.");
						statistics = new TerminalStatisticsBo();
						statistics.addAgentId(agentId);
						statistics.sampleElapsedTime(elapsed);
					}
					
					logger.debug("Statistics = {}", statistics.toString());

					Put put = new Put(rowKey, lock);
					put.add(TERMINAL_STATISTICS_CF_COUNTER, columnName, statistics.toBytes());

					htable.put(put);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				} finally {
					if (lock != null) {
						try {
							htable.unlockRow(lock);

							if (logger.isDebugEnabled()) {
								logger.debug("Row lock {} released. lock={}", Arrays.toString(rowKey), lock.getLockId());
							}
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
						}
					}
				}
			}
		});
	}
}
