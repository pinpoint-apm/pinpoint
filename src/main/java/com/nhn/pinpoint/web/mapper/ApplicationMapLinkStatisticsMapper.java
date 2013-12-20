package com.nhn.pinpoint.web.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;

/**
 * rowkey = caller col = callee
 * 
 * @author netspider
 * 
 */
public class ApplicationMapLinkStatisticsMapper implements RowMapper<Map<Long, Map<Short, Long>>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	final String callerApplicationName;
	final ServiceType callerServiceType;
	final String calleeApplicationName;
	final ServiceType calleeServiceType;

	public ApplicationMapLinkStatisticsMapper(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType) {
		this.callerApplicationName = callerApplicationName;
		this.callerServiceType = ServiceType.findServiceType(callerServiceType);
		this.calleeApplicationName = calleeApplicationName;
		this.calleeServiceType = ServiceType.findServiceType(calleeServiceType);
	}

	private boolean dropRow(String foundApplicationName, short foundServiceType) {
		if (this.calleeServiceType.isWas() && this.callerServiceType.isWas()) {
			logger.debug("check was to was.");
			// src가 같지 않으면 버림.
			if (!this.callerApplicationName.equals(foundApplicationName) || this.callerServiceType.getCode() != foundServiceType) {
				if (logger.isDebugEnabled()) {
					logger.debug("  DROP THE ROW,1, DIFFERENT SRC. fetched={" + foundApplicationName + ", " + ServiceType.findServiceType(foundServiceType) + "}, params={" + this.calleeApplicationName + ", " + this.calleeServiceType + "}");
				}
				return true;
			}
		} else if (this.callerServiceType.isUser()) {
			logger.debug("check client to was");
			// dest가 해당 was가 아니면 버림.
			if (!this.calleeApplicationName.equals(foundApplicationName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("  DROP THE ROW,2, DIFFERENT DEST. fetched={" + foundApplicationName + ", " + ServiceType.findServiceType(foundServiceType) + "}, params={" + this.calleeApplicationName + ", " + this.calleeServiceType + "}");
				}
				return true;
			}
		} else {
			logger.debug("check any to any.");
			// dest가 같지 않으면 버림.
			if (this.calleeServiceType.isUnknown()) {
				// dest가 unknown인 경우 application name만 비교.
				// TODO 다른 좋은 비교 방법 없을까??
				if (!this.calleeApplicationName.equals(foundApplicationName)) {
					if (logger.isDebugEnabled()) {
						logger.debug("  DROP THE ROW,3, DIFFERENT DEST. fetched={" + foundApplicationName + ", " + ServiceType.findServiceType(foundServiceType) + "}, params={" + this.calleeApplicationName + ", " + this.calleeServiceType + "}");
					}
					return true;
				}
			} else {
				// dest가 unknown이 아니면 applicaiton name, type 둘 다 비교.
				if (!this.calleeApplicationName.equals(foundApplicationName) || this.calleeServiceType.getCode() != foundServiceType) {
					if (logger.isDebugEnabled()) {
						logger.debug("  DROP THE ROW,4, DIFFERENT DEST. fetched={" + foundApplicationName + ", " + ServiceType.findServiceType(foundServiceType) + "}, params={" + this.calleeApplicationName + ", " + this.calleeServiceType + "}");
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Map<Long, Map<Short, Long>> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyMap();
        }

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
		Map<Long, Map<Short, Long>> resultStat = new HashMap<Long, Map<Short, Long>>();

		for (KeyValue kv : keyList) {
			final byte[] qualifier = kv.getQualifier();
			String foundApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
			short foundServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);

			if (dropRow(foundApplicationName, foundServiceType)) {
				continue;
			}

			long timestamp = ApplicationMapStatisticsUtils.getTimestampFromRowKey(kv.getRow());
			short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
			long requestCount = Bytes.toLong(kv.getValue());

			if (logger.isDebugEnabled()) {
				logger.debug("Fetched statistics. timestamp=" + timestamp + ", histogramSlot=" + histogramSlot + ", requestCount=" + requestCount);
			}
			
			if (resultStat.containsKey(timestamp)) {
				Map<Short, Long> map = resultStat.get(timestamp);
				long value = (map.containsKey(histogramSlot) ? map.get(histogramSlot) + requestCount : requestCount);
				map.put(histogramSlot, value);
				resultStat.put(timestamp, map);
			} else {
				Map<Short, Long> map = new TreeMap<Short, Long>();
				map.put(histogramSlot, requestCount);
				resultStat.put(timestamp, map);
			}
		}

		return resultStat;
	}
}
