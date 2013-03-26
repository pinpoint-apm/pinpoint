package com.nhn.hippo.web.dao.hbase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.web.dao.HostApplicationMapDao;
import com.nhn.hippo.web.vo.Application;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.TimeSlot;

/**
 * 
 * @author netspider
 * 
 */
@Repository
public class HbaseHostApplicationMapDao implements HostApplicationMapDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("hostApplicationMapper")
	private RowMapper<Application> hostApplicationMapper;

	@Override
	public Application findApplicationName(String host, long from, long to) {
		Scan scan = createScan(host, from, to);
		List<Application> result = hbaseOperations2.find(HBaseTables.HOST_APPLICATION_MAP, scan, hostApplicationMapper);
		if (result != null && result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}

	private Scan createScan(String host, long from, long to) {
		long startTime = TimeSlot.getStatisticsRowSlot(from);
		long endTime = TimeSlot.getStatisticsRowSlot(to) + 1;

		if (logger.isDebugEnabled()) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
			logger.debug("scan startTime:{} endTime:{}", simpleDateFormat.format(new Date(startTime)), simpleDateFormat.format(new Date(endTime)));
		}

		byte[] startKey = Bytes.toBytes(startTime);
		byte[] endKey = Bytes.toBytes(endTime);

		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);
		scan.setStartRow(startKey);
		scan.setStopRow(endKey);
		scan.addColumn(HBaseTables.HOST_APPLICATION_MAP_CF_MAP, Bytes.toBytes(host));
		scan.setId("HostApplicationScan");

		return scan;
	}

}
