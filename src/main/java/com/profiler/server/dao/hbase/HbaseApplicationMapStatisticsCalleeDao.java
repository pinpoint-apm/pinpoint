package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE;
import static com.profiler.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.ServiceType;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.ApplicationMapStatisticsUtils;
import com.profiler.common.util.TimeSlot;
import com.profiler.common.util.TimeUtils;
import com.profiler.server.dao.ApplicationMapStatisticsCalleeDao;
import com.profiler.server.util.AcceptedTimeService;

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
			logger.debug("[UpdatingApplicationMapStatisticsCallee] " + callerApplicationName + " (" + ServiceType.findServiceType(callerServiceType) + ") -> " + calleeApplicationName + " (" + ServiceType.findServiceType(calleeServiceType) + ")");
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
