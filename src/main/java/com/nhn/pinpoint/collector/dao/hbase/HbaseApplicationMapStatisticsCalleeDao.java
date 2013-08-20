package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER;

import com.nhn.pinpoint.collector.util.ConcurrentCounterMap;
import org.apache.hadoop.hbase.client.Increment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.collector.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.FlushHandler;
import com.nhn.pinpoint.collector.dao.hbase.StatisticsCache.Value;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * 나를 호출한 application 통계 갱신
 * 
 * @author netspider
 */
@Repository
public class HbaseApplicationMapStatisticsCalleeDao implements ApplicationMapStatisticsCalleeDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTimeService;

	private final boolean useBulk;

    private final ConcurrentCounterMap<CalleeKey> counter = new ConcurrentCounterMap<CalleeKey>();

	public HbaseApplicationMapStatisticsCalleeDao() {
        this(true);
	}

	public HbaseApplicationMapStatisticsCalleeDao(boolean useBulk) {
		this.useBulk = useBulk;
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

		if (useBulk) {
            CalleeKey calleeKey = new CalleeKey(calleeApplicationName, calleeServiceType, rowTimeSlot, callerServiceType, callerApplicationName, calleeHost, elapsed, isError);
            this.counter.increment(calleeKey, 1L);
		} else {
            final byte[] rowKey = ApplicationMapStatisticsUtils.makeRowKey(calleeApplicationName, calleeServiceType, rowTimeSlot);
            // column name은 나를 호출한 app
            byte[] columnName = ApplicationMapStatisticsUtils.makeColumnName(callerServiceType, callerApplicationName, calleeHost, elapsed, isError);
            increment(rowKey, columnName, 1L);
        }
	}

    private void increment(byte[] rowKey, byte[] columnName, long increment) {
        hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLEE, rowKey, APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER, columnName, increment);
    }

    public class CalleeKey {
        private String calleeApplicationName;
        private short calleeServiceType;
        private long rowTimeSlot;

        private short callerServiceType;
        private String callerApplicationName;
        private String calleeHost;
        private int elapsed;
        private boolean isError;
        // 주의 hash 값 캐시는 equals/hashCode 생성시 넣으면 안됨.
        private int hashCode;

        public CalleeKey(String calleeApplicationName, short calleeServiceType, long rowTimeSlot, short callerServiceType, String callerApplicationName, String calleeHost, int elapsed, boolean error) {
            this.calleeApplicationName = calleeApplicationName;
            this.calleeServiceType = calleeServiceType;
            this.rowTimeSlot = rowTimeSlot;
            this.callerServiceType = callerServiceType;
            this.callerApplicationName = callerApplicationName;
            this.calleeHost = calleeHost;
            this.elapsed = elapsed;
            this.isError = error;
        }

        public byte[] getRowKey() {
            return ApplicationMapStatisticsUtils.makeRowKey(calleeApplicationName, calleeServiceType, rowTimeSlot);
        }

        public byte[] getColumnName() {
            return ApplicationMapStatisticsUtils.makeColumnName(callerServiceType, callerApplicationName, calleeHost, elapsed, isError);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CalleeKey calleeKey = (CalleeKey) o;

            if (calleeServiceType != calleeKey.calleeServiceType) return false;
            if (callerServiceType != calleeKey.callerServiceType) return false;
            if (elapsed != calleeKey.elapsed) return false;
            if (isError != calleeKey.isError) return false;
            if (rowTimeSlot != calleeKey.rowTimeSlot) return false;
            if (calleeApplicationName != null ? !calleeApplicationName.equals(calleeKey.calleeApplicationName) : calleeKey.calleeApplicationName != null)
                return false;
            if (calleeHost != null ? !calleeHost.equals(calleeKey.calleeHost) : calleeKey.calleeHost != null) return false;
            if (callerApplicationName != null ? !callerApplicationName.equals(calleeKey.callerApplicationName) : calleeKey.callerApplicationName != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            if (hashCode != 0 ) {
                return hashCode;
            }
            int result = calleeApplicationName != null ? calleeApplicationName.hashCode() : 0;
            result = 31 * result + (int) calleeServiceType;
            result = 31 * result + (int) (rowTimeSlot ^ (rowTimeSlot >>> 32));
            result = 31 * result + (int) callerServiceType;
            result = 31 * result + (callerApplicationName != null ? callerApplicationName.hashCode() : 0);
            result = 31 * result + (calleeHost != null ? calleeHost.hashCode() : 0);
            result = 31 * result + elapsed;
            result = 31 * result + (isError ? 1 : 0);
            hashCode = result;
            return result;
        }
    }

	@Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException();
		}
        logger.trace("flush CalleeKey"); ]
        // 일단 rowkey and column 별로 업데이트 치게 함. rowkey 별로 묶어서 보내야 될듯.
        Map<CalleeKey ,ConcurrentCounterMap.LongAdder> flush = this.counter.remove();
        for (Map.Entry<CalleeKey, ConcurrentCounterMap.LongAdder> entry : flush.entrySet()) {
            CalleeKey key = entry.getKey();
            byte[] rowKey = key.getRowKey();
            byte[] columnName = key.getColumnName();
            long increment = entry.getValue().get();
            increment(rowKey, columnName, increment);
        }
	}
}
