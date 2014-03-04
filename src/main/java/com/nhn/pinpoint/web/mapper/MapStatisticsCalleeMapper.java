package com.nhn.pinpoint.web.mapper;

import java.util.*;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.common.util.TimeUtils;
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
 * 
 * @author netspider
 * 
 */
@Component
public class MapStatisticsCalleeMapper implements RowMapper<Collection<LinkStatistics>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public Collection<LinkStatistics> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
        logger.debug("mapRow:{}", rowNum);

        final Buffer row = new FixedBuffer(result.getRow());
        final Application calleeApplication = readCalleeApplication(row);
        final long timestamp = TimeUtils.recoveryTimeMillis(row.readLong());


        final Map<LinkKey, LinkStatistics> linkStatisticsMap = new HashMap<LinkKey, LinkStatistics>();
		for (KeyValue kv : result.raw()) {

            final byte[] qualifier = kv.getQualifier();
            Application callerApplication = readCallerApplication(qualifier);

            long requestCount = Bytes.toLong(kv.getBuffer(), kv.getValueOffset());
            short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);

            String callerHost = ApplicationMapStatisticsUtils.getHost(qualifier);
            boolean isError = histogramSlot == (short) -1;

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched Callee. {} callerHost:{} -> {} (slot:{}/{}),  ", callerApplication, callerHost, calleeApplication, histogramSlot, requestCount);
            }

            LinkStatistics statistics = getLinkStatics(linkStatisticsMap, callerApplication, calleeApplication, timestamp);
            statistics.addCallData(callerApplication.getName(), callerApplication.getServiceTypeCode(), callerHost, calleeApplication.getServiceTypeCode(), (isError) ? (short) -1 : histogramSlot, requestCount);

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched Callee. statistics:{}", statistics);
            }
		}

        return linkStatisticsMap.values();
	}

    private LinkStatistics getLinkStatics(Map<LinkKey, LinkStatistics> linkStatisticsMap, Application callerApplication, Application calleeApplication, long timestamp) {
        final LinkKey key = new LinkKey(callerApplication, calleeApplication);
        LinkStatistics statistics = linkStatisticsMap.get(key);
        if (statistics == null) {
            statistics = new LinkStatistics(callerApplication, calleeApplication, timestamp);
            linkStatisticsMap.put(key, statistics);
        }
        return statistics;
    }

    private Application readCallerApplication(byte[] qualifier) {
        String callerApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
        short callerServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);
        return new Application(callerApplicationName, callerServiceType);
    }

    private Application readCalleeApplication(Buffer row) {
        String calleeApplicationName = row.read2PrefixedString();
        short calleeServiceType = row.readShort();
        return new Application(calleeApplicationName, calleeServiceType);
    }
}
