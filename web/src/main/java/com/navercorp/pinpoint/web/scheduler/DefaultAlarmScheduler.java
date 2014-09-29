package com.nhn.pinpoint.web.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.nhn.pinpoint.web.alarm.AlarmEvent;
import com.nhn.pinpoint.web.alarm.AlarmJob;
import com.nhn.pinpoint.web.alarm.AlarmJobsRepository;
import com.nhn.pinpoint.web.alarm.DefaultAlarmEvent;
import com.nhn.pinpoint.web.alarm.DefaultAlarmJob;
import com.nhn.pinpoint.web.alarm.DefaultAlarmJobsRepository;
import com.nhn.pinpoint.web.alarm.MainCategory;
import com.nhn.pinpoint.web.alarm.SubCategory;
import com.nhn.pinpoint.web.alarm.filter.AlarmCheckFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmMailSendFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmSendFilter;
import com.nhn.pinpoint.web.alarm.filter.AlarmSmsSendFilter;
import com.nhn.pinpoint.web.alarm.resource.MailResource;
import com.nhn.pinpoint.web.alarm.resource.SmsResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmContactGroupResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmContactResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleGroupResource;
import com.nhn.pinpoint.web.alarm.vo.AlarmRuleResource;
import com.nhn.pinpoint.web.dao.AlarmResourceDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.dao.MapStatisticsCallerDao;
import com.nhn.pinpoint.web.vo.Application;

/**
 * 
 * @author koo.taejin
 */
