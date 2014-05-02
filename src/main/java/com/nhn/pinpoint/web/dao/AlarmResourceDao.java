package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.alarm.vo.AlarmContactGroupResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmContactResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleGroupResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;


public interface AlarmResourceDao {

	public int selectAlarmCount();

	public List<AlarmResource> selectAlarmList();

	public List<AlarmRuleResource> selectAlarmRuleList();
	
	public List<AlarmRuleGroupResource> selectAlarmRuleGroupList();
	
	public List<AlarmContactResource> selectAlarmContactList();
	
	public List<AlarmContactGroupResource> selectAlarmContactGroupList();
	
	
}
