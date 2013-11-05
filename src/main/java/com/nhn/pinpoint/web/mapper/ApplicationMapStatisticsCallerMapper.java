package com.nhn.pinpoint.web.mapper;

import java.util.*;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;

/**
 * 
 * @author netspider
 * 
 */
@Component
public class ApplicationMapStatisticsCallerMapper implements RowMapper<Map<String, TransactionFlowStatistics>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Map<String, TransactionFlowStatistics> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyMap();
        }
		KeyValue[] keyList = result.raw();

		// key of map is destApplicationName.
		Map<String, TransactionFlowStatistics> stat = new HashMap<String, TransactionFlowStatistics>();

		// key of map is destApplicationName
//		Map<String, Set<String>> calleeAppHostMap = new HashMap<String, Set<String>>();

		for (KeyValue kv : keyList) {
			byte[] qualifier = kv.getQualifier();

			String calleeApplicationName = ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(kv.getRow());
			short calleeServiceType = ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(kv.getRow());

			String callerApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
			short callerServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);

			long requestCount = Bytes.toLong(kv.getValue());
			short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
			
			// TODO 이건 callerHost가 되어야 할 듯.
			String calleeHost = ApplicationMapStatisticsUtils.getHost(qualifier);
			boolean isError = histogramSlot == (short) -1;
			
			String id = callerApplicationName + callerServiceType + calleeApplicationName + calleeServiceType;

			logger.debug("    Fetched. " + callerApplicationName + "[" + ServiceType.findServiceType(callerServiceType) + "] -> " + calleeApplicationName + "[" + ServiceType.findServiceType(calleeServiceType) + "] (" + requestCount + ")");
			
			// hostname은 일단 따로 보관.
//			if (calleeHost != null) {
//				if (calleeAppHostMap.containsKey(id)) {
//					calleeAppHostMap.get(id).add(calleeHost);
//				} else {
//					Set<String> set = new HashSet<String>();
//					set.add(calleeHost);
//					calleeAppHostMap.put(id, set);
//				}
//			}

//			System.out.println("--------------------------------------------");
//			System.out.println("CallerMapper");
//			System.out.println("callerApplicationName:" + callerApplicationName);
//			System.out.println("callerServiceType=" + ServiceType.findServiceType(callerServiceType));
//			System.out.println("calleeApplicationName=" + calleeApplicationName);
//			System.out.println("calleeServiceType=" + ServiceType.findServiceType(calleeServiceType));
//			System.out.println("calleeHost:" + calleeHost);
//			System.out.println("--------------------------------------------");
			
			if (stat.containsKey(id)) {
				TransactionFlowStatistics statistics = stat.get(id);
				statistics.addSample(calleeHost, calleeServiceType, (isError) ? (short) -1 : histogramSlot, requestCount);

//				if (isError) {
//					statistics.getHistogram().addSample((short) -1, requestCount);
//				} else {
//					statistics.getHistogram().addSample(histogramSlot, requestCount);
//				}
			} else {
				TransactionFlowStatistics statistics = new TransactionFlowStatistics(callerApplicationName, callerServiceType, calleeApplicationName, calleeServiceType);
				statistics.addSample(calleeHost, calleeServiceType, (isError) ? (short) -1 : histogramSlot, requestCount);
				
//				if (isError) {
//					statistics.getHistogram().addSample((short) -1, requestCount);
//				} else {
//					statistics.getHistogram().addSample(histogramSlot, requestCount);
//				}
				stat.put(id, statistics);
			}
		}

		// statistics에 dest host정보 삽입.
//		for (Entry<String, TransactionFlowStatistics> entry : stat.entrySet()) {
//			entry.getValue().addToHosts(calleeAppHostMap.get(entry.getKey()));
//		}

		return stat;
	}
}
