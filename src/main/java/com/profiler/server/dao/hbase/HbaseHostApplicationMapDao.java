package com.profiler.server.dao.hbase;

import static com.profiler.common.hbase.HBaseTables.HOST_APPLICATION_MAP;
import static com.profiler.common.hbase.HBaseTables.HOST_APPLICATION_MAP_CF_MAP;

import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.BytesUtils;
import com.profiler.common.util.TimeSlot;
import com.profiler.server.dao.HostApplicationMapDao;
import com.profiler.server.util.AcceptedTimeService;

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

		byte[] rowKey = Bytes.toBytes(TimeSlot.getStatisticsRowSlot(acceptedTimeService.getAcceptedTime()));
		byte[] columnName = Bytes.toBytes(host);

		byte[] applicationNameBytes = Bytes.toBytes(applicationName);
		byte[] offsetBytes = new byte[HBaseTables.APPLICATION_NAME_MAX_LEN - applicationNameBytes.length];
		byte[] serviceTypeBytes = Bytes.toBytes(serviceType);
		byte[] value = BytesUtils.concat(applicationNameBytes, offsetBytes, serviceTypeBytes);

		hbaseTemplate.put(HOST_APPLICATION_MAP, rowKey, HOST_APPLICATION_MAP_CF_MAP, columnName, value);
	}
}
