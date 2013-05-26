package com.nhn.pinpoint.web.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;

import com.profiler.common.ServiceType;
import com.profiler.common.util.ApplicationMapStatisticsUtils;

/**
 * rowkey = caller col = callee
 * 
 * @author netspider
 * 
 */
public class ApplicationMapLinkStatisticsCalleeMapper implements RowMapper<Map<Long, Map<Short, Long>>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	final String calleeApplicationName;
	final ServiceType calleeServiceType;

	public ApplicationMapLinkStatisticsCalleeMapper(String calleeApplicationName, short calleeServiceType) {
		this.calleeApplicationName = calleeApplicationName;
		this.calleeServiceType = ServiceType.findServiceType(calleeServiceType);
	}
	
	@Override
	public Map<Long, Map<Short, Long>> mapRow(Result result, int rowNum) throws Exception {
		KeyValue[] keyList = result.raw();

		/**
		 * <pre>
		 * map {
		 *     key = timestamp
		 *     value = map {
		 *         key = histogram slot
		 *         value = count
		 *     }
		 * }
		 * </pre>
		 */
		Map<Long, Map<Short, Long>> stat = new HashMap<Long, Map<Short, Long>>();

		for (KeyValue kv : keyList) {
			byte[] qualifier = kv.getQualifier();

			String calleeApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
			short calleeServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);

			// dest가 같지 않으면 버림.
			if (this.calleeServiceType.isUnknown()) {
				if (!this.calleeApplicationName.equals(calleeApplicationName)) {
					System.out.println("\tFIND CALLEE SKIP,1, DIFFERENT DEST. fetched={" + calleeApplicationName + ", " + ServiceType.findServiceType(calleeServiceType) + "}, params={" + this.calleeApplicationName + ", " + this.calleeServiceType + "}");
					continue;
				}
			} else {
				if (!this.calleeApplicationName.equals(calleeApplicationName) || this.calleeServiceType.getCode() != calleeServiceType) {
					System.out.println("\tFIND CALLEE SKIP,2, DIFFERENT DEST. fetched={" + calleeApplicationName + ", " + ServiceType.findServiceType(calleeServiceType) + "}, params={" + this.calleeApplicationName + ", " + this.calleeServiceType + "}");
					continue;
				}
			}

			long timestamp = ApplicationMapStatisticsUtils.getTimestampFromRowKey(kv.getRow());
			short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
			long requestCount = Bytes.toLong(kv.getValue());

			if (stat.containsKey(timestamp)) {
				Map<Short, Long> map = stat.get(timestamp);
				long value = (map.containsKey(histogramSlot) ? map.get(histogramSlot) + requestCount : 0);
				map.put(histogramSlot, value);
				
				System.out.println("FETCHED : " + map);
			} else {
				Map<Short, Long> map = new TreeMap<Short, Long>();
				map.put(histogramSlot, requestCount);
				stat.put(timestamp, map);
				
				System.out.println("FETCHED : " + map);
			}
			
		}
		
		return stat;
	}
}
