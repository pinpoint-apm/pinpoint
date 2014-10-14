package com.nhn.pinpoint.web.alarm.vo;

import org.apache.ibatis.type.Alias;

@Alias(value = "rule")
public class Rule {

	private String id;
	private String applicationId;
	private String CheckerName;
	private Integer threshold;
	private String empGroup;
	private boolean smsSend;
	private boolean emailSend;
	
	public Rule() {
	}
	
	public Rule(String applicationId, String checkerName, Integer Threshold, String empGroup, boolean smsSend, boolean emailSend) {
		this.applicationId = applicationId;
		this.CheckerName = checkerName;
		this.threshold = Threshold;
		this.empGroup = empGroup;
		this.smsSend = smsSend;
		this.emailSend = emailSend;
	}
	
	public String getApplicationId() {
		return applicationId;
	}
	
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	public String getCheckerName() {
		return CheckerName;
	}
	
	public void setCheckerName(String checkerName) {
		CheckerName = checkerName;
	}
	
	public Integer getThreshold() {
		return threshold;
	}
	
	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}
	
	public String getEmpGroup() {
		return empGroup;
	}
	
	public void setEmpGroup(String empGroup) {
		this.empGroup = empGroup;
	}

	public boolean isSmsSend() {
		return smsSend;
	}

	public void setSmsSend(boolean smsSend) {
		this.smsSend = smsSend;
	}

	public boolean isEmailSend() {
		return emailSend;
	}

	public void setEmailSend(boolean emailSend) {
		this.emailSend = emailSend;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
