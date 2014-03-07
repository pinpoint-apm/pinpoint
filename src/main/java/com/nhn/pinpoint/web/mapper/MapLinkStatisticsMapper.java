package com.nhn.pinpoint.web.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.web.vo.Application;
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
public class MapLinkStatisticsMapper implements RowMapper<Map<Long, Map<Short, Long>>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkFilter filter;

	public MapLinkStatisticsMapper(Application callerApplication, Application calleeApplication) {
        if (callerApplication == null) {
            throw new NullPointerException("callerApplication must not be null");
        }
        if (calleeApplication == null) {
            throw new NullPointerException("calleeApplication must not be null");
        }
        filter = new DefaultLinkFilter(callerApplication, calleeApplication);
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

        final long timestamp = ApplicationMapStatisticsUtils.getTimestampFromRowKey(result.getRow());
		for (KeyValue kv : keyList) {
            final byte[] family = kv.getFamily();
            if (Bytes.equals(family, HBaseTables.MAP_STATISTICS_CALLEE_CF_COUNTER)) {
                final byte[] qualifier = kv.getQualifier();
                String foundApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
                short foundServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);

                if (filter.filter(foundApplicationName, foundServiceType)) {
                    continue;
                }

                short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
                long requestCount = getValueToLong(kv);

                if (logger.isDebugEnabled()) {
                    logger.debug("Fetched statistics. timestamp={}, histogramSlot={}, requestCount={}", timestamp, histogramSlot, requestCount);
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
            } else if (Bytes.equals(family, HBaseTables.MAP_STATISTICS_CALLEE_CF_VER2_COUNTER)) {
                final byte[] qualifier = kv.getQualifier();
                final Buffer buffer = new FixedBuffer(qualifier);

                short foundServiceType = buffer.readShort();
                String foundApplicationName = buffer.readPrefixedString();

                if (filter.filter(foundApplicationName, foundServiceType)) {
                    continue;
                }
                String skipCalleeHost = buffer.readPrefixedString();
                short histogramSlot = buffer.readShort();
                String skipCallerAgentId = buffer.readPrefixedString();

                long requestCount = getValueToLong(kv);

                if (logger.isDebugEnabled()) {
                    logger.debug("Fetched statistics. timestamp={}, histogramSlot={}, requestCount={}", timestamp, histogramSlot, requestCount);
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
		}

		return resultStat;
	}

    private long getValueToLong(KeyValue kv) {
        return Bytes.toLong(kv.getBuffer(), kv.getValueOffset());
    }
}
