package com.nhn.pinpoint.collector.dao.hbase;

import static com.nhn.pinpoint.common.hbase.HBaseTables.HOST_APPLICATION_MAP;
import static com.nhn.pinpoint.common.hbase.HBaseTables.HOST_APPLICATION_MAP_CF_MAP;

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

/**
 * 
 * @author netspider
 */
public class HbaseHostApplicationMapDao implements HostApplicationMapDao {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private HbaseOperations2 hbaseTemplate;

	@Autowired
	private AcceptedTimeService acceptedTimeService;

	@Override
	public void insert(String host, String applicationName, short serviceType) {
		logger.debug("Insert host-application map. host={}, applicationName={}, serviceType={}", new Object[] { host, applicationName, serviceType });

		byte[] rowKey = Bytes.toBytes(TimeUtils.reverseCurrentTimeMillis(TimeSlot.getStatisticsRowSlot(acceptedTimeService.getAcceptedTime())));
		byte[] columnName = Bytes.toBytes(host);

		byte[] applicationNameBytes = Bytes.toBytes(applicationName);
		byte[] offsetBytes = new byte[HBaseTables.APPLICATION_NAME_MAX_LEN - applicationNameBytes.length];
		byte[] serviceTypeBytes = Bytes.toBytes(serviceType);
		byte[] value = BytesUtils.concat(applicationNameBytes, offsetBytes, serviceTypeBytes);

		hbaseTemplate.put(HOST_APPLICATION_MAP, rowKey, HOST_APPLICATION_MAP_CF_MAP, columnName, value);
	}
}
