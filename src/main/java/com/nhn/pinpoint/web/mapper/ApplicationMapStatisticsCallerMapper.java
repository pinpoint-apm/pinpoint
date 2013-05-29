package com.nhn.pinpoint.web.mapper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.web.applicationmap.ApplicationStatistics;
import com.profiler.common.util.ApplicationMapStatisticsUtils;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class ApplicationMapStatisticsCallerMapper implements RowMapper<Map<String, ApplicationStatistics>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Map<String, ApplicationStatistics> mapRow(Result result, int rowNum) throws Exception {
		KeyValue[] keyList = result.raw();

		// key of map is destApplicationName.
		Map<String, ApplicationStatistics> stat = new HashMap<String, ApplicationStatistics>();

		// key of map is destApplicationName
		Map<String, Set<String>> calleeAppHostMap = new HashMap<String, Set<String>>();

		for (KeyValue kv : keyList) {
			byte[] qualifier = kv.getQualifier();

			String calleeApplicationName = ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(kv.getRow());
			short calleeServiceType = ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(kv.getRow());

			String callerApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
			short callerServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);

			logger.debug("    Fetched. " + callerApplicationName + " -> " + calleeApplicationName);

			long requestCount = Bytes.toLong(kv.getValue());
			short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
			String calleeHost = ApplicationMapStatisticsUtils.getHost(qualifier);
			boolean isError = histogramSlot == (short) -1;
			
			String id = callerApplicationName + callerServiceType + calleeApplicationName + calleeServiceType;

			// hostname은 일단 따로 보관.
			if (calleeHost != null) {
				if (calleeAppHostMap.containsKey(id)) {
					calleeAppHostMap.get(id).add(calleeHost);
				} else {
					Set<String> set = new HashSet<String>();
					set.add(calleeHost);
					calleeAppHostMap.put(id, set);
				}
			}

			if (stat.containsKey(id)) {
				ApplicationStatistics statistics = stat.get(id);
				if (isError) {
					statistics.getHistogram().addSample((short) -1, 1L);
					// statistics.getHistogram().incrErrorCount(requestCount);
				} else {
					statistics.getHistogram().addSample(histogramSlot, requestCount);
				}
			} else {
				ApplicationStatistics statistics = new ApplicationStatistics(callerApplicationName, callerServiceType, calleeApplicationName, calleeServiceType);
				if (isError) {
					statistics.getHistogram().addSample((short) -1, 1L);
					// statistics.getHistogram().incrErrorCount(requestCount);
				} else {
					statistics.getHistogram().addSample(histogramSlot, requestCount);
				}
				stat.put(id, statistics);
			}
		}

		// statistics에 dest host정보 삽입.
		for (Entry<String, ApplicationStatistics> entry : stat.entrySet()) {
			entry.getValue().addToHosts(calleeAppHostMap.get(entry.getKey()));
		}

		return stat;
	}
}
