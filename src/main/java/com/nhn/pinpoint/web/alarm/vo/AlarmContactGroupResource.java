package com.nhn.pinpoint.web.alarm.vo;


import java.util.Date;
import java.util.List;

import org.apache.ibatis.type.Alias;

@Alias("alarmContactGroup")
public class AlarmContactGroupResource {

	private Integer id;

	private String alarmContactGroupName;
	private String alarmContactGroupDescrption;
	
	private Date registerTime;
	private String registerEmployeeNumber;
	
	private Date modifyTime;
	private String modifyEmployeeNumber;

	private List<AlarmContactResource> alarmContactList;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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
	

	public Date getRegisterTime() {
		return registerTime;
	}

	public void setRegisterTime(Date registerTime) {
		this.registerTime = registerTime;
	}

	public String getRegisterEmployeeNumber() {
		return registerEmployeeNumber;
	}

	public void setRegisterEmployeeNumber(String registerEmployeeNumber) {
		this.registerEmployeeNumber = registerEmployeeNumber;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	public String getModifyEmployeeNumber() {
		return modifyEmployeeNumber;
	}

	public void setModifyEmployeeNumber(String modifyEmployeeNumber) {
		this.modifyEmployeeNumber = modifyEmployeeNumber;
	}

	@Override
	public String toString() {
		return "AlarmContactGroupResource [id=" + id + ", alarmContactGroupName=" + alarmContactGroupName + ", alarmContactGroupDescrption="
				+ alarmContactGroupDescrption + ", registerTime=" + registerTime + ", registerEmployeeNumber=" + registerEmployeeNumber + ", modifyTime="
				+ modifyTime + ", modifyEmployeeNumber=" + modifyEmployeeNumber + ", alarmContactList=" + alarmContactList + "]";
	}

}
