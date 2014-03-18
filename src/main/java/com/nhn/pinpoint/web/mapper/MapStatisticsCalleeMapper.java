package com.nhn.pinpoint.web.mapper;

import java.util.*;

import com.nhn.pinpoint.common.buffer.Buffer;
import com.nhn.pinpoint.common.buffer.FixedBuffer;
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
 * 
 * @author netspider
 * 
 */
@Component
public class MapStatisticsCalleeMapper implements RowMapper<LinkStatisticsData> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkFilter filter;

    public MapStatisticsCalleeMapper() {
        this(SkipLinkFilter.FILTER);
    }

    public MapStatisticsCalleeMapper(LinkFilter filter) {
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
        final Application calleeApplication = readCalleeApplication(row);
        final long timestamp = TimeUtils.recoveryTimeMillis(row.readLong());


        final LinkStatisticsData linkStatisticsData = new LinkStatisticsData();
		for (KeyValue kv : result.raw()) {

            final byte[] qualifier = kv.getQualifier();
            final Application callerApplication = readCallerApplication(qualifier);
            if (filter.filter(callerApplication)) {
                continue;
            }

            long requestCount = Bytes.toLong(kv.getBuffer(), kv.getValueOffset());
            short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);

            String callerHost = ApplicationMapStatisticsUtils.getHost(qualifier);
            boolean isError = histogramSlot == (short) -1;

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched Callee. {} callerHost:{} -> {} (slot:{}/{}),  ", callerApplication, callerHost, calleeApplication, histogramSlot, requestCount);
            }


            final short slotTime = (isError) ? (short) -1 : histogramSlot;
            linkStatisticsData.addCallData(callerApplication, callerApplication.getName(), calleeApplication, callerHost, timestamp, slotTime, requestCount);

            if (logger.isDebugEnabled()) {
                logger.debug("    Fetched Callee. statistics:{}", linkStatisticsData);
            }
		}

        return linkStatisticsData;
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
