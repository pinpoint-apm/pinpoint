package com.nhn.pinpoint.web.alarm.vo;

import org.apache.ibatis.type.Alias;

@Alias("alarmContact")
public class AlarmContactResource {

	private Integer id;
	private Integer alarmContactGroupId;
	private String phoneNum;
	private String emailAddress;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getAlarmContactGroupId() {
		return alarmContactGroupId;
	}
	public void setAlarmContactGroupId(Integer alarmContactGroupId) {
		this.alarmContactGroupId = alarmContactGroupId;
	}
	public String getPhoneNum() {
		return phoneNum;
	}
	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@Override
	public String toString() {
		return "AlarmContactResource [id=" + id + ", alarmContactGroupId=" + alarmContactGroupId + ", phoneNum=" + phoneNum + ", emailAddress=" + emailAddress
				+ "]";
	}
	
}
