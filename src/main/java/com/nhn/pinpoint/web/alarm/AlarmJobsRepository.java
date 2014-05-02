package com.nhn.pinpoint.web.alarm;

import java.util.List;

import com.nhn.pinpoint.web.vo.Application;

public interface AlarmJobsRepository {

	List<AlarmJob> getAlarmJob(Application application);
	
	List<Application> getRegistedApplicationList();
	
}
