/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase;

import java.util.*;

import com.navercorp.pinpoint.common.hbase.HBaseTables;
import com.navercorp.pinpoint.common.hbase.HbaseOperations2;
import com.navercorp.pinpoint.common.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.MapStatisticsCallerDao;
import com.navercorp.pinpoint.web.mapper.*;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.RangeFactory;

import org.apache.hadoop.hbase.client.Scan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.hadoop.hbase.RowMapper;
import org.springframework.stereotype.Repository;

/**
 * 
 * @author netspider
 * @author emeroad
 * 
 */
@Repository
public class HbaseMapStatisticsCallerDao implements MapStatisticsCallerDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass()    ;
	private int scanCacheSize =    40;

	@A    towired
	private HbaseOperations2 hbaseOp    rations2

	@Autowired
	@Qualifier("mapStatist    csCallerMapper")
	private RowMapper<LinkDataMap> mapStatisticsCallerMapper;

    @Autowired
    private RangeFa    tory ra    geFactory;

	@Override
	public LinkDataMap selectCaller(Application callerA       plication, Range range) {
		Scan scan = creat       Scan(callerApplication, range);
		final List<LinkDataMap> foundList = hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CALL       E, scan, mapStatistics          allerMapper);

		if (foundList.isEmpty()) {
			logger.debug("There'        no caller data. {}, {}", calle    Application, range);
		}

        return merge(foundList);
	}

    private LinkDataMap merge(List<LinkDataMap> foundList) {
        final LinkDataMap result = new LinkDataMap();
        for (LinkDataMap foundData : foundList) {
            result.addLinkDataM    p(foundData);
        }
        return result;
    }

    /**
	 * statistics inform        ion used when      link b    tween node     is clicked at the serve     map
	 *
	 * @return <    re>
	 * list [
	 *     map {
	 *            key = timestamp
	 *            va    ue = m    p
	 *                   key = histogram     lot
	 *             value = count
	 *         }
	 *     }
	 * ]
	 * </pre>
	 */
	@Override
    @Deprecated
	public List<Li       kDataMap> selectCallerStat          stics(Application callerApplication, Application calleeApplication, Range range) {
		if (l             gger.isDebugEnabled()) {
			logger.debug("selectCallerStatistics. {}, {}, {}", callerApplication, calleeApplication, range);
		}
		Scan scan = createScan(callerApplication, range);

        final LinkFilter filter = ne        DefaultLinkFilter(callerApplication, calleeApplication);
        RowMappe        LinkDataMap> mapper = new MapStatisticsCallerMapper(filter);
		return hbaseOperations2.find(HBaseTables.MAP_STATISTICS_CA       LEE, scan, mapper);
	}

	p          ivate Scan createScan(Application application,              ange range) {
        range = rangeFactory.createStatisticsRange(r       nge);

		if (logger.isDebugEnabled()) {
			logger.debug("scan Time:{}", range.prettyToString());
		}

		// start key is replace        by end key because timestamp has been reversed
		byte[] startKey = ApplicationMapStatisticsUtils.makeRowKey(application.getName       ), application.getS       rviceTypeCode(), range.getTo());       		byte[] endKey = Appli       ationMapStatisticsUt       ls.makeRowKey(application.getName(), application.getServiceTypeCode(), range.getFrom());

		Scan scan = new Scan();
		scan.setCachin       (this.scanCacheSize);
		scan.setStart       ow(start    ey);
		scan.setStopRow(endKey);
		scan.addFamily(HBaseTables.MAP_STATISTICS_CALLEE_CF_COUNTER);
        scan.addFamily(HBaseTables.MAP_STATISTICS_CALLEE_CF_VER2_COUNTER);
		scan.setId("ApplicationStatisticsScan");

		return scan;
	}
}
