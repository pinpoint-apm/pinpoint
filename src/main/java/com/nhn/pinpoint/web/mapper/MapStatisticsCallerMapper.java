package com.nhn.pinpoint.web.mapper;

import java.util.*;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LinkKey;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;

/**
 * rowkey = caller col = callee
 * 
 * @author netspider
 * 
 */
@Component
public class MapStatisticsCallerMapper implements RowMapper<List<LinkStatistics>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<LinkStatistics> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        logger.debug("mapRow:{}", rowNum);
		final KeyValue[] keyList = result.raw();
        final byte[] rowKey = result.getRow();

        final long timestamp = ApplicationMapStatisticsUtils.getTimestampFromRowKey(rowKey);
        Application caller = readCallerApplication(rowKey);
		// key is destApplicationName.
        final Map<LinkKey, LinkStatistics> linkStatisticsMap = new HashMap<LinkKey, LinkStatistics>();
		for (KeyValue kv : keyList) {
            final byte[] family = kv.getFamily();
            if (Bytes.equals(family, HBaseTables.MAP_STATISTICS_CALLEE_CF_COUNTER)) {
                final byte[] qualifier = kv.getQualifier();
                Application callee = readCalleeApplication(qualifier);

                long requestCount = getValueToLong(kv);

                short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
                boolean isError = histogramSlot == (short) -1;

                String calleeHost = ApplicationMapStatisticsUtils.getHost(qualifier);

                if (logger.isDebugEnabled()) {
                    logger.debug("    Fetched Caller.  {} -> {} (slot:{}/{}) calleeHost:{}", caller, callee, histogramSlot, requestCount, calleeHost);
                }

                LinkStatistics statistics = getLinkStatistics(linkStatisticsMap, caller, callee, timestamp);
                statistics.addCallData(caller.getName(), caller.getServiceTypeCode(), calleeHost, callee.getServiceTypeCode(), (isError) ? (short) -1 : histogramSlot, requestCount);
            } else if (Bytes.equals(family, HBaseTables.MAP_STATISTICS_CALLEE_CF_VER2_COUNTER)) {

                final byte[] qualifier = kv.getQualifier();
                final Buffer buffer = new FixedBuffer(qualifier);
                Application callee = readCalleeApplication(buffer);

                String calleeHost = buffer.readPrefixedString();
                short histogramSlot = buffer.readShort();
                boolean isError = histogramSlot == (short) -1;

                String callerAgentId = buffer.readPrefixedString();

                long requestCount = getValueToLong(kv);
                if (logger.isDebugEnabled()) {
                    logger.debug("    Fetched Caller.(New) {} {} -> {} (slot:{}/{}) calleeHost:{}", caller, callerAgentId, callee, histogramSlot, requestCount, calleeHost);
                }

                LinkStatistics statistics = getLinkStatistics(linkStatisticsMap, caller, callee, timestamp);
                statistics.addCallData(callerAgentId, caller.getServiceTypeCode(), calleeHost, callee.getServiceTypeCode(), (isError) ? (short) -1 : histogramSlot, requestCount);
            } else {
                throw new IllegalArgumentException("unknown ColumnFamily :" + Arrays.toString(family));
            }

		}

        return new ArrayList<LinkStatistics>(linkStatisticsMap.values());
	}

    private long getValueToLong(KeyValue kv) {
        return Bytes.toLong(kv.getBuffer(), kv.getValueOffset());
    }

    private LinkStatistics getLinkStatistics(Map<LinkKey, LinkStatistics> linkStatisticsMap, Application caller, Application callee, long timestamp) {
        final LinkKey key = new LinkKey(caller, callee);
        LinkStatistics statistics = linkStatisticsMap.get(key);
        if (statistics == null) {
            statistics = new LinkStatistics(caller, callee, timestamp);
            linkStatisticsMap.put(key, statistics);
        }
        return statistics;
    }

    private Application readCalleeApplication(byte[] qualifier) {
        String calleeApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
        short calleeServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);
        return new Application(calleeApplicationName, calleeServiceType);
    }


    private Application readCalleeApplication(Buffer buffer) {
        short calleeServiceyType = buffer.readShort();
        String calleeApplicationName = buffer.readPrefixedString();
        return new Application(calleeApplicationName, calleeServiceyType);
    }

    private Application readCallerApplication(byte[] row) {
        String callerApplicationName = ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(row);
        short callerServiceType = ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(row);
        return new Application(callerApplicationName, callerServiceType);
    }
}
