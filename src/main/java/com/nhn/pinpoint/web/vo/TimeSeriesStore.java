package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatisticsKey;
import com.nhn.pinpoint.web.util.JsonSerializable;

/**
 * 
 * @author netspider
 * 
 */
public interface TimeSeriesStore extends JsonSerializable {
	void addLinkStat(LinkStatisticsKey key, long timestamp, int responseTimeslot, long callCount, boolean isFailed);

    void addNodeStat(Application key, long timestamp, int responseTimeslot, long callCount, boolean isFailed);
}
