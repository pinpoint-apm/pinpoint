package com.nhn.pinpoint.web.alarm;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmSendFilter;
import com.nhn.pinpoint.web.vo.Application;

/**
 * 
 * @author koo.taejin
 */
public class DefaultAlarmJob implements AlarmJob {
	
	private final Application application;
	
	public DefaultAlarmJob(Application application) {
		this.application = application;
	}

	private final List<AlarmCheckFilter> checkFilterList = new ArrayList<AlarmCheckFilter>();
	private final List<AlarmSendFilter> sendFilterList = new ArrayList<AlarmSendFilter>();

	@Override
	public void execute(AlarmEvent event) {
		
		for (AlarmCheckFilter checkFilter : this.checkFilterList) {
			boolean isSatisfy = checkFilter.execute(event);
			if (!isSatisfy) {
				return;
			}
		}
		
		for (AlarmSendFilter sendFilter : this.sendFilterList) {
			sendFilter.execute(event);
		}
		
	}

	public <T extends AlarmFilter> void addFilter(List<T> filterList) {
		for (AlarmFilter filter : filterList) {
			addFilter(filter);
		}
	}

	public boolean addFilter(AlarmFilter filter) {
		if (filter instanceof AlarmSendFilter) {
			sendFilterList.add((AlarmSendFilter) filter);
			return true;
		} 
		
		if (filter instanceof AlarmCheckFilter) {
			checkFilterList.add((AlarmCheckFilter) filter);
			return true;
		}
		
		return false;
	}

}
