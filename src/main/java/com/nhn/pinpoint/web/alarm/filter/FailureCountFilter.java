package com.nhn.pinpoint.web.alarm.filter;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.web.alarm.AlarmEvent;
import com.nhn.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.AgentHistogram;
import com.nhn.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkData;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.nhn.pinpoint.web.dao.MapStatisticsCallerDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.Range;

public class FailureCountFilter extends AlarmCheckCountFilter  {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Application application;

	public FailureCountFilter(Application application) {
		this.application = application;
	}
	
	
	@Override
	protected boolean check(AlarmEvent event) {
		logger.debug("{} check.", this.getClass().getSimpleName());
		
		MapStatisticsCallerDao dao = event.getMapStatisticsCallerDao();
		if (dao == null) {
			logger.warn("{} object is null.", MapStatisticsCallerDao.class.getSimpleName());
			return false;
		}
		
		long startEventTimeMillis = event.getEventStartTimeMillis();

		// 이것도 CheckTImeMillis의 시간이 길면 나누어야 함
		// 추가적으로 시간이 범위에 있는 만큼 다 안들어 오면 ??
		// 일단은 아주 단순하게
		int continuationTime = getRule().getContinuosTime();
		Range range = Range.createUncheckedRange(startEventTimeMillis - continuationTime, startEventTimeMillis);

		LinkDataMap linkDataMap = dao.selectCaller(application, range);
		for (LinkData linkData : linkDataMap.getLinkDataList()) {
			Application toApplication = linkData.getToApplication();
			if (toApplication.getServiceType().isTerminal() || toApplication.getServiceType().isUnknown()) {
				logger.debug("Application({}) is invalid serviceType. this is skip.", toApplication.getName());
				continue;
			}

			AgentHistogramList sourceList = linkData.getSourceList();
			Collection<AgentHistogram> agentHistogramList = sourceList.getAgentHistogramList();

			boolean isSatisFied = checkCounts(toApplication, agentHistogramList);
			if (isSatisFied) {
				return true;
			}
		}

		return false;
	}

	private boolean checkCounts(Application toApplication, Collection<AgentHistogram> agentHistogramList) {
		long totalCount = 0;
		long successCount = 0;
		long errorCount = 0;

		for (AgentHistogram agent : agentHistogramList) {
			for (TimeHistogram time : agent.getTimeHistogram()) {
				totalCount += time.getTotalCount();
				successCount += time.getSuccessCount();
				errorCount = time.getErrorCount();
			}
		}
		logger.info("{} -> {} {}/{}(error={})", application.getName(), toApplication.getName(), successCount, totalCount, errorCount);

		return check(errorCount);
	}

}
