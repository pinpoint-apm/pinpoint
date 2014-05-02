package com.nhn.pinpoint.web.alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.MapUtils;

import com.nhn.pinpoint.web.vo.Application;

public class DefaultAlarmJobsRepository implements AlarmJobsRepository {

	private final Map<Application, List<AlarmJob>> repository = new HashMap<Application, List<AlarmJob>>();

	public DefaultAlarmJobsRepository() {
	}

	public List<AlarmJob> addAlarmJob(Application application, AlarmJob job) {
		List<AlarmJob> registedJobs = (List<AlarmJob>) MapUtils.getObject(repository, application, new ArrayList<AlarmJob>());
		registedJobs.add(job);

		repository.put(application, registedJobs);

		return registedJobs;
	}

	public List<AlarmJob> getAlarmJob(Application application) {
		List<AlarmJob> alarmJobList = repository.get(application);

		if (alarmJobList == null || alarmJobList.size() == 0) {
			return Collections.EMPTY_LIST;
		}

		List<AlarmJob> shallowCopyResult = new ArrayList<AlarmJob>();
		shallowCopyResult.addAll(alarmJobList);
		return shallowCopyResult;
	}

	public List<Application> getRegistedApplicationList() {
		Set<Application> keyList = repository.keySet();

		if (keyList == null || keyList.size() == 0) {
			return Collections.EMPTY_LIST;
		}

		List<Application> shallowCopyResult = new ArrayList<Application>();
		shallowCopyResult.addAll(keyList);
		return shallowCopyResult;
	}

}
