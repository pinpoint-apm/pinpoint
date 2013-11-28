package com.nhn.pinpoint.web.dao.hbase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatisticsKey;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
import com.nhn.pinpoint.web.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.web.mapper.ApplicationMapLinkStatisticsMapper;
import com.nhn.pinpoint.common.hbase.HBaseTables;
import com.nhn.pinpoint.common.hbase.HbaseOperations2;
import com.nhn.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.nhn.pinpoint.common.util.TimeSlot;

/**
 * 
 * @author netspider
 * @author emeroad
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
	private RowMapper<List<TransactionFlowStatistics>> applicationMapStatisticsCalleeMapper;

	@Override
	public List<TransactionFlowStatistics> selectCallee(String callerApplicationName, short callerServiceType, long from, long to) {
		Scan scan = createScan(callerApplicationName, callerServiceType, from, to);
		final List<List<TransactionFlowStatistics>> foundListList = hbaseOperations2.find(HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE, scan, applicationMapStatisticsCalleeMapper);

		if (foundListList.isEmpty()) {
			logger.debug("There's no callee data. {}, {}, {}, {}", callerApplicationName, callerServiceType, from, to);
		}
		
        return merge(foundListList);
	}

    private List<TransactionFlowStatistics> merge(List<List<TransactionFlowStatistics>> foundListList) {
        final Map<TransactionFlowStatisticsKey, TransactionFlowStatistics> result = new HashMap<TransactionFlowStatisticsKey, TransactionFlowStatistics>();

        for (List<TransactionFlowStatistics> foundList : foundListList) {
            for (TransactionFlowStatistics found : foundList) {
                final TransactionFlowStatisticsKey key = new TransactionFlowStatisticsKey(found);
                final TransactionFlowStatistics find = result.get(key);
                if (find != null) {
                    find.add(found);
                } else {
                    result.put(key, found);
                }
            }
        }


        return new ArrayList<TransactionFlowStatistics>(result.values());
    }

    /**
	 * 메인페이지 서버 맵에서 연결선을 선택했을 때 보여주는 통계정보.
	 * 
	 * @return <pre>
	 * list [
	 *     map {
	 *         key = timestamp
	 *         value = map {
	 *             key = histogram slot
	 *             value = count
	 *         }
	 *     }
	 * ]
	 * </pre>
	 */
	@Override
	public List<Map<Long, Map<Short, Long>>> selectCalleeStatistics(String callerApplicationName, short callerServiceType, String calleeApplicationName, short calleeServiceType, long from, long to) {
		if (logger.isDebugEnabled()) {
			logger.debug("selectCalleeStatistics. {}, {}, {}, {}, {}, {}", callerApplicationName, callerServiceType, calleeApplicationName, calleeServiceType, from, to);
		}
		Scan scan = createScan(callerApplicationName, callerServiceType, from, to);
		RowMapper<Map<Long, Map<Short, Long>>> mapper = new ApplicationMapLinkStatisticsMapper(callerApplicationName, callerServiceType, calleeApplicationName, calleeServiceType);
		return hbaseOperations2.find(HBaseTables.APPLICATION_MAP_STATISTICS_CALLEE, scan, mapper);
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
