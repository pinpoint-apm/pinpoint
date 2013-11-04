package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.HOST_APPLICATION_MAP;
import static com.nhn.pinpoint.common.hbase.HBaseTables.HOST_APPLICATION_MAP_CF_MAP;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

	private final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<String, Object>(1024);
	private long lastUpdated = System.currentTimeMillis();

	@Override
	public void insert(String host, String applicationName, short serviceType) {
		String cacheKey = host + applicationName + serviceType;

		// 매 번 넣을 필요 없음.
		if (cache.containsKey(cacheKey)) {
			logger.debug("Skip insert host-application map. host={}, applicationName={}, serviceType={}", host, applicationName, serviceType);
			return;
		}

		logger.debug("Insert host-application map. host={}, applicationName={}, serviceType={}", host, applicationName, serviceType);

		byte[] rowKey = Bytes.toBytes(TimeUtils.reverseCurrentTimeMillis(TimeSlot.getStatisticsRowSlot(acceptedTimeService.getAcceptedTime())));
		byte[] columnName = Bytes.toBytes(host);

		byte[] applicationNameBytes = Bytes.toBytes(applicationName);
		byte[] offsetBytes = new byte[HBaseTables.APPLICATION_NAME_MAX_LEN - applicationNameBytes.length];
		byte[] serviceTypeBytes = Bytes.toBytes(serviceType);
		byte[] value = BytesUtils.concat(applicationNameBytes, offsetBytes, serviceTypeBytes);

		hbaseTemplate.put(HOST_APPLICATION_MAP, rowKey, HOST_APPLICATION_MAP_CF_MAP, columnName, value);

		if (System.currentTimeMillis() - lastUpdated > 5000) {
			cache.clear();
		} else {
			cache.put(cacheKey, 1);
		}
		lastUpdated = System.currentTimeMillis();
	}
}
