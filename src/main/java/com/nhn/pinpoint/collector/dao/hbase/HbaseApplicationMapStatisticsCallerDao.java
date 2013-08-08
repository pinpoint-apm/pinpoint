package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLER;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER;

import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCallerDao;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.FlushHandler;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.Value;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;

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

	private final boolean useBulk;
	private final StatisticsCache cache;

	public HbaseApplicationMapStatisticsCallerDao() {
		this.useBulk = false;
		this.cache = null;
	}

	public HbaseApplicationMapStatisticsCallerDao(boolean useBulk) {
		this.useBulk = useBulk;
		this.cache = (useBulk) ? new StatisticsCache(new FlushHandler() {
			@Override
			public void handleValue(Value value) {
				hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLER, value.getRowKey(), APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER, value.getColumnName(), value.getLongValue());
			}

			@Override
			public void handleValue(Increment increment) {
				hbaseTemplate.increment(APPLICATION_MAP_STATISTICS_CALLER, increment);
			}
		}) : null;
	}

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

		if (useBulk) {
			cache.add(rowKey, columnName, 1L);
		} else {
			hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLER, rowKey, APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER, columnName, 1L);
		}
	}

	@Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException();
		}
		cache.flushAll();
	}
}
