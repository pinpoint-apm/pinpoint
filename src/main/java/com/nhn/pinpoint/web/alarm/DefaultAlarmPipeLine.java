package com.nhn.pinpoint.web.alarm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.alarm.filter.AlarmFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmHeadFilter;

/**
 * 
 * @author koo.taejin
 */
public class DefaultAlarmPipeLine implements AlarmPipeLine {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final DefaultAlarmFilterContext head;

	public DefaultAlarmPipeLine() {
		head = new DefaultAlarmFilterContext(new AlarmHeadFilter());
	}

	@Override
	public void execute(AlarmEvent event) {
		boolean isSuccess = head.check(event);
		if (isSuccess) {
			head.send(event);
		}
	}

	@Override
	public boolean addLast(AlarmFilter filter) {
		
		// Context를 받는 것이 아니기 때문에 사용자가 잘못하더라도, 무한반복이 될수 없음. 무한반복하면 내가 잘못 짠거임 ㅠ_ㅠ
		// 그래서 일부러 중복 검사는 하지 않는다. 
		
		AlarmFilterContext lastContext = head;
		while (true) {
			AlarmFilterContext context = lastContext.getNext();
			if (context == null) {
				break;
			} else {
				lastContext = context;
			}
		}

		if (!(lastContext instanceof DefaultAlarmFilterContext)) {
			logger.warn("AlarmFilterContext unexpected(actual={}, expected={}) class.", 
				lastContext.getClass().getName(), DefaultAlarmFilterContext.class.getName());
			return false;
		}

		DefaultAlarmFilterContext lastAlarmFilterContext = (DefaultAlarmFilterContext) lastContext;

		DefaultAlarmFilterContext newConext = new DefaultAlarmFilterContext(filter);

		lastAlarmFilterContext.setNext(newConext);
		newConext.setPrev(lastContext);
		
		return true;
	}

}
