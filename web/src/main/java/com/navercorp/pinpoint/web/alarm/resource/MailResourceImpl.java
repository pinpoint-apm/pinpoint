package com.nhn.pinpoint.web.alarm.resource;

import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * @author koo.taejin
 */
public class MailResourceImpl implements MailResource {

	@Value("#{dataProps['pinpoint.url']}")
	private String pinpointUrl;
	@Value("#{dataProps['alarm.mail.url']}")
	private String url;
	@Value("#{dataProps['alarm.mail.serviceId']}")
	private String serviceId;
	@Value("#{dataProps['alarm.mail.sender.emailAddress']}")
	private String senderEmailAddress;
	@Value("#{dataProps['alarm.mail.option']}")
	private String option;
	@Value("#{dataProps['alarm.mail.subject']}")
	private String subject;
	
	
	
	@Override
	public String getPinpointUrl() {
		return pinpointUrl;
	}
	public void setPinpointUrl(String pinpointUrl) {
		this.pinpointUrl = pinpointUrl;
	}

	@Override
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	
	@Override
	public String getSenderEmailAddress() {
		return senderEmailAddress;
	}
	public void setSenderEmailAddress(String senderEmailAddress) {
		this.senderEmailAddress = senderEmailAddress;
	}
	
	@Override
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}

	@Override
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	@Override
	public String toString() {
		return "MailResourceImpl [pinpointUrl=" + pinpointUrl + ", url=" + url + ", serviceId=" + serviceId + ", senderEmailAddress=" + senderEmailAddress
				+ ", option=" + option + ", subject=" + subject + "]";
	}
	
}
