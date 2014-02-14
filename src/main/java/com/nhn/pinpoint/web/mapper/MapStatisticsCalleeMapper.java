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
 * rowkey = caller col = callee
 * 
 * @author netspider
 * 
 */
@Component
public class MapStatisticsCalleeMapper implements RowMapper<List<LinkStatistics>> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<LinkStatistics> mapRow(Result result, int rowNum) throws Exception {
        if (result.isEmpty()) {
            return Collections.emptyList();
        }
		final KeyValue[] keyList = result.raw();

		// key is destApplicationName.
		final List<LinkStatistics> linkStatisticsList = new ArrayList<LinkStatistics>(keyList.length + 10);


		for (KeyValue kv : keyList) {

            final byte[] row = kv.getRow();
            Application caller = readCallerApplication(row);

            final byte[] qualifier = kv.getQualifier();
            Application callee = readCalleeApplication(qualifier);
			
			long requestCount = Bytes.toLong(kv.getValue());
			short histogramSlot = ApplicationMapStatisticsUtils.getHistogramSlotFromColumnName(qualifier);
			
			// TODO 이게 callerHost가 아니라 calleeHost가 되어야하지 않나 싶음.
			String calleeHost = ApplicationMapStatisticsUtils.getHost(qualifier);
			boolean isError = histogramSlot == (short) -1;

			if (logger.isDebugEnabled()) {
			    logger.debug("    Fetched Callee. {} -> {} ({}) calleeHost", caller, callee, requestCount, calleeHost);
            }
			
            LinkStatistics statistics = new LinkStatistics(caller, callee);
            statistics.addSample(calleeHost, callee.getServiceTypeCode(), (isError) ? (short) -1 : histogramSlot, requestCount);

			linkStatisticsList.add(statistics);
		}

		// statistics에 dest host정보 삽입.
//		for (Entry<String, LoadFactor> entry : stat.entrySet()) {
//			entry.getValue().addToHosts(callerAppHostMap.get(entry.getKey()));
//		}

		return linkStatisticsList;
	}

    private Application readCalleeApplication(byte[] qualifier) {
        String calleeApplicationName = ApplicationMapStatisticsUtils.getDestApplicationNameFromColumnName(qualifier);
        short calleeServiceType = ApplicationMapStatisticsUtils.getDestServiceTypeFromColumnName(qualifier);
        return new Application(calleeApplicationName, calleeServiceType);
    }

    private Application readCallerApplication(byte[] row) {
        String callerApplicationName = ApplicationMapStatisticsUtils.getApplicationNameFromRowKey(row);
        short callerServiceType = ApplicationMapStatisticsUtils.getApplicationTypeFromRowKey(row);
        return new Application(callerApplicationName, callerServiceType);
    }
}
