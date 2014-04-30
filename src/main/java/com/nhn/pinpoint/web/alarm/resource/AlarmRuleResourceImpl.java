package com.nhn.pinpoint.web.alarm.resource;

import com.nhn.pinpoint.web.alarm.MainCategory;
import com.nhn.pinpoint.web.alarm.SubCategory;

/**
 * 
 * @author koo.taejin
 */
public class AlarmRuleResourceImpl implements AlarmRuleResource {

	private final MainCategory main;
	private final SubCategory sub;
	private final int threshold;
	private final long continuationTime;

	// compare rule은 나중에
	public AlarmRuleResourceImpl(MainCategory main, SubCategory sub, int threshold, long continuationTime) {
		this.main = main;
		this.sub = sub;
		this.threshold = threshold;
		this.continuationTime = continuationTime;
	}

	public MainCategory getMain() {
		return main;
	}

	public SubCategory getSub() {
		return sub;
	}

	public int getThreshold() {
		return threshold;
	}

	public long getContinuationTime() {
		return continuationTime;
	}

}
