package com.profiler.server.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLER;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import com.profiler.server.dao.ApplicationMapStatisticsCallerDao;
import com.profiler.server.util.AcceptedTimeService;

/**
 * 내가 호출한 appllication 통계 갱신
 * 
 * @author netspider
 */
public class HbaseApplicationMapStatisticsCallerDao implements ApplicationMapStatisticsCallerDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTimeService;

	@Override
	public void update(String calleeApplicationName, short calleeServiceType, String callerApplicationName, short callerServiceType, String callerHost, int elapsed, boolean isError) {
		if (calleeApplicationName == null) {
			throw new IllegalArgumentException("calleeApplicationName is null.");
		}

		if (callerApplicationName == null) {
			throw new IllegalArgumentException("callerApplicationName is null.");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("[UpdatingApplicationMapStatisticsCaller] " + callerApplicationName + " (" + ServiceType.findServiceType(callerServiceType) + ")[" + callerHost + "] -> " + calleeApplicationName + " (" + ServiceType.findServiceType(calleeServiceType) + ")");
		}

		if (callerHost == null) {
			// httpclient와 같은 경우는 endpoint가 없을수 있다.
			callerHost = "";
		}

		// make row key. rowkey는 나.
		long acceptedTime = acceptedTimeService.getAcceptedTime();
		long rowTimeSlot = TimeSlot.getStatisticsRowSlot(acceptedTime);
		final byte[] rowKey = ApplicationMapStatisticsUtils.makeRowKey(callerApplicationName, callerServiceType, rowTimeSlot);

		// 컬럼 이름은 내가 호출한 app.
		byte[] columnName = ApplicationMapStatisticsUtils.makeColumnName(calleeServiceType, calleeApplicationName, callerHost, elapsed, isError);

		hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLER, rowKey, APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER, columnName, 1L);
	}
}
