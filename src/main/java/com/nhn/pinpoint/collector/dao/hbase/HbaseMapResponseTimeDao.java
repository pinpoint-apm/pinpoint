package com.nhn.pinpoint.collector.dao.hbase;

import com.nhn.pinpoint.collector.dao.MapResponseTimeDao;
import com.nhn.pinpoint.collector.dao.hbase.statistics.*;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.collector.util.ConcurrentCounterMap;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import static com.nhn.pinpoint.common.hbase.HBaseTables.*;

/**
 * was의 응답시간 데이터를 저장한다.
 * 
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseMapResponseTimeDao implements MapResponseTimeDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTimeService;

    @Autowired
    @Qualifier("selfMerge")
    private RowKeyMerge rowKeyMerge;

	private final boolean useBulk;

    private final ConcurrentCounterMap<RowInfo> counter = new ConcurrentCounterMap<RowInfo>();

	public HbaseMapResponseTimeDao() {
        this(true);
	}

	public HbaseMapResponseTimeDao(boolean useBulk) {
		this.useBulk = useBulk;
	}

    @Override
    public void received(String applicationName, short applicationServiceType, String agentId, int elapsed, boolean isError) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

		if (logger.isDebugEnabled()) {
			logger.debug("[Received] {} ({})[{}]",
                    applicationName, ServiceType.findServiceType(applicationServiceType), agentId);
		}


        // make row key. rowkey는 나.
		final long acceptedTime = acceptedTimeService.getAcceptedTime();
		final long rowTimeSlot = TimeSlot.getStatisticsRowSlot(acceptedTime);
        final RowKey selfRowKey = new CallRowKey(applicationName, applicationServiceType, rowTimeSlot);

        final short slotNumber = ApplicationMapStatisticsUtils.getSlotNumber(applicationServiceType, elapsed, isError);
        final ColumnName selfColumnName = new ResponseColumnName(agentId, slotNumber);
		if (useBulk) {
            RowInfo rowInfo = new DefaultRowInfo(selfRowKey, selfColumnName);
            this.counter.increment(rowInfo, 1L);
		} else {
            final byte[] rowKey = selfRowKey.getRowKey();
            // column name은 나를 호출한 app
            byte[] columnName = selfColumnName.getColumnName();
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
        hbaseTemplate.incrementColumnValue(MAP_STATISTICS_SELF, rowKey, MAP_STATISTICS_SELF_CF_COUNTER, columnName, increment);
    }


	@Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException("useBulk is " + useBulk);
		}
        // 일단 rowkey and column 별로 업데이트 치게 함. rowkey 별로 묶어서 보내야 될듯.
        Map<RowInfo,ConcurrentCounterMap.LongAdder> remove = this.counter.remove();
        List<Increment> merge = rowKeyMerge.createBulkIncrement(remove);
        if (!merge.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("flush {} Increment:{}", this.getClass().getSimpleName(), merge.size());
            }
            hbaseTemplate.increment(MAP_STATISTICS_SELF, merge);
        }

	}
}
