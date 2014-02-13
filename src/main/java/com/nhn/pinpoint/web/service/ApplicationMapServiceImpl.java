package com.nhn.pinpoint.web.service;

import java.util.*;
import java.util.Map.Entry;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.nhn.pinpoint.web.applicationmap.Link;
import com.nhn.pinpoint.web.applicationmap.rawdata.*;
import com.nhn.pinpoint.web.dao.*;
import com.nhn.pinpoint.web.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;

/**
 * 
 * @author netspider
 */
@Service
public class ApplicationMapServiceImpl implements ApplicationMapService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private AgentInfoDao agentInfoDao;

    @Autowired
    private MapResponseDao mapResponseDao;

	@Autowired
	private ApplicationMapStatisticsCallerDao applicationMapStatisticsCallerDao;

	@Autowired
	private ApplicationMapStatisticsCalleeDao applicationMapStatisticsCalleeDao;

	@Autowired
	private HostApplicationMapDao hostApplicationMapDao;

	private Set<AgentInfoBo> selectAgents(String applicationId) {
        if (applicationId == null) {
            throw new NullPointerException("applicationId must not be null");
        }

        String[] agentIds = applicationIndexDao.selectAgentIds(applicationId);
		Set<AgentInfoBo> agentSet = new HashSet<AgentInfoBo>();
		for (String agentId : agentIds) {
			// TODO 조회 시간대에 따라서 agent info row timestamp를 변경하여 조회해야하는지는 모르겠음.
			AgentInfoBo info = agentInfoDao.findAgentInfoBeforeStartTime(agentId, System.currentTimeMillis());
			agentSet.add(info);
		}
		return agentSet;
	}

	/**
	 * callerApplicationName이 호출한 callee를 모두 조회
	 * 
	 * @param callerApplication
	 * @param range
	 * @param calleeFoundApplications
	 * @param callerFoundApplications
	 * @return
	 */
	private Set<LinkStatistics> selectCallee(Application callerApplication, Range range, Set<Application> calleeFoundApplications, Set<Application> callerFoundApplications) {
		// 이미 조회된 구간이면 skip
        if (calleeFoundApplications.contains(callerApplication)) {
			logger.debug("ApplicationStatistics exists. Skip finding callee. {} ", callerApplication);
			return new HashSet<LinkStatistics>(0);
		}

        calleeFoundApplications.add(callerApplication);
        if (logger.isDebugEnabled()) {
		    logger.debug("Finding Callee. caller={}", callerApplication);
        }

		List<LinkStatistics> callee = applicationMapStatisticsCalleeDao.selectCallee(callerApplication, range);
        if (logger.isDebugEnabled()) {
		    logger.debug("Found Callee. count={}, caller={}", callee.size(), callerApplication);
        }

        final Set<LinkStatistics> calleeSet = new HashSet<LinkStatistics>();
		for (LinkStatistics stat : callee) {
			final boolean replaced = replaceApplicationInfo(stat, range);

			// replaced된 녀석은 CLIENT이기 때문에 callee검색용도로만 사용하고 map에 추가하지 않는다.
			if (!replaced) {
				fillAdditionalInfo(stat, range);
				calleeSet.add(stat);
			}

			// terminal, unknowncloud 인 경우에는 skip
			if (stat.getToServiceType().isTerminal() || stat.getToServiceType().isUnknown()) {
				continue;
			}

			logger.debug("     Find subCallee of {}", stat.getToApplication());
            Set<LinkStatistics> calleeSub = selectCallee(stat.getToApplication(), range, calleeFoundApplications, callerFoundApplications);
			logger.debug("     Found subCallee. count={}, caller={}", calleeSub.size(), stat.getToApplication());

			calleeSet.addAll(calleeSub);

			// 찾아진 녀석들에 대한 caller도 찾는다.
			for (LinkStatistics eachCallee : calleeSub) {
				logger.debug("     Find caller of {}", eachCallee.getFromApplication());
				Set<LinkStatistics> callerSub = selectCaller(eachCallee.getFromApplication(), range, calleeFoundApplications, callerFoundApplications);
				logger.debug("     Found subCaller. count={}, callee={}", callerSub.size(), eachCallee.getFromApplication());
				calleeSet.addAll(callerSub);
			}
		}

		return calleeSet;
	}

	/**
	 * callee applicationname을 호출한 caller 조회.
	 * 
	 * @param calleeApplication
	 * @param range
	 * @return
	 */
	private Set<LinkStatistics> selectCaller(Application calleeApplication, Range range, Set<Application> calleeFoundApplications, Set<Application> callerFoundApplications) {
		// 이미 조회된 구간이면 skip

        if (callerFoundApplications.contains(calleeApplication)) {
			logger.debug("ApplicationStatistics exists. Skip finding caller. {}", calleeApplication);
			return new HashSet<LinkStatistics>(0);
		}
		callerFoundApplications.add(calleeApplication);
        if (logger.isDebugEnabled()) {
		    logger.debug("Finding Caller. callee={}", calleeApplication);
        }

		final List<LinkStatistics> caller = applicationMapStatisticsCallerDao.selectCaller(calleeApplication, range);
		logger.debug("Found Caller. count={}, callee={}", caller.size(), calleeApplication);

        final Set<LinkStatistics> callerSet = new HashSet<LinkStatistics>();
		for (LinkStatistics stat : caller) {
			fillAdditionalInfo(stat, range);
			callerSet.add(stat);

			// 나를 부른 application을 찾아야 하기 떄문에 to를 입력.
            Set<LinkStatistics> callerSub = selectCaller(stat.getFromApplication(), range, calleeFoundApplications, callerFoundApplications);
			callerSet.addAll(callerSub);

			// 찾아진 녀석들에 대한 callee도 찾는다.
			for (LinkStatistics eachCallee : callerSub) {
				// terminal이면 skip
				if (eachCallee.getToServiceType().isTerminal() || eachCallee.getToServiceType().isUnknown()) {
					continue;
				}
				Set<LinkStatistics> calleeSub = selectCallee(eachCallee.getToApplication(), range, calleeFoundApplications, callerFoundApplications);
				callerSet.addAll(calleeSub);
			}
		}

		return callerSet;
	}

	private boolean replaceApplicationInfo(LinkStatistics stat, Range range) {
		// rpc client의 목적지가 agent가 설치되어 application name이 존재한다면 replace.
		if (stat.getToServiceType().isRpcClient()) {
			logger.debug("Find applicationName {} {}", stat.getToApplication(), range);
			final Application app = hostApplicationMapDao.findApplicationName(stat.getTo(), range);
			if (app != null) {
				logger.debug("Application info replaced. {} => {}", stat, app);

                stat.setToApplication(new Application(app.getName(), app.getServiceType()));
				return true;
			} else {
                Application unknown = new Application(stat.getTo(), ServiceType.UNKNOWN);
                stat.setToApplication(unknown);
			}
		}
		return false;
	}

	private void fillAdditionalInfo(LinkStatistics stat, Range range) {
		if (stat.getToServiceType().isTerminal() || stat.getToServiceType().isUnknown()) {
			return;
		}

		Set<AgentInfoBo> agentSet = selectAgents(stat.getToApplication().getName());

		if (agentSet.isEmpty()) {
			return;
		}

		// destination이 WAS이고 agent가 설치되어있으면 agentSet이 존재한다.
		stat.addToAgentSet(agentSet);

		logger.debug("Fill agent info. {}, {}", stat.getToApplication().getName(), agentSet);
	}

	/**
	 * 메인화면에서 사용. 시간별로 TimeSlot을 조회하여 서버 맵을 그릴 때 사용한다.
	 */
	@Override
	public ApplicationMap selectApplicationMap(Application sourceApplication, Range range) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        logger.debug("SelectApplicationMap");

		StopWatch watch = new StopWatch("applicationMapWatch");
		watch.start();

        // 무한 탐색을 방지하기 위한 용도.
		final Set<Application> callerFoundApplications = new HashSet<Application>();
		final Set<Application> calleeFoundApplications = new HashSet<Application>();
		Set<LinkStatistics> callee = selectCallee(sourceApplication, range, calleeFoundApplications, callerFoundApplications);
		logger.debug("Result of finding callee {}", callee);

		Set<LinkStatistics> caller = selectCaller(sourceApplication, range, calleeFoundApplications, callerFoundApplications);
		logger.debug("Result of finding caller {}", caller);

		Set<LinkStatistics> data = new HashSet<LinkStatistics>(callee.size() + caller.size());
		data.addAll(callee);
		data.addAll(caller);

		ApplicationMap map = new ApplicationMapBuilder().build(new ArrayList<LinkStatistics>(data));
        map.appendResponseTime(range, this.mapResponseDao);

		watch.stop();
		logger.info("Fetch applicationmap elapsed. {}ms", watch.getLastTaskTimeMillis());

		return map;
	}



    @Override
	public LoadFactor linkStatistics(Application sourceApplication, Application destinationApplication, Range range) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        if (destinationApplication == null) {
            throw new NullPointerException("destinationApplication must not be null");
        }

        List<Map<Long, Map<Short, Long>>> list;

		if (sourceApplication.getServiceType().isUser()){
			logger.debug("Find 'client -> any' link statistics");
			// client는 applicatinname + servicetype.client로 기록된다.
			// 그래서 src, dest가 둘 다 dest로 같음.
            Application userApplication = new Application(destinationApplication.getName(), sourceApplication.getServiceTypeCode());
			list = applicationMapStatisticsCalleeDao.selectCalleeStatistics(userApplication, destinationApplication, range);
		} else if (destinationApplication.getServiceType().isWas()) {
			logger.debug("Find 'any -> was' link statistics");
			// destination이 was인 경우에는 중간에 client event가 끼어있기 때문에 callee에서
			// caller가
			// 같은녀석을 찾아야 한다.
			list = applicationMapStatisticsCallerDao.selectCallerStatistics(sourceApplication, destinationApplication, range);
		} else {
			logger.debug("Find 'was -> terminal' link statistics");
			// 일반적으로 was -> terminal 간의 통계정보 조회.
			list = applicationMapStatisticsCalleeDao.selectCalleeStatistics(sourceApplication, destinationApplication, range);
		}

		LoadFactor loadFactor = new LoadFactor(range);

		// 조회가 안되는 histogram slot이 있으면 UI에 모두 보이지 않기 때문에 미리 정의된 slot을 모두 할당한다.
        HistogramSchema histogramSchema = destinationApplication.getServiceType().getHistogramSchema();
        loadFactor.setDefaultHistogramSlotList(histogramSchema);

		logger.debug("Fetched statistics data={}", list);

		for (Map<Long, Map<Short, Long>> map : list) {
			for (Entry<Long, Map<Short, Long>> entry : map.entrySet()) {
				long timestamp = entry.getKey();
				Map<Short, Long> histogramMap = entry.getValue();

				for (Entry<Short, Long> histogram : histogramMap.entrySet()) {
					if (histogram.getKey() == -1) {
						loadFactor.addSample(timestamp, histogram.getKey(), histogram.getValue(), true);
					} else {
						loadFactor.addSample(timestamp, histogram.getKey(), histogram.getValue(), false);
					}
				}
			}
		}
		return loadFactor;
	}
}
