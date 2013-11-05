package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.HOST_APPLICATION_MAP;
import static com.nhn.pinpoint.common.hbase.HBaseTables.HOST_APPLICATION_MAP_CF_MAP;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.nhn.pinpoint.collector.dao.HostApplicationMapDao;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import com.nhn.pinpoint.common.util.TimeUtils;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author netspider
 * @author emeroad
 */
@Repository
public class HbaseHostApplicationMapDao implements HostApplicationMapDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTimeService;

	// FIXME 매핑정보 매번 저장하지 말고 30~50초 주기로 한 개만 저장되도록 변경.
	private final ConcurrentMap<CacheKey, AtomicLong> cache = new ConcurrentHashMap<CacheKey, AtomicLong>(1024, 0.75f, 32);


	@Override
	public void insert(String host, String applicationName, short serviceType) {
        final long statisticsRowSlot = getSlotTime();

        final CacheKey cacheKey = new CacheKey(host, applicationName, serviceType);
        final AtomicLong hitSlot = cache.get(cacheKey);
        if (hitSlot == null ) {
            final AtomicLong newTime = new AtomicLong(statisticsRowSlot);
            final AtomicLong oldTime = cache.putIfAbsent(cacheKey, newTime);
            if (oldTime == null) {
                // 자신이 새롭게 넣는 주체이다.
                insertHost(host, applicationName, serviceType, statisticsRowSlot);
            } else {
                // 이미 키가 존재한다.
                boolean update = updateTime(statisticsRowSlot, oldTime);
                if (update) {
                    insertHost(host, applicationName, serviceType, statisticsRowSlot);
                }
            }
        } else {
            // 이미 키가 존재할 경우 update한다.
            boolean update = updateTime(statisticsRowSlot, hitSlot);
            if (update) {
                insertHost(host, applicationName, serviceType, statisticsRowSlot);
            }
        }
	}

    private boolean updateTime(final long newTime, final AtomicLong oldTime) {
        final long oldLong = oldTime.get();
        if (newTime > oldLong) {
            return oldTime.compareAndSet(oldLong, newTime);
        }
        return false;
    }

    private long getSlotTime() {
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        return TimeSlot.getStatisticsRowSlot(acceptedTime);
    }

    private void insertHost(String host, String applicationName, short serviceType, long statisticsRowSlot) {
        logger.debug("Insert host-application map. host={}, applicationName={}, serviceType={}", host, applicationName, serviceType);

        byte[] rowKey = Bytes.toBytes(TimeUtils.reverseCurrentTimeMillis(statisticsRowSlot));
        byte[] columnName = Bytes.toBytes(host);

        byte[] applicationNameBytes = Bytes.toBytes(applicationName);
        byte[] offsetBytes = new byte[HBaseTables.APPLICATION_NAME_MAX_LEN - applicationNameBytes.length];
        byte[] serviceTypeBytes = Bytes.toBytes(serviceType);
        byte[] value = BytesUtils.concat(applicationNameBytes, offsetBytes, serviceTypeBytes);

        try {
            hbaseTemplate.put(HOST_APPLICATION_MAP, rowKey, HOST_APPLICATION_MAP_CF_MAP, columnName, value);
        } catch (Exception ex) {
            logger.info("retry one. Caused:{}", ex.getCause(), ex);
            hbaseTemplate.put(HOST_APPLICATION_MAP, rowKey, HOST_APPLICATION_MAP_CF_MAP, columnName, value);
        }
    }

    private static class CacheKey {
        private String host;
        private String applicationName;
        private short serviceType;

        public CacheKey(String host, String applicationName, short serviceType) {
            if (host == null) {
                throw new NullPointerException("host must not be null");
            }
            if (applicationName == null) {
                throw new NullPointerException("applicationName must not be null");
            }
            this.host = host;
            this.applicationName = applicationName;
            this.serviceType = serviceType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (serviceType != cacheKey.serviceType) return false;
            if (!applicationName.equals(cacheKey.applicationName)) return false;
            if (!host.equals(cacheKey.host)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = host.hashCode();
            result = 31 * result + applicationName.hashCode();
            result = 31 * result + (int) serviceType;
            return result;
        }
    }
}
