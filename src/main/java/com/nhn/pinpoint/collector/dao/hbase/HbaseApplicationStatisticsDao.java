package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_STATISTICS;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_STATISTICS_CF_COUNTER;

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

	private final boolean useBulk;
    private final ConcurrentCounterMap<StatisticsKey> counter = new ConcurrentCounterMap<StatisticsKey>();

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


		if (useBulk) {
            StatisticsKey statisticsKey = new StatisticsKey(applicationName, serviceType, rowTimeSlot, serviceType, agentId, elapsed, isError);
            counter.increment(statisticsKey, 1L);
        } else {

            final byte[] rowKey = ApplicationStatisticsUtils.makeRowKey(applicationName, serviceType, rowTimeSlot);
            byte[] columnName = ApplicationStatisticsUtils.makeColumnName(serviceType, agentId, elapsed, isError);
            increment(rowKey, columnName, 1L);
        }
	}

    private void increment(byte[] rowKey, byte[] columnName, long increment) {
        hbaseTemplate.incrementColumnValue(APPLICATION_STATISTICS, rowKey, APPLICATION_STATISTICS_CF_COUNTER, columnName, increment);
    }

    public class StatisticsKey {
        private String applicationName;
        private short applicationType;
        private long rowTimeSlot;
        private short serviceType;
        private String agentId;
        private int elapsed;
        private boolean isError;

        public StatisticsKey(String applicationName, short applicationType, long rowTimeSlot, short serviceType, String agentId, int elapsed, boolean error) {
            this.applicationName = applicationName;
            this.applicationType = applicationType;
            this.rowTimeSlot = rowTimeSlot;
            this.serviceType = serviceType;
            this.agentId = agentId;
            this.elapsed = elapsed;
            isError = error;
        }
        public byte[] getRowKey() {
            return ApplicationStatisticsUtils.makeRowKey(applicationName, applicationType, rowTimeSlot);
        }

        public byte[] getColumnName() {
            return ApplicationStatisticsUtils.makeColumnName(serviceType, agentId, elapsed, isError);
        }
    }

	@Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException();
		}

        logger.trace("flush StatisticsKey");
        Map<StatisticsKey ,ConcurrentCounterMap.LongAdder> flush = this.counter.remove();
        for (Map.Entry<StatisticsKey, ConcurrentCounterMap.LongAdder> entry : flush.entrySet()) {
            StatisticsKey key = entry.getKey();
            byte[] rowKey = key.getRowKey();
            byte[] columnName = key.getColumnName();
            long increment = entry.getValue().get();
            increment(rowKey, columnName, increment);
        }
	}
}
