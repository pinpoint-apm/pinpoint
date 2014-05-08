package com.nhn.pinpoint.web.alarm.resource;

import org.springframework.beans.factory.annotation.Value;

/**
 * 
 * @author koo.taejin
 */
public class SmsResourceImpl implements SmsResource {

	@Value("#{dataProps['pinpoint.url']}")
	private String pinpointUrl;
	@Value("#{dataProps['alarm.sms.url']}")
	private String url;
	@Value("#{dataProps['alarm.sms.serviceId']}")
	private String serviceId;
	@Value("#{dataProps['alarm.sms.sender.phoneNumber']}")
	private String senderPhoneNumber;
	
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
	public String getSenderPhoneNumber() {
		return senderPhoneNumber;
	}
	public void setSenderPhoneNumber(String senderPhoneNumber) {
		this.senderPhoneNumber = senderPhoneNumber;
	}
	
	@Override
	public String toString() {
		return "SmsResourceImpl [pinpointUrl=" + pinpointUrl + ", url=" + url + ", serviceId=" + serviceId + ", senderPhoneNumber=" + senderPhoneNumber + "]";
	}
	
}
