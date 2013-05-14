package com.nhn.pinpoint.web.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.applicationmap.ApplicationStatistics;
import com.nhn.pinpoint.web.dao.AgentInfoDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.web.dao.ApplicationMapStatisticsCallerDao;
import com.nhn.pinpoint.web.dao.HostApplicationMapDao;
import com.nhn.pinpoint.web.vo.Application;
import com.profiler.common.ServiceType;
import com.profiler.common.bo.AgentInfoBo;

/**
 * 
 * @author netspider
 */
@Service
public class ApplicationMapServiceImpl implements ApplicationMapService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

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
	 * callerApplicationName이 호출한 callee를 모두 탐색
	 * 
	 * @param callerApplicationName
	 * @param from
	 * @param to
	 * @param foundApplications
	 * @return
	 */
	private Set<ApplicationStatistics> selectCallee(String callerApplicationName, short callerServiceType, long from, long to, Set<String> calleeFoundApplications, Set<String> callerFoundApplications) {
		// 이미 조회된 구간이면 skip
		if (calleeFoundApplications.contains(callerApplicationName + callerServiceType)) {
			logger.debug("ApplicationStatistics exists. Skip finding callee. " + callerApplicationName + callerServiceType);
			return new HashSet<ApplicationStatistics>(0);
		}
		calleeFoundApplications.add(callerApplicationName + callerServiceType);

		logger.debug("Find Callee. caller=" + callerApplicationName + ", serviceType=" + ServiceType.findServiceType(callerServiceType));

		final Set<ApplicationStatistics> calleeSet = new HashSet<ApplicationStatistics>();

		Map<String, ApplicationStatistics> callee = applicationMapStatisticsCalleeDao.selectCallee(callerApplicationName, callerServiceType, from, to);

		logger.debug("     Found Callee. caller=" + callerApplicationName + " (" + callee.size() + ")");

		for (Entry<String, ApplicationStatistics> entry : callee.entrySet()) {
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

			ApplicationStatistics stat = entry.getValue();

			Set<ApplicationStatistics> calleeSub = selectCallee(stat.getTo(), stat.getToServiceType().getCode(), from, to, calleeFoundApplications, callerFoundApplications);
			calleeSet.addAll(calleeSub);

			// 찾아진 녀석들에 대한 caller도 찾는다.
			for (ApplicationStatistics eachCallee : calleeSub) {
				Set<ApplicationStatistics> callerSub = selectCaller(eachCallee.getFrom(), eachCallee.getFromServiceType().getCode(), from, to, calleeFoundApplications, callerFoundApplications);
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
	 * @param foundApplications
	 * @return
	 */
	private Set<ApplicationStatistics> selectCaller(String calleeApplicationName, short calleeServiceType, long from, long to, Set<String> calleeFoundApplications, Set<String> callerFoundApplications) {
		// 이미 조회된 구간이면 skip
		if (callerFoundApplications.contains(calleeApplicationName + calleeServiceType)) {
			logger.debug("ApplicationStatistics exists. Skip finding caller. " + calleeApplicationName + calleeServiceType);
			return new HashSet<ApplicationStatistics>(0);
		}
		callerFoundApplications.add(calleeApplicationName + calleeServiceType);

		logger.debug("Find Caller. callee=" + calleeApplicationName + ", serviceType=" + ServiceType.findServiceType(calleeServiceType));

		final Set<ApplicationStatistics> callerSet = new HashSet<ApplicationStatistics>();

		Map<String, ApplicationStatistics> caller = applicationMapStatisticsCallerDao.selectCaller(calleeApplicationName, calleeServiceType, from, to);

		logger.debug("     Found Caller. callee=" + calleeApplicationName + " (" + caller.size() + ")");

		for (Entry<String, ApplicationStatistics> entry : caller.entrySet()) {
			fillAdditionalInfo(entry.getValue(), from, to);
			callerSet.add(entry.getValue());

			ApplicationStatistics stat = entry.getValue();

			// 나를 부른 application을 찾아야 하기 떄문에 to를 입력.
			Set<ApplicationStatistics> callerSub = selectCaller(stat.getFrom(), stat.getFromServiceType().getCode(), from, to, calleeFoundApplications, callerFoundApplications);
			callerSet.addAll(callerSub);

			// 찾아진 녀석들에 대한 callee도 찾는다.
			for (ApplicationStatistics eachCallee : callerSub) {
				// terminal이면 skip
				if (eachCallee.getToServiceType().isTerminal() || eachCallee.getToServiceType().isUnknown()) {
					continue;
				}
				Set<ApplicationStatistics> calleeSub = selectCallee(eachCallee.getTo(), eachCallee.getToServiceType().getCode(), from, to, calleeFoundApplications, callerFoundApplications);
				callerSet.addAll(calleeSub);
			}
		}

		return callerSet;
	}

	private boolean replaceApplicationInfo(ApplicationStatistics stat, long from, long to) {
		// rpc client의 목적지가 agent가 설치되어 application name이 존재한다면 replace.
		if (stat.getToServiceType().isRpcClient()) {
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

	private void fillAdditionalInfo(ApplicationStatistics stat, long from, long to) {
		if (stat.getToServiceType().isTerminal() || stat.getToServiceType().isUnknown()) {
			return;
		}
		
		Set<AgentInfoBo> agentSet = selectAgents(stat.getTo());
		
		if (agentSet.isEmpty()) {
			return;
		}
		
		stat.addToAgents(agentSet);
		stat.clearHosts();
		for (AgentInfoBo agentInfo : agentSet) {
			stat.addToHost(agentInfo.getHostname());
		}
		
		logger.debug("fill agent info. {}, {}", stat.getTo(), agentSet);
	}

	/**
	 * 메인화면에서 사용. 시간별로 TimeSlot을 조회하여 서버 맵을 그릴 때 사용한다.
	 */
	@Override
	public ApplicationMap selectApplicationMap(String applicationName, short serviceType, long from, long to) {
		logger.debug("SelectApplicationMap");

		StopWatch watch = new StopWatch("applicationMapWatch");
		watch.start();

		// 무한 탐색을 방지하기 위한 용도.
		final Set<String> callerFoundApplications = new HashSet<String>();
		final Set<String> calleeFoundApplications = new HashSet<String>();

		Set<ApplicationStatistics> callee = selectCallee(applicationName, serviceType, from, to, calleeFoundApplications, callerFoundApplications);
		Set<ApplicationStatistics> caller = selectCaller(applicationName, serviceType, from, to, calleeFoundApplications, callerFoundApplications);

		Set<ApplicationStatistics> data = new HashSet<ApplicationStatistics>(callee.size() + caller.size());
		data.addAll(callee);
		data.addAll(caller);

		ApplicationMap map = new ApplicationMap(data).build();

		watch.stop();
		logger.info("Fetch applicationmap elapsed. {}ms", watch.getLastTaskTimeMillis());
		
		return map;
	}
}
