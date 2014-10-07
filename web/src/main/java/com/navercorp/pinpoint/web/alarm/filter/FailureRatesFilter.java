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

/**
 * 
 * @author koo.taejin
 */
public class FailureRatesFilter extends AlarmCheckRatesFilter {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Application application;

	public FailureRatesFilter(Application application) {
		super(null, null);
		this.application = application;
	}
	
	public void check() {
	}
	
//	@Override
	public boolean check(AlarmEvent event) {
		logger.debug("{} check.", this.getClass().getSimpleName());
		
//		MapStatisticsCallerDao dao = event.getMapStatisticsCallerDao();
		MapStatisticsCallerDao dao = null;
		
		if (dao == null) {
			logger.warn("{} object is null.", MapStatisticsCallerDao.class.getSimpleName());
			return false;
		}
		
		long startEventTimeMillis = event.getEventStartTimeMillis();

		// 이것도 CheckTImeMillis의 시간이 길면 나누어야 함
		// 추가적으로 시간이 범위에 있는 만큼 다 안들어 오면 ??
		// 일단은 아주 단순하게
//		int continuationTime = getRule().getContinuosTime();
		int continuationTime = 300000;
		Range range = Range.createUncheckedRange(startEventTimeMillis - continuationTime, startEventTimeMillis);

		LinkDataMap linkDataMap = dao.selectCaller(application, range);
		for (LinkData linkData : linkDataMap.getLinkDataList()) {
			Application toApplication = linkData.getToApplication();
//			if (toApplication.getServiceType().isTerminal() || toApplication.getServiceType().isUnknown()) {
//				logger.debug("Application({}) is invalid serviceType. this is skip.", toApplication.getName());
//				continue;
//			}

			AgentHistogramList sourceList = linkData.getSourceList();
			Collection<AgentHistogram> agentHistogramList = sourceList.getAgentHistogramList();

			boolean isSatisFied = checkRates(toApplication, agentHistogramList);
			if (isSatisFied) {
				return true;
			}
		}

		return false;
	}
	
	private boolean checkRates(Application toApplication, Collection<AgentHistogram> agentHistogramList) {
		long totalCount = 0;
		long successCount = 0;
		long slowCount = 0;
		long errorCount = 0;

		for (AgentHistogram agent : agentHistogramList) {
			for (TimeHistogram time : agent.getTimeHistogram()) {
				totalCount += time.getTotalCount();
				successCount += time.getSuccessCount();
				slowCount += time.getSlowCount();
				errorCount = time.getErrorCount();
			}
		}
		logger.info("{} -> {} {}/{}(slow={}, error={})", application.getName(), toApplication.getName(), successCount, totalCount, slowCount, errorCount);

		return check(errorCount, totalCount);
	}

	@Override
	protected long getDetectedValue() {
		// TODO Auto-generated method stub
		return 0;
	}
}
