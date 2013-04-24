package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.CLIENT_STATISTICS;
import static com.profiler.common.hbase.HBaseTables.CLIENT_STATISTICS_CF_COUNTER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.ClientStatUtils;
import com.profiler.common.util.TimeSlot;
import com.profiler.server.dao.ClientStatisticsDao;
import com.profiler.server.util.AcceptedTimeService;

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
