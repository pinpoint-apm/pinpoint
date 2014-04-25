package com.nhn.pinpoint.web.alarm;

import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmSendFilter;

/**
 * 
 * @author koo.taejin
 */
public class DefaultAlarmFilterContext implements AlarmFilterContext {

	private final AlarmFilter filter;

	private AlarmFilterContext prev;
	private AlarmFilterContext next;

	public DefaultAlarmFilterContext(AlarmFilter filter) {
		this.filter = filter;
	}

	@Override
	public boolean canCheck() {
		if (filter instanceof AlarmCheckFilter) {
			return true;
		}

		return false;
	}

	@Override
	public boolean check(AlarmEvent event) {
		AlarmFilterContext context = this;

		if (context.canCheck()) {
			boolean isSuccess = context.getFilter().execute(event);
			if (!isSuccess) {
				return false;
			}
		}

		context = context.getNext();
		if (context == null) {
			return true;
		}

		return context.check(event);
	}

	@Override
	public boolean canSend() {
		if (filter instanceof AlarmSendFilter) {
			return true;
		}

		return false;
	}

	@Override
	public void send(AlarmEvent event) {
		AlarmFilterContext context = this;

		if (context.canSend()) {
			context.getFilter().execute(event);
		}

		context = context.getNext();
		if (context == null) {
			return;
		}

		context.send(event);
	}

	@Override
	public AlarmFilterContext getPrev() {
		return prev;
	}

	@Override
	public AlarmFilterContext getNext() {
		return next;
	}

	@Override
	public AlarmFilter getFilter() {
		return filter;
	}

	public void setPrev(AlarmFilterContext prev) {
		this.prev = prev;
	}

	public void setNext(AlarmFilterContext next) {
		this.next = next;
	}

}