public class DefaultAlarmScheduler implements AlarmScheduler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private AlarmJobsRepository repository;
	
	@Autowired
	MailResource mailResource;
	
	@Autowired
	SmsResource smsResource;

	@Autowired
	private AlarmResourceDao alarmResourceDao;

	@Autowired
	private ApplicationIndexDao applicationIndexDao;
	
	@Autowired
	private MapStatisticsCallerDao mapStatisticsCallerDao;

	@Override
	public void initialize() {
		logger.info("{} initialize.", this.getClass().getName());

		DefaultAlarmJobsRepository repository = new DefaultAlarmJobsRepository();

		List<Application> applicationList = applicationIndexDao.selectAllApplicationNames();
		List<AlarmResource> alarmResourceList = alarmResourceDao.selectAlarmList();

		for (AlarmResource alarmResource : alarmResourceList) {
			String agentId = alarmResource.getAgentId();

			Application application = findApplication(agentId, applicationList);
			if (application == null) {
				logger.warn("Can't find Application({}).", agentId);
				continue;
			}

			AlarmJob job = createAlarmJob(application, alarmResource);
			if (job == null) {
				continue;
			}
			
			repository.addAlarmJob(application, job);
		}
		
		logger.info("{} initilization success(Registred {} Jobs). ", this.getClass().getName(), repository.getTotalJobCount());
		
		synchronized (this) {
			this.repository = repository;
		}
	}

	@Override
	public void execute() {
		logger.info("{} execute.", this.getClass().getName());

		AlarmEvent event = createAlarmEvent();
		
		synchronized (this) {
			if (this.repository == null) {
				logger.warn("{}'s repository is null. this job will be skipped.", this.getClass().getName());
				return;
			}
			
			int totalJobCount = this.repository.getTotalJobCount();
			
			List<Application> applicationList = this.repository.getRegistedApplicationList();
			for (Application application : applicationList) {
				executeEachApplication(application, event, totalJobCount);
			}
		}
		
	}

	private Application findApplication(String applicationName, List<Application> applicationList) {
		for (Application application : applicationList) {
			if (application.getName().equalsIgnoreCase(applicationName)) {
				return application;
			}
		}

		return null;
	}

	private AlarmJob createAlarmJob(Application application, AlarmResource resource) {
		DefaultAlarmJob alarmJob = new DefaultAlarmJob(application);

		String applicationName = application.getName();
		String alarmName = resource.getAlarmGroupName();
		
		AlarmContactGroupResource contactGroup = resource.getAlarmContactGroup();
		if (contactGroup == null || CollectionUtils.isEmpty(contactGroup.getAlarmContactList())) {
			logger.warn("Application={}, Rule={} does not have contact resource.", applicationName, alarmName);
			return null;
		}
		List<AlarmContactResource> contactResourceList = contactGroup.getAlarmContactList();
		List<AlarmSendFilter> alarmSendFilterList = createAlarmSendFilter(application, contactResourceList);
		if (CollectionUtils.isEmpty(alarmSendFilterList)) {
			logger.warn("Application={}, Rule={} can't find valid contact resource.", applicationName, alarmName);
			return null;
		}
		alarmJob.addFilter(alarmSendFilterList);
		
		AlarmRuleGroupResource ruleGroup = resource.getAlarmRuleGroup();
		if (ruleGroup == null || CollectionUtils.isEmpty(ruleGroup.getAlarmRuleList())) {
			logger.warn("Application={}, Rule={} does not have rule resource.", applicationName, alarmName);
			return null;
		}
		List<AlarmRuleResource> alarmRuleList = ruleGroup.getAlarmRuleList();
		List<AlarmCheckFilter> alarmCheckFilterList = createAlarmCheckFilter(application, alarmRuleList);
		alarmJob.addFilter(alarmCheckFilterList);
		if (CollectionUtils.isEmpty(alarmCheckFilterList)) {
			logger.warn("Application={}, Rule={} can't find valid rule resource.", applicationName, alarmName);
			return null;
		}
		
		return alarmJob;
	}
	
	private List<AlarmSendFilter> createAlarmSendFilter(Application application, List<AlarmContactResource> contactResourceList) {
		List<String> phoneNumberList = new ArrayList<String>();
		List<String> emailAddressList = new ArrayList<String>();
		
		for (AlarmContactResource contactResource : contactResourceList) {
			String phoneNumber = contactResource.getPhoneNum();
			if (!StringUtils.isEmpty(phoneNumber)) {
				phoneNumberList.add(phoneNumber);
			}
			
			String emailAddress = contactResource.getEmailAddress();
			if (!StringUtils.isEmpty(emailAddress)) {
				emailAddressList.add(emailAddress);
			}
		}
		
		List<AlarmSendFilter> alarmSendFilter = new ArrayList<AlarmSendFilter>();
		
		if (!CollectionUtils.isEmpty(phoneNumberList)) {
			AlarmSendFilter smsSendFilter = new AlarmSmsSendFilter(application, smsResource, phoneNumberList);
			alarmSendFilter.add(smsSendFilter);
		}
		
		if (!CollectionUtils.isEmpty(emailAddressList)) {
			AlarmSendFilter emailSendFilter = new AlarmMailSendFilter(application, mailResource, emailAddressList);
			alarmSendFilter.add(emailSendFilter);
		}
		
		return alarmSendFilter;
	}
	
	private List<AlarmCheckFilter> createAlarmCheckFilter(Application application, List<AlarmRuleResource> ruleResourceList) {
		List<AlarmCheckFilter> alarmCheckFilterList = new ArrayList<AlarmCheckFilter>();
		
		for (AlarmRuleResource ruleResource : ruleResourceList) {
			MainCategory mainCategory = ruleResource.getMainCategory();
			SubCategory subCategory = ruleResource.getSubCategory();
		
			AlarmCheckFilter alarmCheckFilter = subCategory.createAlarmFilter(application, mainCategory, ruleResource);
			if (alarmCheckFilter == null) {
				logger.warn("{}({}) can't build Filter.", ruleResource.getAlarmRuleName(), ruleResource.getId());
				continue;
			}
			alarmCheckFilterList.add(alarmCheckFilter);
		}
		
		return alarmCheckFilterList;
	}

	private AlarmEvent createAlarmEvent() {
//		DefaultAlarmEvent event = new DefaultAlarmEvent(System.currentTimeMillis(), mapStatisticsCallerDao);
		DefaultAlarmEvent event = null;
		return event;
	}
	
	private void executeEachApplication(Application application, AlarmEvent event, int totalJobCount) {
		List<AlarmJob> jobList = repository.getAlarmJob(application);
		AtomicInteger index = new AtomicInteger(1);
		for (AlarmJob job : jobList) {
			logger.debug("{} ({}/{}) jobs start(TotalJobCount={}).", application.getName(), index.getAndIncrement(), jobList.size(), totalJobCount);
			job.execute(event);
		}
	}

}
