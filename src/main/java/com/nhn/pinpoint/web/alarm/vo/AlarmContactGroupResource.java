package com.nhn.pinpoint.web.alarm.vo;

import java.util.List;

import org.apache.ibatis.type.Alias;

@Alias("alarmContactGroup")
public class AlarmContactGroupResource {

	private Integer id;
	private Integer alarmId;

	private String alarmContactGroupName;
	private String alarmContactGroupDescrption;

	private List<AlarmContactResource> alarmContactList;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAlarmId() {
		return alarmId;
	}

	public void setAlarmId(Integer alarmId) {
		this.alarmId = alarmId;
	}

	public String getAlarmContactGroupName() {
		return alarmContactGroupName;
	}

	public void setAlarmContactGroupName(String alarmContactGroupName) {
		this.alarmContactGroupName = alarmContactGroupName;
	}

	public String getAlarmContactGroupDescrption() {
		return alarmContactGroupDescrption;
	}

	public void setAlarmContactGroupDescrption(String alarmContactGroupDescrption) {
		this.alarmContactGroupDescrption = alarmContactGroupDescrption;
	}

	public List<AlarmContactResource> getAlarmContactList() {
		return alarmContactList;
	}

	public void setAlarmContactList(List<AlarmContactResource> alarmContactList) {
		this.alarmContactList = alarmContactList;
	}

	@Override
	public String toString() {
		return "AlarmContactGroupResource [id=" + id + ", alarmId=" + alarmId + ", alarmContactGroupName=" + alarmContactGroupName
				+ ", alarmContactGroupDescrption=" + alarmContactGroupDescrption + ", alarmContactList=" + alarmContactList + "]";
	}

}
