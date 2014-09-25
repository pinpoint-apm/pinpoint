package com.nhn.pinpoint.web.alarm.filter;


public abstract class AlarmCheckRatesFilter extends AlarmCheckFilter {

	protected boolean check(long count, long totalCount) {
		int rates = getRates(count, totalCount);

		int threshold = getRule().getThresholdRule();
		
		if (rates >= threshold) {
			return true;
		} else {
			return false;
		}
	}
	
	private int getRates(long count, long totalCount) {
		int percent = 0;
		if (count == 0 || totalCount == 0) {
			return percent;
		} else {
			percent = Math.round((count * 100) / totalCount);
		}

		return percent;
	}

}
