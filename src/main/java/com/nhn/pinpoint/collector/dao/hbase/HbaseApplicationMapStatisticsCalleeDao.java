package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.Value;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;

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

	private final boolean useBulk;
	private final StatisticsCache cache;

	public HbaseApplicationMapStatisticsCalleeDao() {
		this.useBulk = false;
		this.cache = null;
	}

	public HbaseApplicationMapStatisticsCalleeDao(boolean useBulk) {
		this.useBulk = useBulk;
		this.cache = (useBulk) ? new StatisticsCache() : null;
	}

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

		if (useBulk) {
			cache.add(rowKey, columnName, 1L);
		} else {
			hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLEE, rowKey, APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER, columnName, 1L);
		}
	}

	@Override
	public void flush() {
		if (!useBulk) {
			throw new IllegalStateException();
		}

		List<Value> itemList = cache.getItems();
		for (Value item : itemList) {
			hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLEE, item.getRowKey(), APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER, item.getColumnName(), item.getValue());
		}
	}

	@Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException();
		}

		List<Value> itemList1 = cache.getAllItems();
		for (Value item : itemList1) {
			hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLEE, item.getRowKey(), APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER, item.getColumnName(), item.getValue());
		}
		
		List<Value> itemList2 = cache.getAllItems();
		for (Value item : itemList2) {
			hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLEE, item.getRowKey(), APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER, item.getColumnName(), item.getValue());
		}
	}
}
