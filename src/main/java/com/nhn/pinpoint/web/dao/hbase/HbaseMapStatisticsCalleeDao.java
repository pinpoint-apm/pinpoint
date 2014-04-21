package com.nhn.pinpoint.web.dao.hbase;

import java.util.*;


import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.nhn.pinpoint.web.dao.MapStatisticsCalleeDao;
import com.nhn.pinpoint.web.mapper.*;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;
import com.nhn.pinpoint.web.vo.RangeFactory;
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

/**
 * 
 * @author netspider
 * @author emeroad
 * 
 */
@Repository
public class HbaseMapStatisticsCalleeDao implements MapStatisticsCalleeDao {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private int scanCacheSize = 40;

	@Autowired
	private HbaseOperations2 hbaseOperations2;

	@Autowired
	@Qualifier("mapStatisticsCalleeMapper")
	private RowMapper<LinkDataMap> mapStatisticsCalleeMapper;

    @Autowired
    private RangeFactory rangeFactory;

	@Override
	public LinkDataMap selectCallee(Application calleeApplication, Range range) {
        if (calleeApplication == null) {
            throw new NullPointerException("calleeApplication must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        Scan scan = createScan(calleeApplication, range);
		List<LinkDataMap> foundListList = hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLER, scan, mapStatisticsCalleeMapper);

		if (foundListList.isEmpty()) {
			logger.debug("There's no caller data. {}, {}", calleeApplication, range);
		}
		
        return merge(foundListList);
	}

    private LinkDataMap merge(List<LinkDataMap> foundListList) {
        final LinkDataMap result = new LinkDataMap();

        for (LinkDataMap foundList : foundListList) {
            result.addLinkDataMap(foundList);
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
	public List<LinkDataMap> selectCalleeStatistics(Application callerApplication, Application calleeApplication, Range range) {
		if (logger.isDebugEnabled()) {
			logger.debug("selectCalleeStatistics. {}, {}, {}", callerApplication, calleeApplication, range);
		}
		Scan scan = createScan(calleeApplication, range);


        final LinkFilter filter = new DefaultLinkFilter(callerApplication, calleeApplication);
        RowMapper<LinkDataMap> mapper = new MapStatisticsCalleeMapper(filter);
		return hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALLER, scan, mapper);
	}

	private Scan createScan(Application application, Range range) {
        range = rangeFactory.createReverseStatisticsRange(range);

		if (logger.isDebugEnabled()) {
			logger.debug("scan time:{} ", range.prettyToString());
		}
		
		// timestamp가 reverse되었기 때문에 start, end를 바꿔서 조회.
		byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getFrom());
		byte[] endKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getTo());

		Scan scan = new Scan();
		scan.setCaching(this.scanCacheSize);
		scan.setStartRow(startKey);
		scan.setStopRow(endKey);
		scan.addFamily(HBaseTables.MAP_STATISTICS_CALLEE_CF_COUNTER);
		scan.setId("ApplicationStatisticsScan");

		return scan;
	}
}
