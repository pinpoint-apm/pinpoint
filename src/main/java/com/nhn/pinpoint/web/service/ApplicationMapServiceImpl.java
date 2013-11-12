package com.nhn.pinpoint.web.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
import com.nhn.pinpoint.web.dao.AgentInfoDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.web.dao.ApplicationMapStatisticsCallerDao;
import com.nhn.pinpoint.web.dao.HostApplicationMapDao;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.LinkStatistics;

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
	 * @param callerApplicationName
	 * @param callerServiceType
	 * @param from
	 * @param to
	 * @param calleeFoundApplications
	 * @param callerFoundApplications
	 * @return
	 */
	private Set<TransactionFlowStatistics> selectCallee(String callerApplicationName, short callerServiceType, long from, long to, Set<Node> calleeFoundApplications, Set<Node> callerFoundApplications) {
		// 이미 조회된 구간이면 skip
        final Node key = new Node(callerApplicationName, ServiceType.findServiceType(callerServiceType));
        if (calleeFoundApplications.contains(key)) {
			logger.debug("ApplicationStatistics exists. Skip finding callee. {} {} ", callerApplicationName, callerServiceType);
			return new HashSet<TransactionFlowStatistics>(0);
		}
		calleeFoundApplications.add(key);
        if (logger.isDebugEnabled()) {
		    logger.debug("Find Callee. caller={}, serviceType={}", callerApplicationName, ServiceType.findServiceType(callerServiceType));
        }

		final Set<TransactionFlowStatistics> calleeSet = new HashSet<TransactionFlowStatistics>();

		Map<String, TransactionFlowStatistics> callee = applicationMapStatisticsCalleeDao.selectCallee(callerApplicationName, callerServiceType, from, to);

        if (logger.isDebugEnabled()) {
		    logger.debug("     Found Callee. count={}, caller={}", callee.size(), callerApplicationName);
        }

		for (Entry<String, TransactionFlowStatistics> entry : callee.entrySet()) {
			boolean replaced = replaceApplicationInfo(entry.getValue(), from, to);

			// replaced된 녀석은 CLIENT이기 때문에 callee검색용도로만 사용하고 map에 추가하지 않는다.
			if (!replaced) {
				fillAdditionalInfo(entry.getValue(), from, to);
				calleeSet.add(entry.getValue());
			}

			// terminal, unknowncloud 인 경우에는 skip
			if (entry.getValue().getToServiceType().isTerminal() || entry.getValue().getToServiceType().isUnknown()) {
				continue;
			}

			TransactionFlowStatistics stat = entry.getValue();

			logger.debug("     Find subCallee of {}", stat.getTo());
			Set<TransactionFlowStatistics> calleeSub = selectCallee(stat.getTo(), stat.getToServiceType().getCode(), from, to, calleeFoundApplications, callerFoundApplications);
			logger.debug("     Found subCallee. count={}, caller={}", calleeSub.size(), stat.getTo());

			calleeSet.addAll(calleeSub);

			// 찾아진 녀석들에 대한 caller도 찾는다.
			for (TransactionFlowStatistics eachCallee : calleeSub) {
				logger.debug("     Find caller of {}", eachCallee.getFrom());
				Set<TransactionFlowStatistics> callerSub = selectCaller(eachCallee.getFrom(), eachCallee.getFromServiceType().getCode(), from, to, calleeFoundApplications, callerFoundApplications);
				logger.debug("     Found subCaller. count={}, callee={}", callerSub.size(), eachCallee.getFrom());
				calleeSet.addAll(callerSub);
			}
		}

		return calleeSet;
	}

	/**
	 * callee applicationname을 호출한 caller 조회.
	 * 
	 * @param calleeApplicationName
	 * @param from
	 * @param to
	 * @return
	 */
	private Set<TransactionFlowStatistics> selectCaller(String calleeApplicationName, short calleeServiceType, long from, long to, Set<Node> calleeFoundApplications, Set<Node> callerFoundApplications) {
		// 이미 조회된 구간이면 skip
        final Node key = new Node(calleeApplicationName, ServiceType.findServiceType(calleeServiceType));
        if (callerFoundApplications.contains(key)) {
			logger.debug("ApplicationStatistics exists. Skip finding caller. {} {}", calleeApplicationName, calleeServiceType);
			return new HashSet<TransactionFlowStatistics>(0);
		}
		callerFoundApplications.add(key);
        if (logger.isDebugEnabled()) {
		    logger.debug("Find Caller. callee={}, serviceType={}" , calleeApplicationName, ServiceType.findServiceType(calleeServiceType));
        }

		final Set<TransactionFlowStatistics> callerSet = new HashSet<TransactionFlowStatistics>();

		Map<String, TransactionFlowStatistics> caller = applicationMapStatisticsCallerDao.selectCaller(calleeApplicationName, calleeServiceType, from, to);

		logger.debug("     Found Caller. count={}, callee={}", caller.size(), calleeApplicationName);

		for (Entry<String, TransactionFlowStatistics> entry : caller.entrySet()) {
			fillAdditionalInfo(entry.getValue(), from, to);
			callerSet.add(entry.getValue());

			TransactionFlowStatistics stat = entry.getValue();

			// 나를 부른 application을 찾아야 하기 떄문에 to를 입력.
			Set<TransactionFlowStatistics> callerSub = selectCaller(stat.getFrom(), stat.getFromServiceType().getCode(), from, to, calleeFoundApplications, callerFoundApplications);
			callerSet.addAll(callerSub);

			// 찾아진 녀석들에 대한 callee도 찾는다.
			for (TransactionFlowStatistics eachCallee : callerSub) {
				// terminal이면 skip
				if (eachCallee.getToServiceType().isTerminal() || eachCallee.getToServiceType().isUnknown()) {
					continue;
				}
				Set<TransactionFlowStatistics> calleeSub = selectCallee(eachCallee.getTo(), eachCallee.getToServiceType().getCode(), from, to, calleeFoundApplications, callerFoundApplications);
				callerSet.addAll(calleeSub);
			}
		}

		return callerSet;
	}

	private boolean replaceApplicationInfo(TransactionFlowStatistics stat, long from, long to) {
		// rpc client의 목적지가 agent가 설치되어 application name이 존재한다면 replace.
		if (stat.getToServiceType().isRpcClient()) {
			logger.debug("Find applicationName {} {} {}", stat.getTo(), from, to);
			Application app = hostApplicationMapDao.findApplicationName(stat.getTo(), from, to);
			if (app != null) {
				logger.debug("Application info replaced. {} => {}", stat, app);

				stat.setTo(app.getApplicationName());
				stat.setToServiceType(app.getServiceType());
				return true;
			} else {
				stat.setToServiceType(ServiceType.UNKNOWN_CLOUD);
			}
		}
		return false;
	}

	private void fillAdditionalInfo(TransactionFlowStatistics stat, long from, long to) {
		if (stat.getToServiceType().isTerminal() || stat.getToServiceType().isUnknown()) {
			return;
		}

		Set<AgentInfoBo> agentSet = selectAgents(stat.getTo());

		if (agentSet.isEmpty()) {
			return;
		}

		// destination이 WAS이고 agent가 설치되어있으면 agentSet이 존재한다.
		stat.addToAgentSet(agentSet);

		logger.debug("fill agent info. {}, {}", stat.getTo(), agentSet);
	}

	/**
	 * 메인화면에서 사용. 시간별로 TimeSlot을 조회하여 서버 맵을 그릴 때 사용한다.
	 */
	@Override
	public ApplicationMap selectApplicationMap(String applicationName, short serviceType, long from, long to) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        logger.debug("SelectApplicationMap");

		StopWatch watch = new StopWatch("applicationMapWatch");
		watch.start();

		// 무한 탐색을 방지하기 위한 용도.
		final Set<Node> callerFoundApplications = new HashSet<Node>();
		final Set<Node> calleeFoundApplications = new HashSet<Node>();

		Set<TransactionFlowStatistics> callee = selectCallee(applicationName, serviceType, from, to, calleeFoundApplications, callerFoundApplications);
		Set<TransactionFlowStatistics> caller = selectCaller(applicationName, serviceType, from, to, calleeFoundApplications, callerFoundApplications);

		Set<TransactionFlowStatistics> data = new HashSet<TransactionFlowStatistics>(callee.size() + caller.size());
		data.addAll(callee);
		data.addAll(caller);

		ApplicationMap map = new ApplicationMap(data).build();

		watch.stop();
		logger.info("Fetch applicationmap elapsed. {}ms", watch.getLastTaskTimeMillis());

		return map;
	}
	
	@Override
	public LinkStatistics linkStatistics(long from, long to, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType) {
        if (srcApplicationName == null) {
            throw new NullPointerException("srcApplicationName must not be null");
        }
        if (destApplicationName == null) {
            throw new NullPointerException("destApplicationName must not be null");
        }

        List<Map<Long, Map<Short, Long>>> list;

		if (ServiceType.findServiceType(srcServiceType) == ServiceType.CLIENT) {
			logger.debug("Find 'client -> any' link statistics");
			// client는 applicatinname + servicetype.client로 기록된다.
			// 그래서 src, dest가 둘 다 dest로 같음.
			list = applicationMapStatisticsCalleeDao.selectCalleeStatistics(destApplicationName, srcServiceType, destApplicationName, destServiceType, from, to);
		} else if (ServiceType.findServiceType(destServiceType).isWas()) {
			logger.debug("Find 'any -> was' link statistics");
			// destination이 was인 경우에는 중간에 client event가 끼어있기 때문에 callee에서
			// caller가
			// 같은녀석을 찾아야 한다.
			list = applicationMapStatisticsCallerDao.selectCallerStatistics(srcApplicationName, srcServiceType, destApplicationName, destServiceType, from, to);
		} else {
			logger.debug("Find 'was -> terminal' link statistics");
			// 일반적으로 was -> terminal 간의 통계정보 조회.
			list = applicationMapStatisticsCalleeDao.selectCalleeStatistics(srcApplicationName, srcServiceType, destApplicationName, destServiceType, from, to);
		}

		LinkStatistics statistics = new LinkStatistics(from, to);

		// 조회가 안되는 histogram slot이 있으면 UI에 모두 보이지 않기 때문에 미리 정의된 slot을 모두 할당한다.
		statistics.setDefaultHistogramSlotList(ServiceType.findServiceType(destServiceType).getHistogram().getHistogramSlotList());

		logger.debug("Fetched statistics data={}", list);

		for (Map<Long, Map<Short, Long>> map : list) {
			for (Entry<Long, Map<Short, Long>> entry : map.entrySet()) {
				long timestamp = entry.getKey();
				Map<Short, Long> histogramMap = entry.getValue();

				for (Entry<Short, Long> histogram : histogramMap.entrySet()) {
					if (histogram.getKey() == -1) {
						statistics.addSample(timestamp, histogram.getKey(), histogram.getValue(), true);
					} else {
						statistics.addSample(timestamp, histogram.getKey(), histogram.getValue(), false);
					}
				}
			}
		}
		return statistics;
	}
}
