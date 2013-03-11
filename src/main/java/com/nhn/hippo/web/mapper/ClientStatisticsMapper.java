package com.nhn.hippo.web.mapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.hippo.web.vo.ClientStatistics;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.util.ClientStatUtils;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class ClientStatisticsMapper implements RowMapper<Map<String, ClientStatistics>> {

	/**
	 * <pre>
	 * rowkey = applicationName + serviceType + timeslot
	 * cf = Count
	 * cq = Slot
	 * </pre>
	 */
	@Override
	public Map<String, ClientStatistics> mapRow(Result result, int rowNum) throws Exception {
		KeyValue[] keyList = result.raw();

		// key is destApplicationName.
		Map<String, ClientStatistics> stat = new HashMap<String, ClientStatistics>();

		for (KeyValue kv : keyList) {
			if (kv.getFamilyLength() != HBaseTables.CLIENT_STATISTICS_CF_COUNTER.length) {
				continue;
			}

			byte[] qualifier = kv.getQualifier();

			String destApplicationName = ClientStatUtils.getApplicationNameFromRowKey(kv.getRow());
			short destServiceType = ClientStatUtils.getApplicationServiceTypeFromRowKey(kv.getRow());
			long requestCount = Bytes.toLong(kv.getValue());
			short histogramSlot = ClientStatUtils.getHistogramSlotFromColumnName(qualifier);
			boolean isError = histogramSlot == (short) -1;

			if (stat.containsKey(destApplicationName)) {
				ClientStatistics statistics = stat.get(destApplicationName);
				if (isError) {
					statistics.getHistogram().incrErrorCount(requestCount);
				} else {
					statistics.getHistogram().addSample(histogramSlot, requestCount);
				}
			} else {
				ClientStatistics statistics = new ClientStatistics(destApplicationName, destServiceType);
				if (isError) {
					statistics.getHistogram().incrErrorCount(requestCount);
				} else {
					statistics.getHistogram().addSample(histogramSlot, requestCount);
				}
				stat.put(destApplicationName, statistics);
			}
		}

		return stat;
	}
}
