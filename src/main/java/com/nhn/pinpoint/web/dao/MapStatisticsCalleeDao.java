package com.nhn.pinpoint.web.dao;

import java.util.List;
import java.util.Map;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

/**
 * 
 * @author netspider
 * 
 */
public interface MapStatisticsCalleeDao {
	public List<LinkStatistics> selectCallee(Application calleeApplication, Range range);
	
	public List<Map<Long, Map<Short, Long>>> selectCalleeStatistics(Application callerApplication, Application calleeApplication, Range range);
}
