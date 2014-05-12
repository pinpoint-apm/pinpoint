package com.nhn.pinpoint.web.alarm;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;
import com.nhn.pinpoint.web.alarm.resource.MailResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;
import com.nhn.pinpoint.web.vo.Application;

public class AlarmMailTemplate {

	private static final String LINE_FEED = "<br>";
	private static final String LINK_FORMAT = "<a href=\"%s\" >%s</a>";

	private final Application application;
	private final MailResource mailResource;
	private final AlarmEvent alarmEvent;
	
	public AlarmMailTemplate(Application application, MailResource mailResource, AlarmEvent alarmEvent) {
		this.application = application;
		this.mailResource = mailResource;
		this.alarmEvent = alarmEvent;
	}
	
	public String createSubject() {
		return String.format(mailResource.getSubject(), application.getName());
	}

	public String createBody(List<AlarmCheckFilter> alarmCheckFilterList) {
		StringBuilder body = new StringBuilder();
		body.append(application + " Alarm." + LINE_FEED);

		body.append("<ul>");
		for (AlarmCheckFilter alarmCheckFilter : alarmCheckFilterList) {
			AlarmRuleResource rule = alarmCheckFilter.getRule();
			
			body.append("<li>");

			body.append(String.format("<strong>%s</strong><br>", rule.getMainCategory().getName() + " (" + rule.getSubCategory().getName() + ")"));
			body.append(">= " + rule.getThresholdRule() + " " + rule.getSubCategory().getUnit() + " (" + TimeUnit.MILLISECONDS.toMinutes(rule.getContinuosTime()) + " min)<br>");
			String url = mailResource.getPinpointUrl() + "/#/main/" + application.getName() + "@" + application.getCode() + "/" + TimeUnit.MILLISECONDS.toMinutes(rule.getContinuosTime()) + "/" + alarmEvent.getEventStartTimeMillis();
			body.append(String.format(LINK_FORMAT, url, url));
//			contents.append("<br>");

			body.append("</li>");
		}
		body.append("</ul>");

		return body.toString();
	}
	
	
}
