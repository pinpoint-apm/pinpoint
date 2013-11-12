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
 * rowkey = caller col = callee
 * 
 * @author netspider
 * 
 */
@Component
public class ApplicationMapStatisticsCalleeMapper implements RowMapper<Map<String, TransactionFlowStatistics>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Map<String, TransactionFlowStatistics> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyMap();
        }
		KeyValue[] keyList = result.raw();

		// key is destApplicationName.
		Map<String, TransactionFlowStatistics> stat = new HashMap<String, TransactionFlowStatistics>();

		// key is destApplicationName
//		Map<String, Set<String>> callerAppHostMap = new HashMap<String, Set<String>>();

		for (KeyValue kv : keyList) {

            final byte[] row = kv.getRow();
            String callerApplicationName = ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(row);
			short callerServiceType = ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(row);

            final byte[] qualifier = kv.getQualifier();
			String calleeApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
			short calleeServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);
			
			long requestCount = Bytes.toLong(kv.getValue());
			short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
			
			// TODO 이게 callerHost가 아니라 calleeHost가 되어야하지 않나 싶음.
			String calleeHost = ApplicationMapStatisticsUtils.getHost(qualifier);
			boolean isError = histogramSlot == (short) -1;

			String id = callerApplicationName + callerServiceType + calleeApplicationName + calleeServiceType;
			if (logger.isDebugEnabled()) {
			    logger.debug("    Fetched. {}[{}] -> {}[{}] ({})", callerApplicationName, ServiceType.findServiceType(callerServiceType), calleeApplicationName, ServiceType.findServiceType(calleeServiceType), requestCount);
            }
			
			// hostname은 일단 따로 보관.
//			if (callerHost != null) {
//				if (callerAppHostMap.containsKey(id)) {
//					callerAppHostMap.get(id).add(callerHost);
//				} else {
//					Set<String> set = new HashSet<String>();
//					set.add(callerHost);
//					callerAppHostMap.put(id, set);
//				}
//			}

//			System.out.println("--------------------------------------------");
//			System.out.println("CalleeMapper");
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
//					// statistics.getHistogram().addSample((short) -1, requestCount);
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
//			entry.getValue().addToHosts(callerAppHostMap.get(entry.getKey()));
//		}

		return stat;
	}
}
