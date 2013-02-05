package com.nhn.hippo.web.mapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.hippo.web.vo.TerminalStatistics;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.TerminalSpanUtils;

/**
 *
 */
@Component
public class TerminalStatisticsMapper implements RowMapper<Map<String, TerminalStatistics>> {

	/**
	 * <pre>
	 * rowkey = applicationName + timeslot
	 * cf = Cnt, ErrCnt
	 * cn = ServiceType + Slot + ApplicationName
	 * </pre>
	 */
	@Override
	public Map<String, TerminalStatistics> mapRow(Result result, int rowNum) throws Exception {
		KeyValue[] keyList = result.raw();

		// key is applicationName.
		Map<String, TerminalStatistics> stat = new HashMap<String, TerminalStatistics>();

		for (KeyValue kv : keyList) {
			if (kv.getFamilyLength() != HBaseTables.TERMINAL_STATISTICS_CF_COUNTER.length && kv.getFamilyLength() != HBaseTables.TERMINAL_STATISTICS_CF_ERROR_COUNTER.length) {
				continue;
			}

			byte[] qualifier = kv.getQualifier();

			String from = TerminalSpanUtils.getApplicationNameFromRowKey(kv.getRow());
			String to = TerminalSpanUtils.getDestApplicationNameFromColumnName(qualifier);
			long requestCount = Bytes.toLong(kv.getValue());
			short toServiceType = TerminalSpanUtils.getDestServiceTypeFromColumnName(qualifier);
			short histogramSlot = TerminalSpanUtils.getHistogramSlotFromColumnName(qualifier);

			// TODO 문제의 소지가 있지만 길이만.. 일단 길이만 비교.
			boolean isError = kv.getFamilyLength() == HBaseTables.TERMINAL_STATISTICS_CF_ERROR_COUNTER.length;

			// 'to' is target application name
			if (stat.containsKey(to)) {
				TerminalStatistics statistics = stat.get(to);
				if (isError) {
					statistics.getHistogram().incrErrorCount(requestCount);
				} else {
					statistics.getHistogram().addSample(histogramSlot, requestCount);
				}
			} else {
				TerminalStatistics statistics = new TerminalStatistics(from, to, toServiceType);
				if (isError) {
					statistics.getHistogram().incrErrorCount(requestCount);
				} else {
					statistics.getHistogram().addSample(histogramSlot, requestCount);
				}
				stat.put(to, statistics);
			}
		}

		return stat;
	}
}
