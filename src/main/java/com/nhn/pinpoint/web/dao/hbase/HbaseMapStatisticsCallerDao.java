package com.nhn.pinpoint.web.dao.hbase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.nhn.pinpoint.web.dao.MapStatisticsCallerDao;
import com.nhn.pinpoint.web.mapper.*;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

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
public class HbaseMapStatisticsCallerDao implements MapStatisticsCallerDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("mapStatisticsCallerMapper")
	private RowMapper<LinkDataMap> mapStatisticsCallerMapper;

	@Override
	public LinkDataMap selectCaller(Application callerApplication, Range range) {
		Scan scan = createScan(callerApplication, range);
		final List<LinkDataMap> foundList = hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLEE, scan, mapStatisticsCallerMapper);

		if (foundList.isEmpty()) {
			logger.debug("There's no caller data. {}, {}", callerApplication, range);
		}
		// 시계열 데이터가 토탈 머지 데이터로 변경되는듯함.
        return merge(foundList);
	}

    private LinkDataMap merge(List<LinkDataMap> foundList) {
        final LinkDataMap result = new LinkDataMap();
        for (LinkDataMap foundData : foundList) {
            result.addLinkDataMap(foundData);
        }
        return result;
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
	public List<LinkDataMap> selectCallerStatistics(Application callerApplication, Application calleeApplication, Range range) {
		if (logger.isDebugEnabled()) {
			logger.debug("selectCallerStatistics. {}, {}, {}", callerApplication, calleeApplication, range);
		}
		Scan scan = createScan(callerApplication, range);

        final LinkFilter filter = new DefaultLinkFilter(callerApplication, calleeApplication);
        RowMapper<LinkDataMap> mapper = new MapStatisticsCallerMapper(filter);
		return hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLEE, scan, mapper);
	}

	private Scan createScan(Application application, Range range) {
        long startTime = TimeSlot.getStatisticsRowSlot(range.getFrom()) - 1;
		// hbase의 scanner를 사용하여 검색시 endTime은 검색 대상에 포함되지 않기 때문에, +1을 해줘야 된다.
		long endTime = TimeSlot.getStatisticsRowSlot(range.getTo());

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
		scan.addFamily(HBaseTables.MAP_STATISTICS_CALLEE_CF_COUNTER);
        scan.addFamily(HBaseTables.MAP_STATISTICS_CALLEE_CF_VER2_COUNTER);
		scan.setId("ApplicationStatisticsScan");

		return scan;
	}
}
