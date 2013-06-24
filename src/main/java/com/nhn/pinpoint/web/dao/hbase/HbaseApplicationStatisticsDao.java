package com.nhn.pinpoint.web.dao.hbase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;
import com.nhn.pinpoint.web.applicationmap.ApplicationStatistics;
import com.nhn.pinpoint.web.dao.ApplicationStatisticsDao;

/**
 * 
 * @author netspider
 * 
 */
@Repository
public class HbaseApplicationStatisticsDao implements ApplicationStatisticsDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("applicationStatisticsMapper")
	private RowMapper<ApplicationStatistics> applicationStatisticsMapper;

	@Override
	public ApplicationStatistics selectApplicationStatistics(String applicationName, short serviceType, long from, long to) {
		Scan scan = createScan(applicationName, serviceType, from, to);
		List<ApplicationStatistics> statisticsList = hbaseOperations2.find(HBaseTables.APPLICATION_STATISTICS, scan, applicationStatisticsMapper);
	
		if (statisticsList == null) {
			return null;
		}
		
		ApplicationStatistics statistics = new ApplicationStatistics(applicationName, serviceType); 
		
		for(ApplicationStatistics stat : statisticsList) {
			statistics.mergeWith(stat);
		}
		
		logger.debug("Merged applicationStatistics. {}", statistics);
		
		return statistics;
	}

	private Scan createScan(String applicationName, short serviceType, long from, long to) {
		long startTime = TimeSlot.getStatisticsRowSlot(from);
		// hbase의 scanner를 사용하여 검색시 endTime은 검색 대상에 포함되지 않기 때문에, +1을 해줘야 된다.
		long endTime = TimeSlot.getStatisticsRowSlot(to) + 1;

		if (logger.isDebugEnabled()) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
			logger.debug("scan startTime:{} endTime:{}", simpleDateFormat.format(new Date(startTime)), simpleDateFormat.format(new Date(endTime)));
		}

		// timestamp가 reverse되었기 때문에 start, end를 바꿔서 조회.
		byte[] startKey = ApplicationStatisticsUtils.makeRowKey(applicationName, serviceType, endTime);
		byte[] endKey = ApplicationStatisticsUtils.makeRowKey(applicationName, serviceType, startTime);

		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);
		scan.setStartRow(startKey);
		scan.setStopRow(endKey);
		scan.addFamily(HBaseTables.APPLICATION_STATISTICS_CF_COUNTER);
		scan.setId("ApplicationStatisticsScan");

		return scan;
	}
}
