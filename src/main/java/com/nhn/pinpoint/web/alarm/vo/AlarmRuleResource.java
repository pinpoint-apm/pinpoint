package com.nhn.pinpoint.web.alarm.vo;

import org.apache.ibatis.type.Alias;

import com.nhn.pinpoint.web.alarm.MainCategory;
import com.nhn.pinpoint.web.alarm.SubCategory;

@Alias("alarmRule")
public class AlarmRuleResource {

	private Integer id;
	private Integer alarmRuleGroupId;
	private Integer alarmRuleSubCategoryId;
	private String agentId;
	private Integer thresholdRule;
	private boolean compareRule;
	private Integer continuosTime;
	private String alarmRuleName;
	private String alarmRuleDescrption;
	
//	private MainCategory mainCategory;
//	private SubCategory subCategory;

	private String mainCategory;
	private String subCategory;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAlarmRuleGroupId() {
		return alarmRuleGroupId;
	}

	public void setAlarmRuleGroupId(Integer alarmRuleGroupId) {
		this.alarmRuleGroupId = alarmRuleGroupId;
	}

	public Integer getAlarmRuleSubCategoryId() {
		return alarmRuleSubCategoryId;
	}

	public void setAlarmRuleSubCategoryId(Integer alarmRuleSubCategoryId) {
		this.alarmRuleSubCategoryId = alarmRuleSubCategoryId;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public Integer getThresholdRule() {
		return thresholdRule;
	}

	public void setThresholdRule(Integer thresholdRule) {
		this.thresholdRule = thresholdRule;
	}

	public boolean isCompareRule() {
		return compareRule;
	}

	public void setCompareRule(boolean compareRule) {
		this.compareRule = compareRule;
	}

	public Integer getContinuosTime() {
		return continuosTime;
	}

	public void setContinuosTime(Integer continuosTime) {
		this.continuosTime = continuosTime;
	}

	public String getAlarmRuleName() {
		return alarmRuleName;
	}

	public void setAlarmRuleName(String alarmRuleName) {
		this.alarmRuleName = alarmRuleName;
	}

	public String getAlarmRuleDescrption() {
		return alarmRuleDescrption;
	}

	public void setAlarmRuleDescrption(String alarmRuleDescrption) {
		this.alarmRuleDescrption = alarmRuleDescrption;
	}


	public MainCategory getMainCategory() {
		return MainCategory.getValue(mainCategory);
	}

	public void setMainCategory(String mainCategory) {
		this.mainCategory = mainCategory;
	}

	public SubCategory getSubCategory() {
		return SubCategory.getValue(subCategory);
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	@Override
	public String toString() {
		return "AlarmRuleResource [id=" + id + ", alarmRuleGroupId=" + alarmRuleGroupId + ", alarmRuleSubCategoryId=" + alarmRuleSubCategoryId + ", agentId="
				+ agentId + ", thresholdRule=" + thresholdRule + ", compareRule=" + compareRule + ", continuosTime=" + continuosTime + ", alarmRuleDescrption="
				+ alarmRuleDescrption + ", alarmRuleName=" + alarmRuleName + ", mainCategory=" + getMainCategory() + ", subCategory=" + getSubCategory() + "]";
	}

//	public String getMainCategory() {
//		return mainCategory;
//	}
//
//	public void setMainCategory(String mainCategory) {
//		this.mainCategory = mainCategory;
//	}
//
//	public String getSubCategory() {
//		return subCategory;
//	}
//
//	public void setSubCategory(String subCategory) {
//		this.subCategory = subCategory;
//	}
	

}
