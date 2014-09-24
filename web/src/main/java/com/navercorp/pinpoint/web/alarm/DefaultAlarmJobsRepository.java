package com.nhn.pinpoint.web.alarm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.nhn.pinpoint.web.vo.Application;

public class DefaultAlarmJobsRepository implements AlarmJobsRepository {

	private final Map<Application, List<AlarmJob>> repository = new HashMap<Application, List<AlarmJob>>();

	public DefaultAlarmJobsRepository() {
	}
	
	@Override
	public int getTotalJobCount() {
		int totalCount = 0;
		
		Collection<List<AlarmJob>> values = repository.values();
		for (List<AlarmJob> value : values) {
			totalCount += value.size();
		}
		
		return totalCount;
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
			return Collections.emptyList();
		}

		List<AlarmJob> shallowCopyResult = new ArrayList<AlarmJob>();
		shallowCopyResult.addAll(alarmJobList);
		return shallowCopyResult;
	}

	public List<Application> getRegistedApplicationList() {
		Set<Application> keyList = repository.keySet();

        if (CollectionUtils.isEmpty(keyList)) {
			return Collections.emptyList();
		}

		List<Application> shallowCopyResult = new ArrayList<Application>();
		shallowCopyResult.addAll(keyList);
		return shallowCopyResult;
	}

}
