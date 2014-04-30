package com.nhn.pinpoint.web.dao;

import java.util.List;

import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;


public interface AlarmRuleInfoDao {

	public int selectAlarmCount();
	
	public List<AlarmRuleResource> selectAlarmRuleList();
	
}
