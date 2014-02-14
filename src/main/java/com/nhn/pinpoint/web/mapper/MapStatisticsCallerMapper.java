package com.nhn.pinpoint.web.mapper;

import java.util.*;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
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
public class MapStatisticsCallerMapper implements RowMapper<List<LinkStatistics>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<LinkStatistics> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
		final KeyValue[] keyList = result.raw();

		final List<LinkStatistics> stat = new ArrayList<LinkStatistics>(keyList.length + 10);


		for (KeyValue kv : keyList) {

            final byte[] row = kv.getRow();
            String calleeApplicationName = ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(row);
			short calleeServiceType = ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(row);
            Application calleeApplication = new Application(calleeApplicationName, calleeServiceType);

            final byte[] qualifier = kv.getQualifier();
			String callerApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
			short callerServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);
            Application callerApplication = new Application(callerApplicationName, callerServiceType);

			long requestCount = Bytes.toLong(kv.getValue());
			short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
			
			// TODO 이건 callerHost가 되어야 할 듯.
			String calleeHost = ApplicationMapStatisticsUtils.getHost(qualifier);
			boolean isError = histogramSlot == (short) -1;
			
            if (logger.isDebugEnabled()) {
			    logger.debug("    Fetched Caller. {} -> {} host:{} (slot:{}/{})", callerApplication, calleeApplication, calleeHost, histogramSlot, requestCount);
            }

            LinkStatistics statistics = new LinkStatistics(callerApplication, calleeApplication);
            statistics.addSample(calleeHost, calleeServiceType, (isError) ? (short) -1 : histogramSlot, requestCount);

            stat.add(statistics);
		}

		return stat;
	}
}
