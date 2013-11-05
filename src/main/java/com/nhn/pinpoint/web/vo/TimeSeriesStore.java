package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.web.util.JsonSerializable;

/**
 * 
 * @author netspider
 * 
 */
public interface TimeSeriesStore extends JsonSerializable {
	void add(String key, long timestamp, int responseTime, long count);
}
