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

		Map<String, TransactionFlowStatistics> stat = new HashMap<String, TransactionFlowStatistics>();


		for (KeyValue kv : keyList) {


            final byte[] row = kv.getRow();
            String calleeApplicationName = ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(row);
			short calleeServiceType = ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(row);

            final byte[] qualifier = kv.getQualifier();
			String callerApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
			short callerServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);

			long requestCount = Bytes.toLong(kv.getValue());
			short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
			
			// TODO 이건 callerHost가 되어야 할 듯.
			String calleeHost = ApplicationMapStatisticsUtils.getHost(qualifier);
			boolean isError = histogramSlot == (short) -1;
			

            if (logger.isDebugEnabled()) {
			    logger.debug("    Fetched. {}[{}] -> {}[{}] ({})", callerApplicationName, ServiceType.findServiceType(callerServiceType), calleeApplicationName, ServiceType.findServiceType(calleeServiceType), requestCount);
            }

            final String id = callerApplicationName + callerServiceType + calleeApplicationName + calleeServiceType;
			if (stat.containsKey(id)) {
				TransactionFlowStatistics statistics = stat.get(id);
				statistics.addSample(calleeHost, calleeServiceType, (isError) ? (short) -1 : histogramSlot, requestCount);


			} else {
				TransactionFlowStatistics statistics = new TransactionFlowStatistics(callerApplicationName, callerServiceType, calleeApplicationName, calleeServiceType);
				statistics.addSample(calleeHost, calleeServiceType, (isError) ? (short) -1 : histogramSlot, requestCount);
				

				stat.put(id, statistics);
			}
		}

		return stat;
	}
}
