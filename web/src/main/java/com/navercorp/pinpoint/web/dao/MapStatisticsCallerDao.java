package com.navercorp.pinpoint.web.dao;

import java.util.List;

import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;

/**
 * 
 * @author netspider
 * 
 */
public interface MapStatisticsCallerDao {
	LinkDataMap selectCaller(Application callerApplication, Range range);

	List<LinkDataMap> selectCallerStatistics(Application callerApplication, Application calleeApplication, Range range);
}
