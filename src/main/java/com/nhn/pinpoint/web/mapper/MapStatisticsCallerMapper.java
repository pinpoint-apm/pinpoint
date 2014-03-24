package com.nhn.pinpoint.web.mapper;

import java.util.*;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
import com.nhn.pinpoint.common.buffer.OffsetFixedBuffer;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.util.TimeUtils;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatisticsData;
import com.nhn.pinpoint.web.vo.Application;
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
public class MapStatisticsCallerMapper implements RowMapper<LinkStatisticsData> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkFilter filter;

    public MapStatisticsCallerMapper() {
        this(SkipLinkFilter.FILTER);
    }

    public MapStatisticsCallerMapper(LinkFilter filter) {
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }
        this.filter = filter;
    }

    @Override
	public LinkStatisticsData mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return new LinkStatisticsData();
        }
        logger.debug("mapRow:{}", rowNum);

        final Buffer row = new FixedBuffer(result.getRow());
        final Application caller = readCallerApplication(row);
        final long timestamp = TimeUtils.recoveryTimeMillis(row.readLong());

		// key is destApplicationName.
        final LinkStatisticsData linkStatisticsMap = new LinkStatisticsData();
        for (KeyValue kv :  result.raw()) {
            final byte[] family = kv.getFamily();
            if (Bytes.equals(family, HBaseTables.MAP_STATISTICS_CALLEE_CF_COUNTER)) {
                final byte[] qualifier = kv.getQualifier();
                final Application callee = readCalleeApplication(qualifier);
                if (filter.filter(callee)) {
                    continue;
                }

                long requestCount = getValueToLong(kv);

                short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
                boolean isError = histogramSlot == (short) -1;

                String calleeHost = ApplicationMapStatisticsUtils.getHost(qualifier);

                if (logger.isDebugEnabled()) {
                    logger.debug("    Fetched Caller.  {} -> {} (slot:{}/{}) calleeHost:{}", caller, callee, histogramSlot, requestCount, calleeHost);
                }

                final short slotTime = (isError) ? (short) -1 : histogramSlot;
                linkStatisticsMap.addLinkData(caller, caller.getName(), callee, calleeHost, timestamp, slotTime, requestCount);


            } else if (Bytes.equals(family, HBaseTables.MAP_STATISTICS_CALLEE_CF_VER2_COUNTER)) {

                final Buffer buffer = new OffsetFixedBuffer(kv.getBuffer(), kv.getQualifierOffset());
                final Application callee = readCalleeApplication(buffer);
                if (filter.filter(callee)) {
                    continue;
                }

                String calleeHost = buffer.readPrefixedString();
                short histogramSlot = buffer.readShort();

                boolean isError = histogramSlot == (short) -1;

                String callerAgentId = buffer.readPrefixedString();

                long requestCount = getValueToLong(kv);
                if (logger.isDebugEnabled()) {
                    logger.debug("    Fetched Caller.(New) {} {} -> {} (slot:{}/{}) calleeHost:{}", caller, callerAgentId, callee, histogramSlot, requestCount, calleeHost);
                }

                final short slotTime = (isError) ? (short) -1 : histogramSlot;
                linkStatisticsMap.addLinkData(caller, callerAgentId, callee, calleeHost, timestamp, slotTime, requestCount);
            } else {
                throw new IllegalArgumentException("unknown ColumnFamily :" + Arrays.toString(family));
            }

		}

        return linkStatisticsMap;
	}

    private long getValueToLong(KeyValue kv) {
        return Bytes.toLong(kv.getBuffer(), kv.getValueOffset());
    }


    private Application readCalleeApplication(byte[] qualifier) {
        String calleeApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
        short calleeServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);
        return new Application(calleeApplicationName, calleeServiceType);
    }


    private Application readCalleeApplication(Buffer buffer) {
        short calleeServiceType = buffer.readShort();
        String calleeApplicationName = buffer.readPrefixedString();
        return new Application(calleeApplicationName, calleeServiceType);
    }

    private Application readCallerApplication(Buffer row) {
        String callerApplicationName = row.read2PrefixedString();
        short callerServiceType = row.readShort();
        return new Application(callerApplicationName, callerServiceType);
    }
}
