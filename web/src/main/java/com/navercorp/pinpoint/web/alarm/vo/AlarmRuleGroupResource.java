package com.nhn.pinpoint.web.alarm.vo;

import java.util.List;

import org.apache.ibatis.type.Alias;

@Alias("alarmRuleGroup")
public class AlarmRuleGroupResource {

	private Integer id;

	private String alarmRuleGroupName;
	private String alarmRuleGroupDescrption;

	private List<AlarmRuleResource> alarmRuleList;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getAlarmRuleGroupName() {
		return alarmRuleGroupName;
	}

	public void setAlarmRuleGroupName(String alarmRuleGroupName) {
		this.alarmRuleGroupName = alarmRuleGroupName;
	}

	public String getAlarmRuleGroupDescrption() {
		return alarmRuleGroupDescrption;
	}

	public void setAlarmRuleGroupDescrption(String alarmRuleGroupDescrption) {
		this.alarmRuleGroupDescrption = alarmRuleGroupDescrption;
	}

	public List<AlarmRuleResource> getAlarmRuleList() {
		return alarmRuleList;
	}

	public void setAlarmRuleList(List<AlarmRuleResource> alarmRuleList) {
		this.alarmRuleList = alarmRuleList;
	}

	@Override
	public String toString() {
		return "AlarmRuleGroupResource [id=" + id + ", alarmRuleGroupName=" + alarmRuleGroupName + ", alarmRuleGroupDescrption="
				+ alarmRuleGroupDescrption + ", alarmRuleList=" + alarmRuleList + "]";
	}

}
