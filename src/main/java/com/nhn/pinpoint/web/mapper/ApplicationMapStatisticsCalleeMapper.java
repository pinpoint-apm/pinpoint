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

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.web.applicationmap.ApplicationStatistics;

/**
 * rowkey = caller col = callee
 * 
 * @author netspider
 * 
 */
@Component
public class ApplicationMapStatisticsCalleeMapper implements RowMapper<Map<String, ApplicationStatistics>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Map<String, ApplicationStatistics> mapRow(Result result, int rowNum) throws Exception {
		KeyValue[] keyList = result.raw();

		// key is destApplicationName.
		Map<String, ApplicationStatistics> stat = new HashMap<String, ApplicationStatistics>();

		// key is destApplicationName
		Map<String, Set<String>> callerAppHostMap = new HashMap<String, Set<String>>();

		for (KeyValue kv : keyList) {
			byte[] qualifier = kv.getQualifier();

			String callerApplicationName = ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(kv.getRow());
			short callerServiceType = ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(kv.getRow());

			String calleeApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
			short calleeServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);
			
			long requestCount = Bytes.toLong(kv.getValue());
			short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
			String callerHost = ApplicationMapStatisticsUtils.getHost(qualifier);
			boolean isError = histogramSlot == (short) -1;

			String id = callerApplicationName + callerServiceType + calleeApplicationName + calleeServiceType;
			
			logger.debug("    Fetched. " + callerApplicationName + "[" + ServiceType.findServiceType(callerServiceType) + "] -> " + calleeApplicationName + "[" + ServiceType.findServiceType(calleeServiceType) + "] (" + requestCount + ")");
			
			// hostname은 일단 따로 보관.
			if (callerHost != null) {
				if (callerAppHostMap.containsKey(id)) {
					callerAppHostMap.get(id).add(callerHost);
				} else {
					Set<String> set = new HashSet<String>();
					set.add(callerHost);
					callerAppHostMap.put(id, set);
				}
			}

			if (stat.containsKey(id)) {
				ApplicationStatistics statistics = stat.get(id);
				if (isError) {
					statistics.getHistogram().addSample((short) -1, requestCount);
				} else {
					statistics.getHistogram().addSample(histogramSlot, requestCount);
				}
			} else {
				ApplicationStatistics statistics = new ApplicationStatistics(callerApplicationName, callerServiceType, calleeApplicationName, calleeServiceType);
				if (isError) {
					statistics.getHistogram().addSample((short) -1, requestCount);
				} else {
					statistics.getHistogram().addSample(histogramSlot, requestCount);
				}
				stat.put(id, statistics);
			}
		}

		// statistics에 dest host정보 삽입.
		for (Entry<String, ApplicationStatistics> entry : stat.entrySet()) {
			entry.getValue().addToHosts(callerAppHostMap.get(entry.getKey()));
		}

		return stat;
	}
}
