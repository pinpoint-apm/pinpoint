package com.nhn.pinpoint.web.mapper;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Component;

import com.nhn.pinpoint.common.util.ApplicationStatisticsUtils;
import com.nhn.pinpoint.web.applicationmap.rawdata.ApplicationStatistics;

/**
 * 
 * @author netspider
 * 
 */
@Component
@Deprecated
public class ApplicationStatisticsMapper implements RowMapper<ApplicationStatistics> {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public ApplicationStatistics mapRow(Result result, int rowNum) throws Exception {
		KeyValue[] keyList = result.raw();

		ApplicationStatistics stat = null;

		for (KeyValue kv : keyList) {
			byte[] qualifier = kv.getQualifier();

			String applicationName = ApplicationStatisticsUtils.getApplicationNameFromRowKey(kv.getRow());
			short serviceType = ApplicationStatisticsUtils.getApplicationTypeFromRowKey(kv.getRow());

			long requestCount = Bytes.toLong(kv.getValue());
			short histogramSlot = ApplicationStatisticsUtils.getHistogramSlotFromColumnName(qualifier);

			if (logger.isDebugEnabled()) {
				logger.debug("applicationName=" + applicationName + ", serviceType=" + serviceType + ", requestCount=" + requestCount + ", histogramSlot=" + histogramSlot);
			}

			if (stat == null) {
				stat = new ApplicationStatistics(applicationName, serviceType);
				stat.makeId();
			}

			stat.addValue(histogramSlot, requestCount);
		}
		
		logger.debug("Fetched applicationStatistics. {}", stat);

		return stat;
	}
}
