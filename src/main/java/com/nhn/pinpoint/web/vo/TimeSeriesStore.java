package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.util.JsonSerializable;

/**
 * 
 * @author netspider
 * 
 */
public interface TimeSeriesStore extends JsonSerializable {
	void addLinkStat(LinkKey key, long timestamp, int responseTimeslot, long callCount, boolean isFailed);

    void addNodeStat(Application key, long timestamp, int responseTimeslot, long callCount, boolean isFailed);
}
