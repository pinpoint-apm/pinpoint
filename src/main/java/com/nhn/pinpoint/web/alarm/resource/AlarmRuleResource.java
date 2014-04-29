package com.nhn.pinpoint.web.alarm.resource;

import com.nhn.pinpoint.web.alarm.checker.MainCategory;
import com.nhn.pinpoint.web.alarm.checker.SubCategory;

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
