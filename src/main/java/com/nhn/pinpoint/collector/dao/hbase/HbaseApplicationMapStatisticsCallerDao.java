package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLER;
import static com.nhn.pinpoint.common.hbase.HBaseTables.APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER;

import com.nhn.pinpoint.collector.util.ConcurrentCounterMap;
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
import org.springframework.stereotype.Repository;

import javax.management.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 내가 호출한 appllication 통계 갱신
 * 
 * @author netspider
 */
@Repository
public class HbaseApplicationMapStatisticsCallerDao implements ApplicationMapStatisticsCallerDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTimeService;

	private final boolean useBulk;

    private final ConcurrentCounterMap<CallerKey> counter = new ConcurrentCounterMap<CallerKey>();

	public HbaseApplicationMapStatisticsCallerDao() {
        this(true);
	}

	public HbaseApplicationMapStatisticsCallerDao(boolean useBulk) {
		this.useBulk = useBulk;
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

		if (useBulk) {
            CallerKey callerKey = new CallerKey(callerApplicationName, callerServiceType, rowTimeSlot, calleeServiceType, calleeApplicationName, callerHost, elapsed, isError);
            counter.increment(callerKey, 1L);
		} else {

            final byte[] rowKey = ApplicationMapStatisticsUtils.makeRowKey(callerApplicationName, callerServiceType, rowTimeSlot);
            // 컬럼 이름은 내가 호출한 app.
            byte[] columnName = ApplicationMapStatisticsUtils.makeColumnName(calleeServiceType, calleeApplicationName, callerHost, elapsed, isError);
            increment(rowKey, columnName, 1L);
        }
	}

    public class CallerRowKey {
        private String callerApplicationName;
        private short callerServiceType;
        private long rowTimeSlot;
        // 주의 hash 값 캐시는 equals/hashCode 생성시 넣으면 안됨.
        private int hash;

        public CallerRowKey(String callerApplicationName, short callerServiceType, long rowTimeSlot) {
            this.callerApplicationName = callerApplicationName;
            this.callerServiceType = callerServiceType;
            this.rowTimeSlot = rowTimeSlot;
        }
        public byte[] getRowKey() {
            return ApplicationMapStatisticsUtils.makeRowKey(callerApplicationName, callerServiceType, rowTimeSlot);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CallerRowKey that = (CallerRowKey) o;

            if (callerServiceType != that.callerServiceType) return false;
            if (rowTimeSlot != that.rowTimeSlot) return false;
            if (callerApplicationName != null ? !callerApplicationName.equals(that.callerApplicationName) : that.callerApplicationName != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            if (hash != 0) {
                return hash;
            }
            int result = callerApplicationName != null ? callerApplicationName.hashCode() : 0;
            result = 31 * result + (int) callerServiceType;
            result = 31 * result + (int) (rowTimeSlot ^ (rowTimeSlot >>> 32));
            hash = result;
            return result;
        }
    }

    public class CallerKey {
        private CallerRowKey callerRowKey;

        private short calleeServiceType;
        private String calleeApplicationName;
        private String callerHost;
        private int elapsed;
        private boolean isError;

        private long counter;


        public CallerKey(String callerApplicationName, short callerServiceType, long rowTimeSlot, short calleeServiceType, String calleeApplicationName, String callerHost, int elapsed, boolean error) {
            this.callerRowKey = new CallerRowKey(callerApplicationName, callerServiceType, rowTimeSlot);

            this.calleeServiceType = calleeServiceType;
            this.calleeApplicationName = calleeApplicationName;
            this.callerHost = callerHost;
            this.elapsed = elapsed;
            this.isError = error;
        }

        public CallerRowKey getCallerRowKey() {
            return callerRowKey;
        }

        public byte[] getColumnName() {
            return ApplicationMapStatisticsUtils.makeColumnName(calleeServiceType, calleeApplicationName, callerHost, elapsed, isError);
        }

        public long getCounter() {
            return counter;
        }

        public void setCounter(long counter) {
            this.counter = counter;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CallerKey callerKey = (CallerKey) o;

            if (calleeServiceType != callerKey.calleeServiceType) return false;
            if (elapsed != callerKey.elapsed) return false;
            if (isError != callerKey.isError) return false;
            if (calleeApplicationName != null ? !calleeApplicationName.equals(callerKey.calleeApplicationName) : callerKey.calleeApplicationName != null)
                return false;
            if (callerHost != null ? !callerHost.equals(callerKey.callerHost) : callerKey.callerHost != null) return false;
            if (callerRowKey != null ? !callerRowKey.equals(callerKey.callerRowKey) : callerKey.callerRowKey != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = callerRowKey != null ? callerRowKey.hashCode() : 0;
            result = 31 * result + (int) calleeServiceType;
            result = 31 * result + (calleeApplicationName != null ? calleeApplicationName.hashCode() : 0);
            result = 31 * result + (callerHost != null ? callerHost.hashCode() : 0);
            result = 31 * result + elapsed;
            result = 31 * result + (isError ? 1 : 0);
            return result;
        }
    }

    private void increment(byte[] rowKey, byte[] columnName, long increment) {
        hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLER, rowKey, APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER, columnName, increment);
    }

    @Override
	public void flushAll() {
		if (!useBulk) {
			throw new IllegalStateException();
		}

        logger.trace("flush CallerKey");

        // rowKey 기반으로 다시 merge한다.
        Map<CallerRowKey, List<CallerKey>> rowkeyMerge =  new HashMap<CallerRowKey, List<CallerKey>>();

        Map<CallerKey, ConcurrentCounterMap.LongAdder> flush = this.counter.remove();
        for (Map.Entry<CallerKey, ConcurrentCounterMap.LongAdder> entry : flush.entrySet()) {
            final CallerKey callerKey = entry.getKey();

            callerKey.setCounter(entry.getValue().get());
            // 흠 괜히 복잡한게 class로 빼야 될듯.
            List<CallerKey> callerKeyList = rowkeyMerge.get(callerKey.getCallerRowKey());
            if (callerKeyList == null) {
                ArrayList<CallerKey> list = new ArrayList<CallerKey>();
                list.add(callerKey);
                rowkeyMerge.put(callerKey.getCallerRowKey(), list);
            } else {
                callerKeyList.add(callerKey);
            }
//          cf별로 각각 flush
//            byte[] columnName = callerKey.getColumnName();
//            long increment = entry.getValue().get();
//            increment(rowKey, columnName, increment);
        }

        //합쳐서 flush 뭔가 로직이 복잡함.
        for (Map.Entry<CallerRowKey, List<CallerKey>> callerRowKeyListEntry : rowkeyMerge.entrySet()) {
            CallerRowKey key = callerRowKeyListEntry.getKey();
            Increment increment = new Increment(key.getRowKey());
            for(CallerKey callerKey : callerRowKeyListEntry.getValue()) {
                increment.addColumn(APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER, callerKey.getColumnName(), callerKey.getCounter());
            }
            logger.trace("flush CallerKey cf size:{}", callerRowKeyListEntry.getValue().size());
            hbaseTemplate.increment(APPLICATION_MAP_STATISTICS_CALLER, increment);
//            hbaseTemplate.incrementColumnValue(APPLICATION_MAP_STATISTICS_CALLER, rowKey, APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER, columnName, increment);
        }


    }
}
