package com.nhn.pinpoint.web.service;

import java.util.*;
import java.util.Map.Entry;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.web.applicationmap.ApplicationMapBuilder;
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
public class MapServiceImpl implements MapService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private AgentInfoDao agentInfoDao;

    @Autowired
    private MapResponseDao mapResponseDao;

	@Autowired
	private MapStatisticsCalleeDao mapStatisticsCalleeDao;

	@Autowired
	private MapStatisticsCallerDao mapStatisticsCallerDao;

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
	 * @param callerFound
	 * @param calleeFound
	 * @return
	 */
	private Set<LinkStatistics> selectCaller(Application callerApplication, Range range, Set<Application> callerFound, Set<Application> calleeFound) {
		// 이미 조회된 구간이면 skip
        if (callerFound.contains(callerApplication)) {
			logger.debug("ApplicationStatistics exists. Skip finding caller. {} ", callerApplication);
			return new HashSet<LinkStatistics>(0);
		}

        callerFound.add(callerApplication);
        if (logger.isDebugEnabled()) {
		    logger.debug("Finding Caller. caller={}", callerApplication);
        }

		List<LinkStatistics> caller = mapStatisticsCallerDao.selectCaller(callerApplication, range);
        if (logger.isDebugEnabled()) {
		    logger.debug("Found Caller. count={}, caller={}", caller.size(), callerApplication);
        }

        final Set<LinkStatistics> callerSet = new HashSet<LinkStatistics>();
		for (LinkStatistics stat : caller) {
			final boolean replaced = replaceApplicationInfo(stat, range);

			// replaced된 녀석은 CLIENT이기 때문에 callee검색용도로만 사용하고 map에 추가하지 않는다.
			if (!replaced) {
				fillAdditionalInfo(stat, range);
				callerSet.add(stat);
			}

			// terminal, unknowncloud 인 경우에는 skip
			if (stat.getToServiceType().isTerminal() || stat.getToServiceType().isUnknown()) {
				continue;
			}

			logger.debug("     Find subCaller of {}", stat.getToApplication());
            Set<LinkStatistics> callerSub = selectCaller(stat.getToApplication(), range, callerFound, calleeFound);
			logger.debug("     Found subCaller. count={}, caller={}", callerSub.size(), stat.getToApplication());

			callerSet.addAll(callerSub);

			// 찾아진 녀석들에 대한 caller도 찾는다.
			for (LinkStatistics eachCaller : callerSub) {
				logger.debug("     Find callee of {}", eachCaller.getFromApplication());
				Set<LinkStatistics> calleeSub = selectCallee(eachCaller.getFromApplication(), range, callerFound, calleeFound);
				logger.debug("     Found subCallee. count={}, callee={}", calleeSub.size(), eachCaller.getFromApplication());
				callerSet.addAll(calleeSub);
			}
		}

		return callerSet;
	}

	/**
	 * callee applicationname을 호출한 caller 조회.
	 * 
	 * @param calleeApplication
	 * @param range
	 * @return
	 */
	private Set<LinkStatistics> selectCallee(Application calleeApplication, Range range, Set<Application> callerFound, Set<Application> calleeFound) {
		// 이미 조회된 구간이면 skip

        if (calleeFound.contains(calleeApplication)) {
			logger.debug("ApplicationStatistics exists. Skip finding callee. {}", calleeApplication);
			return new HashSet<LinkStatistics>(0);
		}
		calleeFound.add(calleeApplication);
        if (logger.isDebugEnabled()) {
		    logger.debug("Finding Callee. callee={}", calleeApplication);
        }

		final List<LinkStatistics> callee = mapStatisticsCalleeDao.selectCallee(calleeApplication, range);
		logger.debug("Found Callee. count={}, callee={}", callee.size(), calleeApplication);

        final Set<LinkStatistics> calleeSet = new HashSet<LinkStatistics>();
		for (LinkStatistics stat : callee) {
			fillAdditionalInfo(stat, range);
			calleeSet.add(stat);

			// 나를 부른 application을 찾아야 하기 떄문에 to를 입력.
            Set<LinkStatistics> calleeSub = selectCallee(stat.getFromApplication(), range, callerFound, calleeFound);
			calleeSet.addAll(calleeSub);

			// 찾아진 녀석들에 대한 callee도 찾는다.
			for (LinkStatistics eachCallee : calleeSub) {
				// terminal이면 skip
				if (eachCallee.getToServiceType().isTerminal() || eachCallee.getToServiceType().isUnknown()) {
					continue;
				}
				Set<LinkStatistics> callerSub = selectCaller(eachCallee.getToApplication(), range, callerFound, calleeFound);
				calleeSet.addAll(callerSub);
			}
		}

		return calleeSet;
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
		final Set<Application> calleeFound = new HashSet<Application>();
		final Set<Application> callerFound = new HashSet<Application>();
		Set<LinkStatistics> caller = selectCaller(sourceApplication, range, callerFound, calleeFound);
		logger.debug("Result of finding caller {}", caller);

		Set<LinkStatistics> callee = selectCallee(sourceApplication, range, callerFound, calleeFound);
		logger.debug("Result of finding callee {}", callee);

		Set<LinkStatistics> data = new HashSet<LinkStatistics>(caller.size() + callee.size());
		data.addAll(caller);
		data.addAll(callee);

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
			list = mapStatisticsCallerDao.selectCallerStatistics(userApplication, destinationApplication, range);
		} else if (destinationApplication.getServiceType().isWas()) {
			logger.debug("Find 'any -> was' link statistics");
			// destination이 was인 경우에는 중간에 client event가 끼어있기 때문에 callee에서
			// caller가
			// 같은녀석을 찾아야 한다.
			list = mapStatisticsCalleeDao.selectCalleeStatistics(sourceApplication, destinationApplication, range);
		} else {
			logger.debug("Find 'was -> terminal' link statistics");
			// 일반적으로 was -> terminal 간의 통계정보 조회.
			list = mapStatisticsCallerDao.selectCallerStatistics(sourceApplication, destinationApplication, range);
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
