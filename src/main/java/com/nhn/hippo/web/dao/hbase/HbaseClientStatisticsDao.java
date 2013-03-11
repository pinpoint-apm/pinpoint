package com.nhn.hippo.web.dao.hbase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.web.dao.ClientStatisticsDao;
import com.nhn.hippo.web.vo.ClientStatistics;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.ClientStatUtils;
import com.profiler.common.util.TimeSlot;

/**
 * 
 * @author netspider
 * 
 */
@Repository
public class HbaseClientStatisticsDao implements ClientStatisticsDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("clientStatisticsMapper")
	private RowMapper<Map<String, ClientStatistics>> clientStatisticsMapper;

	@Override
	public List<Map<String, ClientStatistics>> selectClient(String applicationName, short serviceType, long from, long to) {
		Scan scan = createScan(applicationName, serviceType, from, to);
		return hbaseOperations2.find(HBaseTables.CLIENT_STATISTICS, scan, clientStatisticsMapper);
	}

	private Scan createScan(String applicationName, short serviceType, long from, long to) {
		long startTime = TimeSlot.getStatisticsRowSlot(from);
		// hbase의 scanner를 사용하여 검색시 endTime은 검색 대상에 포함되지 않기 때문에, +1을 해줘야 된다.
		long endTime = TimeSlot.getStatisticsRowSlot(to) + 1;
		if (logger.isDebugEnabled()) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
			logger.debug("scan startTime:{} endTime:{}", simpleDateFormat.format(new Date(startTime)), simpleDateFormat.format(new Date(endTime)));
		}
		byte[] startKey = ClientStatUtils.makeRowKey(applicationName, serviceType, startTime);
		byte[] endKey = ClientStatUtils.makeRowKey(applicationName, serviceType, endTime);

		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);
		scan.setStartRow(startKey);
		scan.setStopRow(endKey);
		scan.addFamily(HBaseTables.CLIENT_STATISTICS_CF_COUNTER);
		scan.setId("clientStatisticsScan");

		return scan;
	}
}
