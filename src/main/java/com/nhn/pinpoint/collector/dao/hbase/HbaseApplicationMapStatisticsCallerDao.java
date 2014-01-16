package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLER;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER;

import com.nhn.pinpoint.collector.dao.hbase.statistics.*;
import com.nhn.pinpoint.collector.util.ConcurrentCounterMap;
import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCallerDao;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 내가 호출한 appllication 통계 갱신
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
	public void update(String calleeApplicationName, short calleeServiceType, String callerApplicationName, short callerServiceType, String callerHost, int elapsed, boolean isError) {
        if (calleeApplicationName == null) {
            throw new NullPointerException("calleeApplicationName must not be null");
        }
        if (callerApplicationName == null) {
            throw new NullPointerException("callerApplicationName must not be null");
        }

		if (logger.isDebugEnabled()) {
            logger.debug("[UpdatingApplicationMapStatisticsCaller] {} ({})[{}] <- {} ({})",
                    callerApplicationName, ServiceType.findServiceType(callerServiceType), callerHost,
                    calleeApplicationName, ServiceType.findServiceType(calleeServiceType));
		}

		if (callerHost == null) {
			// httpclient와 같은 경우는 endpoint가 없을수 있다.
			callerHost = "";
		}

		// make row key. rowkey는 나.
		long acceptedTime = acceptedTimeService.getAcceptedTime();
		long rowTimeSlot = TimeSlot.getStatisticsRowSlot(acceptedTime);
        RowKey callerRowKey = new CallRowKey(callerApplicationName, callerServiceType, rowTimeSlot);

        short calleeSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(calleeServiceType, elapsed, isError);
        ColumnName calleeColumnName = new CallColumnName(calleeServiceType, calleeApplicationName, callerHost, calleeSlotNumber);

		if (useBulk) {
            RowInfo rowInfo = new DefaultRowInfo(callerRowKey, calleeColumnName);
            counter.increment(rowInfo, 1L);
		} else {
            final byte[] rowKey = callerRowKey.getRowKey();
            // 컬럼 이름은 내가 호출한 app.
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
        hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLER, rowKey, APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER, columnName, increment);
    }

    @Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException();
		}

        Map<RowInfo, ConcurrentCounterMap.LongAdder> remove = this.counter.remove();
        List<Increment> merge = rowKeyMerge.createBulkIncrement(remove);
        if (merge.size() != 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("flush {} Increment:{}", this.getClass().getSimpleName(), merge.size());
            }
            hbaseTemplate.increment(APPLICATION_MAP_STATISTICS_CALLER, merge);
        }

    }
}
