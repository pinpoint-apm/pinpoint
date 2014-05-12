package com.nhn.pinpoint.web.alarm;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmSendFilter;
import com.nhn.pinpoint.web.vo.Application;

/**
 * 
 * @author koo.taejin
 */
public class DefaultAlarmJob implements AlarmJob {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final Application application;
	
	public DefaultAlarmJob(Application application) {
		this.application = application;
	}

	private final List<AlarmCheckFilter> checkFilterList = new ArrayList<AlarmCheckFilter>();
	private final List<AlarmSendFilter> sendFilterList = new ArrayList<AlarmSendFilter>();

	@Override
	public boolean execute(AlarmEvent event) {
		logger.debug("{} {} execute. CheckFilterList={},  SendFilterList={}", application, this.getClass().getSimpleName(), checkFilterList, sendFilterList);
		
		for (AlarmCheckFilter checkFilter : this.checkFilterList) {
			boolean isSatisfy = checkFilter.check(event);
			logger.debug("{} filter Satisfy({})", checkFilter.getClass().getSimpleName(), isSatisfy);
			if (!isSatisfy) {
				return false;
			}
		}
		
		for (AlarmSendFilter sendFilter : this.sendFilterList) {
			sendFilter.send(this.checkFilterList, event);
		}
		
		return true;
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
