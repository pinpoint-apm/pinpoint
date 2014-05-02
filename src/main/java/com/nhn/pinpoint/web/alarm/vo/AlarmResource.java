package com.nhn.pinpoint.web.alarm.vo;

import org.apache.ibatis.type.Alias;

@Alias("alarm")
public class AlarmResource {
	
	private Integer id;

	private String alarmGroupName;
	private String alarmGroupDescrption;
	
	private AlarmRuleGroupResource alarmRuleGroup;
	private AlarmContactGroupResource alarmContactGroup;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getAlarmGroupName() {
		return alarmGroupName;
	}
	public void setAlarmGroupName(String alarmGroupName) {
		this.alarmGroupName = alarmGroupName;
	}
	
	public String getAlarmGroupDescrption() {
		return alarmGroupDescrption;
	}
	public void setAlarmGroupDescrption(String alarmGroupDescrption) {
		this.alarmGroupDescrption = alarmGroupDescrption;
	}
	
	public AlarmRuleGroupResource getAlarmRuleGroup() {
		return alarmRuleGroup;
	}
	
	public void setAlarmRuleGroup(AlarmRuleGroupResource alarmRuleGroup) {
		this.alarmRuleGroup = alarmRuleGroup;
	}
	
	public AlarmContactGroupResource getAlarmContactGroup() {
		return alarmContactGroup;
	}
	
	public void setAlarmContactGroup(AlarmContactGroupResource alarmContactGroup) {
		this.alarmContactGroup = alarmContactGroup;
	}

	@Override
	public String toString() {
		return "AlarmResource [id=" + id + ", alarmGroupName=" + alarmGroupName + ", alarmGroupDescrption=" + alarmGroupDescrption + ", alarmRuleGroup="
				+ alarmRuleGroup + ", alarmContactGroup=" + alarmContactGroup + "]";
	}

}
