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
public interface MapStatisticsCalleeDao {
	public LinkStatisticsData selectCallee(Application calleeApplication, Range range);
	
	public List<LinkStatisticsData> selectCalleeStatistics(Application callerApplication, Application calleeApplication, Range range);
}
