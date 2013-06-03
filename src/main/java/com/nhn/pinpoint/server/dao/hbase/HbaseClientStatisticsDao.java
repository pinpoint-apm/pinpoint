package com.nhn.pinpoint.server.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.CLIENT_STATISTICS;
import static com.nhn.pinpoint.common.hbase.HBaseTables.CLIENT_STATISTICS_CF_COUNTER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ClientStatUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import com.nhn.pinpoint.server.dao.ClientStatisticsDao;
import com.nhn.pinpoint.server.util.AcceptedTimeService;

/**
 * caller table로 통합됨.
 * 
 * @author netspider
 * 
 */
@Deprecated
public class HbaseClientStatisticsDao implements ClientStatisticsDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTImeService;

	@Override
	public void update(String destApplicationName, short destServiceType, int elapsed, boolean isError) {
		if (logger.isDebugEnabled()) {
			logger.debug("[UpdatingClientStatistics] applicationName=" + destApplicationName + " serviceType=" + ServiceType.findServiceType(destServiceType));
		}

		byte[] columnName = ClientStatUtils.makeColumnName(elapsed, isError);

		// make row key
		long acceptedTime = acceptedTImeService.getAcceptedTime();
		long rowTimeSlot = TimeSlot.getStatisticsRowSlot(acceptedTime);
		final byte[] rowKey = ClientStatUtils.makeRowKey(destApplicationName, destServiceType, rowTimeSlot);

		hbaseTemplate.incrementColumnValue(CLIENT_STATISTICS, rowKey, CLIENT_STATISTICS_CF_COUNTER, columnName, 1L);
	}
}
