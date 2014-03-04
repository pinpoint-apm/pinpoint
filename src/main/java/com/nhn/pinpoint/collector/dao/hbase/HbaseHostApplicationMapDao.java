package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.HOST_APPLICATION_MAP;
import static com.nhn.pinpoint.common.hbase.HBaseTables.HOST_APPLICATION_MAP_CF_MAP;

import com.nhn.pinpoint.collector.dao.HostApplicationMapDao;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.collector.util.AtomicLongUpdateMap;
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
    private final AtomicLongUpdateMap<CacheKey> updater = new AtomicLongUpdateMap<CacheKey>();


	@Override
	public void insert(String host, String applicationName, short serviceType) {
        if (host == null) {
            throw new NullPointerException("host must not be null");
        }
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }

        final long statisticsRowSlot = getSlotTime();

        final CacheKey cacheKey = new CacheKey(host, applicationName, serviceType);
        final boolean update = updater.update(cacheKey, statisticsRowSlot);
        if (update) {
            insertHost(host, applicationName, serviceType, statisticsRowSlot);
        }
	}


    private long getSlotTime() {
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        return TimeSlot.getStatisticsRowSlot(acceptedTime);
    }

    private void insertHost(String host, String applicationName, short serviceType, long statisticsRowSlot) {
        logger.debug("Insert host-application map. host={}, applicationName={}, serviceType={}", host, applicationName, serviceType);

        byte[] rowKey = Bytes.toBytes(TimeUtils.reverseTimeMillis(statisticsRowSlot));
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

    private static final class CacheKey {
        private final String host;
        private final String applicationName;
        private final short serviceType;

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
