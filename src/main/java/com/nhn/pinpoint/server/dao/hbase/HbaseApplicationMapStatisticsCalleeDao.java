package com.nhn.pinpoint.server.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER;

import com.nhn.pinpoint.server.util.AcceptedTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import com.nhn.pinpoint.server.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.server.util.AcceptedTimeService;

/**
 * 나를 호출한 application 통계 갱신
 * 
 * @author netspider
 */
public class HbaseApplicationMapStatisticsCalleeDao implements ApplicationMapStatisticsCalleeDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTimeService;

	@Override
	public void update(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError) {
		if (calleeApplicationName == null) {
			throw new IllegalArgumentException("calleeApplicationName is null.");
		}

		if (callerApplicationName == null) {
			throw new IllegalArgumentException("callerApplicationName is null.");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("[UpdatingApplicationMapStatisticsCallee] " + callerApplicationName + " (" + ServiceType.findServiceType(callerServiceType) + ")[" + calleeHost + "] -> " + calleeApplicationName + " (" + ServiceType.findServiceType(calleeServiceType) + ")");
		}

		// make row key. rowkey는 나.
		long acceptedTime = acceptedTimeService.getAcceptedTime();
		long rowTimeSlot = TimeSlot.getStatisticsRowSlot(acceptedTime);
		final byte[] rowKey = ApplicationMapStatisticsUtils.makeRowKey(calleeApplicationName, calleeServiceType, rowTimeSlot);

		// column name은 나를 호출한 app
		byte[] columnName = ApplicationMapStatisticsUtils.makeColumnName(callerServiceType, callerApplicationName, calleeHost, elapsed, isError);

		hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLEE, rowKey, APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER, columnName, 1L);
	}
}
