package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER;

import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCallerDao;
import com.nhn.pinpoint.collector.dao.hbase.statistics.*;
import com.nhn.pinpoint.collector.util.ConcurrentCounterMap;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 나를 호출한 application 통계 갱신
 * 
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseApplicationMapStatisticsCallerDao implements ApplicationMapStatisticsCallerDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTimeService;

    @Autowired
    private RowKeyMerge rowKeyMerge;

	private final boolean useBulk;

    private final ConcurrentCounterMap<RowInfo> counter = new ConcurrentCounterMap<RowInfo>();

	public HbaseApplicationMapStatisticsCallerDao() {
        this(true);
	}

	public HbaseApplicationMapStatisticsCallerDao(boolean useBulk) {
		this.useBulk = useBulk;
	}

    @Override
	public void update(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, String calleeHost, int elapsed, boolean isError) {
        if (callerApplicationName == null) {
            throw new NullPointerException("callerApplicationName must not be null");
        }
        if (calleeApplicationName == null) {
            throw new NullPointerException("calleeApplicationName must not be null");
        }

		if (logger.isDebugEnabled()) {
			logger.debug("[Caller] {} ({}) -> {} ({})[{}]",
                    callerApplicationName, ServiceType.findServiceType(callerServiceType),
                    calleeApplicationName, ServiceType.findServiceType(calleeServiceType), calleeHost);
		}

        // httpclient와 같은 경우는 endpoint가 없을수 있다.
        calleeHost = StringUtils.defaultString(calleeHost);

        // make row key. rowkey는 나.
		final long acceptedTime = acceptedTimeService.getAcceptedTime();
		final long rowTimeSlot = TimeSlot.getStatisticsRowSlot(acceptedTime);
        final RowKey callerRowKey = new CallRowKey(callerApplicationName, callerServiceType, rowTimeSlot);

        final short calleeSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(calleeServiceType, elapsed, isError);
        final ColumnName calleeColumnName = new CallColumnName(calleeServiceType, calleeApplicationName, calleeHost, calleeSlotNumber);
		if (useBulk) {
            RowInfo rowInfo = new DefaultRowInfo(callerRowKey, calleeColumnName);
            this.counter.increment(rowInfo, 1L);
		} else {
            final byte[] rowKey = callerRowKey.getRowKey();
            // column name은 나를 호출한 app
            byte[] columnName = calleeColumnName.getColumnName();
            increment(rowKey, columnName, 1L);
        }
	}

    private void increment(byte[] rowKey, byte[] columnName, long increment) {
        if (rowKey == null) {
            throw new NullPointerException("rowKey must not be null");
        }
        if (columnName == null) {
            throw new NullPointerException("columnName must not be null");
        }
        hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLEE, rowKey, APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER, columnName, increment);
    }


	@Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException();
		}
        // 일단 rowkey and column 별로 업데이트 치게 함. rowkey 별로 묶어서 보내야 될듯.
        Map<RowInfo,ConcurrentCounterMap.LongAdder> remove = this.counter.remove();
        List<Increment> merge = rowKeyMerge.createBulkIncrement(remove);
        if (merge.size() != 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("flush {} Increment:{}", this.getClass().getSimpleName(), merge.size());
            }
            hbaseTemplate.increment(APPLICATION_MAP_STATISTICS_CALLEE, merge);
        }

	}
}
