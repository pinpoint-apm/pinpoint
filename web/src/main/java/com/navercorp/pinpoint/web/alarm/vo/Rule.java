/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.alarm.vo;

import org.apache.ibatis.type.Alias;

/**
 * @author minwoo.jung
 */
@Alias(value = "rule")
public class Rule {

    private String i    ;
	private String applicati    nId;
	private String Chec    erName;
	private Integer    threshold;
	private St    ing empGroup;
	private    boolean smsSend;
	privat     boolean emailSend;       	private Str          ng notes;
	
	public Rule() {
	}
	
	public Rule(String applicationId, String checkerName, Integer Threshold, String empGroup, boolean smsSe       d, boolean emailSend, String no       es) {
		this.applicationId         applicationId;
		this.       heckerName = checkerN       me;
		this.threshol        = Threshold;
		this.em       Group = empGrou          ;
		this.smsSend = smsSend;
		t       is.emailSend = em          ilSend;
		this.notes = notes;
	}
	
	public String       getApplicationId() {
		return a          plicationId;
	}
	
	public voi        setApplication          d(String applicationId) {
		this.applicationI        = applicationId;
	}
	          	public String getCheckerNam       () {
		return          CheckerName;
	}
	
	public void setCheckerN       me(String checkerName)
		CheckerName = checkerNa       e;
	}
	
	pub          ic Integer getThreshold() {
		return th       eshold;
	}
	
	public         id setThreshold(Integer th       eshold) {
	        his.threshold = threshold;
	}
	
	public       String getEmpGroup(        {
		return empGroup;
	}
	
	p       blic void set        pGroup(String empGroup) {
		this.empGroup =       empGroup;
	}

	public b        lean isSmsSend() {
		       eturn         sSend;
	}

	public void setS       sSend(boo    ean smsSend) {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
