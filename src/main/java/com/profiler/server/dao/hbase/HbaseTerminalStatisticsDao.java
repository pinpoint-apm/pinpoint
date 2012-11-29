package com.profiler.server.dao.hbase;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.TerminalSpanUtils;
import com.profiler.common.util.TimeSlot;
import com.profiler.server.dao.TerminalStatistics;

/**
 * 
 * @author netspider
 * 
 */
public class HbaseTerminalStatisticsDao implements TerminalStatistics {

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Override
	public void update(String sourceApplicationName, String destApplicationName, short destServiceType) {
		System.out.println("[WritingTerminalStatistics] " + sourceApplicationName + " -> " + destApplicationName + " (" + ServiceType.parse(destServiceType) + ")");

		byte[] columnName = TerminalSpanUtils.makeColumnName(destServiceType, destApplicationName);
		// TODO 시간을 재는 위치 변경
		long timeSlot = TimeSlot.getSlot(System.currentTimeMillis());
		final byte[] rowKey = TerminalSpanUtils.makeRowKey(sourceApplicationName, timeSlot);

		final Put put = new Put(rowKey);
		put.add(HBaseTables.TERMINAL_STATISTICS_CF_COUNTER, columnName, Bytes.toBytes(1));

		hbaseTemplate.put(HBaseTables.TERMINAL_STATISTICS, put);
		
		// TODO respose time histogram, agentId list가 추가되어야 함.
	}
}
