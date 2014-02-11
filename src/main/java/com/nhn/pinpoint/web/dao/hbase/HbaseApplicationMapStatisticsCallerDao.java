package com.nhn.pinpoint.web.dao.hbase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatisticsKey;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

import com.nhn.pinpoint.web.dao.ApplicationMapStatisticsCallerDao;
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
public class HbaseApplicationMapStatisticsCallerDao implements ApplicationMapStatisticsCallerDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("applicationMapStatisticsCallerMapper")
	private RowMapper<List<LinkStatistics>> applicationMapStatisticsCallerMapper;

	@Override
	public List<LinkStatistics> selectCaller(Application calleeApplication, Range range) {
        if (calleeApplication == null) {
            throw new NullPointerException("calleeApplication must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        Scan scan = createScan(calleeApplication, range);
		final List<List<LinkStatistics>> foundListList = hbaseOperations2.find(HBaseTables.APPLICATION_MAP_STATISTICS_CALLER, scan, applicationMapStatisticsCallerMapper);

		if (foundListList.isEmpty()) {
			logger.debug("There's no caller data. {}, {}", calleeApplication, range);
		}
		
        return merge(foundListList);
	}

    private List<LinkStatistics> merge(List<List<LinkStatistics>> foundListList) {
        final Map<LinkStatisticsKey, LinkStatistics> result = new HashMap<LinkStatisticsKey, LinkStatistics>();

        for (List<LinkStatistics> foundList : foundListList) {
            for (LinkStatistics found : foundList) {
                final LinkStatisticsKey key = new LinkStatisticsKey(found);
                final LinkStatistics find = result.get(key);
                if (find != null) {
                    find.add(found);
                } else {
                    result.put(key, found);
                }
            }
        }

        return new ArrayList<LinkStatistics>(result.values());
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
	public List<Map<Long, Map<Short, Long>>> selectCallerStatistics(Application callerApplication, Application calleeApplication, Range range) {
		if (logger.isDebugEnabled()) {
			logger.debug("selectCallerStatistics. {}, {}, {}", callerApplication, calleeApplication, range);
		}
		Scan scan = createScan(calleeApplication, range);
		RowMapper<Map<Long, Map<Short, Long>>> mapper = new ApplicationMapLinkStatisticsMapper(callerApplication, calleeApplication);
		return hbaseOperations2.find(HBaseTables.APPLICATION_MAP_STATISTICS_CALLER, scan, mapper);
	}

	private Scan createScan(Application application, Range range) {
		long startTime = TimeSlot.getStatisticsRowSlot(range.getFrom());
		// hbase의 scanner를 사용하여 검색시 endTime은 검색 대상에 포함되지 않기 때문에, +1을 해줘야 된다.
		long endTime = TimeSlot.getStatisticsRowSlot(range.getTo()) + 1;
		
		if (logger.isDebugEnabled()) {
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss,SSS");
			logger.debug("scan startTime:{} endTime:{}", simpleDateFormat.format(new Date(startTime)), simpleDateFormat.format(new Date(endTime)));
		}
		
		// timestamp가 reverse되었기 때문에 start, end를 바꿔서 조회.
		byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), endTime);
		byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), startTime);

		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);
		scan.setStartRow(startKey);
		scan.setStopRow(endKey);
		scan.addFamily(HBaseTables.APPLICATION_MAP_STATISTICS_CALLER_CF_COUNTER);
		scan.setId("ApplicationStatisticsScan");

		return scan;
	}
}
