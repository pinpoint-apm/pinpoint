package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.service.NodeId;
import com.nhn.pinpoint.web.util.JsonSerializable;

/**
 * 
 * @author netspider
 * 
 */
public interface TimeSeriesStore extends JsonSerializable {
	void add(NodeId key, long timestamp, int responseTimeslot, long callCount, boolean isFailed);
}
