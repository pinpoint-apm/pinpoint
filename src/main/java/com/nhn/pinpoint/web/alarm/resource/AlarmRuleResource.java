package com.nhn.pinpoint.web.alarm.resource;

import com.nhn.pinpoint.web.alarm.MainCategory;
import com.nhn.pinpoint.web.alarm.SubCategory;

/**
 * 
 * @author koo.taejin
 */
public interface AlarmRuleResource {

	MainCategory getMain();

	SubCategory getSub();
	
	int getThreshold();
	
	long getContinuationTime();

}
