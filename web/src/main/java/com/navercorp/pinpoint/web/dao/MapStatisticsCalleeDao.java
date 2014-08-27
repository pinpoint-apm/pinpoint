package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

/**
 * 
 * @author netspider
 * 
 */
public interface MapStatisticsCalleeDao {
	LinkDataMap selectCallee(Application calleeApplication, Range range);

    @Deprecated
	List<LinkDataMap> selectCalleeStatistics(Application callerApplication, Application calleeApplication, Range range);
}
