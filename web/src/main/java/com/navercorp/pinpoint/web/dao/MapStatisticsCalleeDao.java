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
public interface MapStatisticsCalleeDao {
	LinkDataMap selectCallee(Application calleeApplication, Range range);

    @Deprecated
	List<LinkDataMap> selectCalleeStatistics(Application callerApplication, Application calleeApplication, Range range);
}
