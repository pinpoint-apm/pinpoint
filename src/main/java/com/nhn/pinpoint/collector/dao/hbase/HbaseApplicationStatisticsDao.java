package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLER;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_STATISTICS;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_STATISTICS_CF_COUNTER;

import com.nhn.pinpoint.collector.dao.hbase.statistics.*;
import com.nhn.pinpoint.collector.util.ConcurrentCounterMap;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.ApplicationStatisticsDao;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.FlushHandler;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.Value;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * appllication 통계
 * 
 * @author netspider
 */
@Repository
public class HbaseApplicationStatisticsDao implements ApplicationStatisticsDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTimeService;

    @Autowired
    private RowKeyMerge rowKeyMerge;

	private final boolean useBulk;
    private final ConcurrentCounterMap<RowInfo> counter = new ConcurrentCounterMap<RowInfo>();



	public HbaseApplicationStatisticsDao() {
		this(true);
	}

	public HbaseApplicationStatisticsDao(boolean useBulk) {
		this.useBulk = useBulk;
	}


    @Override
	public void update(String applicationName, short serviceType, String agentId, int elapsed, boolean isError) {
		if (applicationName == null) {
			throw new IllegalArgumentException("applicationName is null.");
		}

		if (logger.isDebugEnabled()) {
			logger.debug("[UpdatingApplicationMapStatistics] " + applicationName + ", " + ServiceType.findServiceType(serviceType) + ", " + agentId + ", " + elapsed + ", " + isError);
		}

		// make row key. rowkey는 나.
		long acceptedTime = acceptedTimeService.getAcceptedTime();
        long rowTimeSlot = TimeSlot.getStatisticsRowSlot(acceptedTime);

        RowKey statisticsRowKey = new StatisticsRowKey(applicationName, serviceType, rowTimeSlot);

        short columnSlotNumber = ApplicationStatisticsUtils.getSlotNumber(serviceType, elapsed, isError);
        ColumnName statisticsColumnName = new StatisticsColumnName(agentId, columnSlotNumber);

        if (useBulk) {
            RowInfo statisticsKey = new DefaultRowInfo(statisticsRowKey, statisticsColumnName);
            counter.increment(statisticsKey, 1L);
        } else {
            final byte[] rowKey = statisticsRowKey.getRowKey();
            byte[] columnName = statisticsColumnName.getColumnName();
            increment(rowKey, columnName, 1L);
        }
	}

    private void increment(byte[] rowKey, byte[] columnName, long increment) {
        hbaseTemplate.incrementColumnValue(APPLICATION_STATISTICS, rowKey, APPLICATION_STATISTICS_CF_COUNTER, columnName, increment);
    }



	@Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException();
		}

        Map<RowInfo, ConcurrentCounterMap.LongAdder> remove = this.counter.remove();
        List<Increment> merge = rowKeyMerge.createBulkIncrement(remove);
        if (merge.size() != 0) {
            logger.debug("flush {} Increment:{}", this.getClass().getSimpleName(), merge.size());
        }
        for (Increment increment: merge) {
            // increment는 비동기 연산이 아니라 그냥 루프 돌려야 됨.
            hbaseTemplate.increment(APPLICATION_STATISTICS, increment);
        }
	}
}
