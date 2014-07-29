package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.HOST_APPLICATION_MAP;
import static com.nhn.pinpoint.common.hbase.HBaseTables.HOST_APPLICATION_MAP_CF_MAP;

import com.nhn.pinpoint.collector.dao.HostApplicationMapDao;
import com.nhn.pinpoint.collector.util.AcceptedTimeService;
import com.nhn.pinpoint.collector.util.AtomicLongUpdateMap;
import com.nhn.pinpoint.common.buffer.AutomaticBuffer;
import com.nhn.pinpoint.common.buffer.Buffer;
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
	public void insert(String host, String bindApplicationName, short bindServiceType, String parentApplicationName, short parentServiceType) {
        if (host == null) {
            throw new NullPointerException("host must not be null");
        }
        if (bindApplicationName == null) {
            throw new NullPointerException("bindApplicationName must not be null");
        }

        final long statisticsRowSlot = getSlotTime();

        final CacheKey cacheKey = new CacheKey(host, bindApplicationName, bindServiceType, parentApplicationName, parentServiceType);
        final boolean update = updater.update(cacheKey, statisticsRowSlot);
        if (update) {
//            insertHost(host, bindApplicationName, bindServiceType, statisticsRowSlot, parentApplicationName, parentServiceType);
            insertHostVer2(host, bindApplicationName, bindServiceType, statisticsRowSlot, parentApplicationName, parentServiceType);
        }
	}


    private long getSlotTime() {
        final long acceptedTime = acceptedTimeService.getAcceptedTime();
        return TimeSlot.getStatisticsRowSlot(acceptedTime);
    }

    private void insertHost(String host, String bindApplicationName, short bindServiceType, long statisticsRowSlot, String parentApplicationName, short parentServiceType) {
        if (logger.isDebugEnabled()) {
            logger.debug("Insert host-application map. host={}, bindApplicationName={}, bindServiceType={}, parentApplicationName={}, parentServiceType={}",
                    host, bindApplicationName, bindServiceType, parentApplicationName, parentServiceType);
        }

        byte[] rowKey = Bytes.toBytes(TimeUtils.reverseTimeMillis(statisticsRowSlot));
        byte[] columnName = Bytes.toBytes(host);

        // TODO bindApplication의 size validation check를 해야 함.
        byte[] applicationNameBytes = Bytes.toBytes(bindApplicationName);
        byte[] offsetBytes = new byte[HBaseTables.APPLICATION_NAME_MAX_LEN - applicationNameBytes.length];
        byte[] serviceTypeBytes = Bytes.toBytes(bindServiceType);
        byte[] value = BytesUtils.concat(applicationNameBytes, offsetBytes, serviceTypeBytes);

        try {
            hbaseTemplate.put(HOST_APPLICATION_MAP, rowKey, HOST_APPLICATION_MAP_CF_MAP, columnName, value);
        } catch (Exception ex) {
            logger.warn("retry one. Caused:{}", ex.getCause(), ex);
            hbaseTemplate.put(HOST_APPLICATION_MAP, rowKey, HOST_APPLICATION_MAP_CF_MAP, columnName, value);
        }
    }

    private void insertHostVer2(String host, String bindApplicationName, short bindServiceType, long statisticsRowSlot, String parentApplicationName, short parentServiceType) {
        if (logger.isDebugEnabled()) {
            logger.debug("Insert host-application map. host={}, bindApplicationName={}, bindServiceType={}, parentApplicationName={}, parentServiceType={}",
                    host, bindApplicationName, bindServiceType, parentApplicationName, parentServiceType);
        }
        // 추후 추가를 다시 검토해볼것.
        String parentAgentId = null;
        final byte[] rowKey = createRowKey(parentApplicationName, parentServiceType, statisticsRowSlot, parentAgentId);

        byte[] columnName = createColumnName(host, bindApplicationName, bindServiceType);

        try {
            hbaseTemplate.put(HBaseTables.HOST_APPLICATION_MAP_VER2, rowKey, HBaseTables.HOST_APPLICATION_MAP_VER2_CF_MAP, columnName, null);
        } catch (Exception ex) {
            logger.warn("retry one. Caused:{}", ex.getCause(), ex);
            hbaseTemplate.put(HBaseTables.HOST_APPLICATION_MAP_VER2, rowKey, HBaseTables.HOST_APPLICATION_MAP_VER2_CF_MAP, columnName, null);
        }
    }

    private byte[] createColumnName(String host, String bindApplicationName, short bindServiceType) {
        Buffer buffer = new AutomaticBuffer();
        buffer.putPrefixedString(host);
        buffer.putPrefixedString(bindApplicationName);
        buffer.put(bindServiceType);
        return buffer.getBuffer();
    }

    byte[] createRowKey(String parentApplicationName, short parentServiceType, long statisticsRowSlot, String parentAgentId) {
        // 향후 이 뒤에다가 추가적인 스펙이 추가되어 agentId을 붙여도 스캔에 안전한것으로 판단됨. + 근데 parentAgentServiceType도 넣어야 되나???
        final int SIZE = HBaseTables.APPLICATION_NAME_MAX_LEN + 2 + 8;
        final Buffer rowKeyBuffer = new AutomaticBuffer(SIZE);
        rowKeyBuffer.putPadString(parentApplicationName, HBaseTables.APPLICATION_NAME_MAX_LEN);
        rowKeyBuffer.put(parentServiceType);
        rowKeyBuffer.put(TimeUtils.reverseTimeMillis(statisticsRowSlot));
        // parentAgentId는 아직 없는데. 나중을 추가되면 살려서 호환성 처리가 필요함.
//        rowKeyBuffer.putPadString(parentAgentId, HBaseTables.AGENT_NAME_MAX_LEN);
        return rowKeyBuffer.getBuffer();
    }

    private static final class CacheKey {
        private final String host;
        private final String applicationName;
        private final short serviceType;

        private final String parentApplicationName;
        private final short parentServiceType;

        public CacheKey(String host, String applicationName, short serviceType, String parentApplicationName, short parentServiceType) {
            if (host == null) {
                throw new NullPointerException("host must not be null");
            }
            if (applicationName == null) {
                throw new NullPointerException("bindApplicationName must not be null");
            }
            this.host = host;
            this.applicationName = applicationName;
            this.serviceType = serviceType;
            // 아래 두 parent 값은 null이 나올수 있음.
            this.parentApplicationName = parentApplicationName;
            this.parentServiceType = parentServiceType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (parentServiceType != cacheKey.parentServiceType) return false;
            if (serviceType != cacheKey.serviceType) return false;
            if (!applicationName.equals(cacheKey.applicationName)) return false;
            if (!host.equals(cacheKey.host)) return false;
            if (parentApplicationName != null ? !parentApplicationName.equals(cacheKey.parentApplicationName) : cacheKey.parentApplicationName != null)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = host.hashCode();
            result = 31 * result + applicationName.hashCode();
            result = 31 * result + (int) serviceType;
            result = 31 * result + (parentApplicationName != null ? parentApplicationName.hashCode() : 0);
            result = 31 * result + (int) parentServiceType;
            return result;
        }
    }
}
