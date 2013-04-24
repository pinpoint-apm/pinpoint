package com.nhn.hippo.web.dao.hbase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.hippo.web.applicationmap.ApplicationStatistics;
import com.nhn.hippo.web.dao.ApplicationMapStatisticsCalleeDao;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.hbase.HbaseOperations2;
import com.profiler.common.util.ApplicationMapStatisticsUtils;
import com.profiler.common.util.TimeSlot;
import com.profiler.common.util.TimeUtils;

/**
 * 
 * @author netspider
 * 
 */
@Repository
public class HbaseApplicationMapStatisticsCalleeDao implements ApplicationMapStatisticsCalleeDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("applicationMapStatisticsCalleeMapper")
	private RowMapper<Map<String, ApplicationStatistics>> applicationMapStatisticsCalleeMapper;

	@Override
	public Map<String, ApplicationStatistics> selectCallee(String callerApplicationName, short callerServiceType, long from, long to) {
		Scan scan = createScan(callerApplicationName, callerServiceType, from, to);
		List<Map<String, ApplicationStatistics>> found = hbaseOperations2.find(HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE, scan, applicationMapStatisticsCalleeMapper);

		Map<String, ApplicationStatistics> result = new HashMap<String, ApplicationStatistics>();

		for (Map<String, ApplicationStatistics> map : found) {
			for (Entry<String, ApplicationStatistics> entry : map.entrySet()) {
				if (result.containsKey(entry.getKey())) {
					result.get(entry.getKey()).mergeWith(entry.getValue());
				} else {
					result.put(entry.getKey(), entry.getValue());
				}
			}
		}

		return result;
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
		byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(applicationName, serviceType, endTime);
		byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(applicationName, serviceType, startTime);

		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);
		scan.setStartRow(startKey);
		scan.setStopRow(endKey);
		scan.addFamily(HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE_CF_COUNTER);
		scan.setId("ApplicationStatisticsScan");

		return scan;
	}
}
