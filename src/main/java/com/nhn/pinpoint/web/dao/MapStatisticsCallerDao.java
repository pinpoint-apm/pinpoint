package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatisticsData;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

/**
 * 
 * @author netspider
 * 
 */
public interface MapStatisticsCallerDao {
	LinkStatisticsData selectCaller(Application callerApplication, Range range);

	List<LinkStatisticsData> selectCallerStatistics(Application callerApplication, Application calleeApplication, Range range);
}
