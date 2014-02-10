package com.nhn.pinpoint.web.service;

import java.util.*;
import java.util.Map.Entry;

import com.nhn.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.nhn.pinpoint.web.applicationmap.Link;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;
import com.nhn.pinpoint.web.dao.*;
import com.nhn.pinpoint.web.vo.RawResponseTime;
import com.nhn.pinpoint.web.vo.ResponseHistogramSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
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
	 * @param from
	 * @param to
	 * @param calleeFoundApplications
	 * @param callerFoundApplications
	 * @return
	 */
	private Set<TransactionFlowStatistics> selectCallee(Application callerApplication, long from, long to, Set<Node> calleeFoundApplications, Set<Node> callerFoundApplications) {
		// 이미 조회된 구간이면 skip
        final Node key = new Node(callerApplication.getName(), callerApplication.getServiceType());
        if (calleeFoundApplications.contains(key)) {
			logger.debug("ApplicationStatistics exists. Skip finding callee. {} ", callerApplication);
			return new HashSet<TransactionFlowStatistics>(0);
		}
		calleeFoundApplications.add(key);
        if (logger.isDebugEnabled()) {
		    logger.debug("Finding Callee. caller={}", callerApplication);
        }

		final Set<TransactionFlowStatistics> calleeSet = new HashSet<TransactionFlowStatistics>();

		List<TransactionFlowStatistics> callee = applicationMapStatisticsCalleeDao.selectCallee(callerApplication.getName(), callerApplication.getServiceTypeCode(), from, to);

        if (logger.isDebugEnabled()) {
		    logger.debug("Found Callee. count={}, caller={}", callee.size(), callerApplication);
        }

		for (TransactionFlowStatistics stat : callee) {
			boolean replaced = replaceApplicationInfo(stat, from, to);

			// replaced된 녀석은 CLIENT이기 때문에 callee검색용도로만 사용하고 map에 추가하지 않는다.
			if (!replaced) {
				fillAdditionalInfo(stat, from, to);
				calleeSet.add(stat);
			}

			// terminal, unknowncloud 인 경우에는 skip
			if (stat.getToServiceType().isTerminal() || stat.getToServiceType().isUnknown()) {
				continue;
			}

			logger.debug("     Find subCallee of {}", stat.getTo());
            final Application application = new Application(stat.getTo(), stat.getToServiceType());
            Set<TransactionFlowStatistics> calleeSub = selectCallee(application, from, to, calleeFoundApplications, callerFoundApplications);
			logger.debug("     Found subCallee. count={}, caller={}", calleeSub.size(), stat.getTo());

			calleeSet.addAll(calleeSub);

			// 찾아진 녀석들에 대한 caller도 찾는다.
			for (TransactionFlowStatistics eachCallee : calleeSub) {
				logger.debug("     Find caller of {}", eachCallee.getFrom());
                Application calleeApplication = new Application(eachCallee.getFrom(), eachCallee.getFromServiceType().getCode());
				Set<TransactionFlowStatistics> callerSub = selectCaller(calleeApplication, from, to, calleeFoundApplications, callerFoundApplications);
				logger.debug("     Found subCaller. count={}, callee={}", callerSub.size(), eachCallee.getFrom());
				calleeSet.addAll(callerSub);
			}
		}

		return calleeSet;
	}

	/**
	 * callee applicationname을 호출한 caller 조회.
	 * 
	 * @param calleeApplication
	 * @param from
	 * @param to
	 * @return
	 */
	private Set<TransactionFlowStatistics> selectCaller(Application calleeApplication, long from, long to, Set<Node> calleeFoundApplications, Set<Node> callerFoundApplications) {
		// 이미 조회된 구간이면 skip
        final Node key = new Node(calleeApplication.getName(), calleeApplication.getServiceType());
        if (callerFoundApplications.contains(key)) {
			logger.debug("ApplicationStatistics exists. Skip finding caller. {}", calleeApplication);
			return new HashSet<TransactionFlowStatistics>(0);
		}
		callerFoundApplications.add(key);
        if (logger.isDebugEnabled()) {
		    logger.debug("Finding Caller. callee={}", calleeApplication);
        }

		final Set<TransactionFlowStatistics> callerSet = new HashSet<TransactionFlowStatistics>();

		final List<TransactionFlowStatistics> caller = applicationMapStatisticsCallerDao.selectCaller(calleeApplication.getName(), calleeApplication.getServiceTypeCode(), from, to);

		logger.debug("Found Caller. count={}, callee={}", caller.size(), calleeApplication);

		for (TransactionFlowStatistics stat : caller) {
			fillAdditionalInfo(stat, from, to);
			callerSet.add(stat);

			// 나를 부른 application을 찾아야 하기 떄문에 to를 입력.
            Application application = new Application(stat.getFrom(), stat.getFromServiceType());
            Set<TransactionFlowStatistics> callerSub = selectCaller(application, from, to, calleeFoundApplications, callerFoundApplications);
			callerSet.addAll(callerSub);

			// 찾아진 녀석들에 대한 callee도 찾는다.
			for (TransactionFlowStatistics eachCallee : callerSub) {
				// terminal이면 skip
				if (eachCallee.getToServiceType().isTerminal() || eachCallee.getToServiceType().isUnknown()) {
					continue;
				}
                Application eachCalleeApplication = new Application(eachCallee.getTo(), eachCallee.getToServiceType());
				Set<TransactionFlowStatistics> calleeSub = selectCallee(eachCalleeApplication, from, to, calleeFoundApplications, callerFoundApplications);
				callerSet.addAll(calleeSub);
			}
		}

		return callerSet;
	}

	private boolean replaceApplicationInfo(TransactionFlowStatistics stat, long from, long to) {
		// rpc client의 목적지가 agent가 설치되어 application name이 존재한다면 replace.
		if (stat.getToServiceType().isRpcClient()) {
			logger.debug("Find applicationName {} {} {}", stat.getTo(), from, to);
			final Application app = hostApplicationMapDao.findApplicationName(stat.getTo(), from, to);
			if (app != null) {
				logger.debug("Application info replaced. {} => {}", stat, app);

				stat.setTo(app.getName());
				stat.setToServiceType(app.getServiceType());
				return true;
			} else {
				stat.setToServiceType(ServiceType.UNKNOWN /* ServiceType.UNKNOWN_CLOUD */);
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

		logger.debug("Fill agent info. {}, {}", stat.getTo(), agentSet);
	}

	/**
	 * 메인화면에서 사용. 시간별로 TimeSlot을 조회하여 서버 맵을 그릴 때 사용한다.
	 */
	@Override
	public ApplicationMap selectApplicationMap(Application sourceApplication, long from, long to) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        logger.debug("SelectApplicationMap");

		StopWatch watch = new StopWatch("applicationMapWatch");
		watch.start();

        // 무한 탐색을 방지하기 위한 용도.
		final Set<Node> callerFoundApplications = new HashSet<Node>();
		final Set<Node> calleeFoundApplications = new HashSet<Node>();
		Set<TransactionFlowStatistics> callee = selectCallee(sourceApplication, from, to, calleeFoundApplications, callerFoundApplications);
		logger.debug("Result of finding callee {}", callee);

		Set<TransactionFlowStatistics> caller = selectCaller(sourceApplication, from, to, calleeFoundApplications, callerFoundApplications);
		logger.debug("Result of finding caller {}", caller);

		Set<TransactionFlowStatistics> data = new HashSet<TransactionFlowStatistics>(callee.size() + caller.size());
		data.addAll(callee);
		data.addAll(caller);

		ApplicationMap map = new ApplicationMapBuilder().build(data);
        appendResponseTime(map, from, to);

		watch.stop();
		logger.info("Fetch applicationmap elapsed. {}ms", watch.getLastTaskTimeMillis());

		return map;
	}

    private void appendResponseTime(ApplicationMap map, long from, long to) {
        List<com.nhn.pinpoint.web.applicationmap.Node> nodes = map.getNodes();
        for (com.nhn.pinpoint.web.applicationmap.Node node : nodes) {
            if (node.getServiceType().isWas()) {
                // was일 경우 자신의 response 히스토그램을 조회하여 채운다.
                final Application application = new Application(node.getApplicationName(), node.getServiceType());
                final List<RawResponseTime> responseHistogram = this.mapResponseDao.selectResponseTime(application, from, to);
                ResponseHistogramSummary histogramSummary = createHistogramSummary(application, responseHistogram);
                node.setResponseHistogramSummary(histogramSummary);
            } else if(node.getServiceType().isTerminal() || node.getServiceType().isUnknown()) {
                // 터미널 노드인경우, 자신을 가리키는 link값을 합하여 histogram을 생성한다.
                Application nodeApplication = new Application(node.getApplicationName(), node.getServiceType());
                final ResponseHistogramSummary summary = new ResponseHistogramSummary(nodeApplication);

                List<Link> linkList = map.getLinks();
                for (Link link : linkList) {
                    com.nhn.pinpoint.web.applicationmap.Node toNode = link.getTo();
                    String applicationName = toNode.getApplicationName();
                    ServiceType serviceType = toNode.getServiceType();
                    Application destination = new Application(applicationName, serviceType);
                    // destnation이 자신을 가리킨다면 데이터를 머지함.
                    if (nodeApplication.equals(destination)) {
                        ResponseHistogram linkHistogram = link.getHistogram();
//                        summary.addTotal(linkHistogram);
                        summary.addLinkHistogram(linkHistogram);
                    }
                }
                node.setResponseHistogramSummary(summary);
            } else if(node.getServiceType().isUser()) {
                // User노드인 경우 source 링크를 찾아 histogram을 생성한다.
                Application nodeApplication = new Application(node.getApplicationName(), node.getServiceType());
                final ResponseHistogramSummary summary = new ResponseHistogramSummary(nodeApplication);

                List<Link> linkList = map.getLinks();
                for (Link link : linkList) {
                    com.nhn.pinpoint.web.applicationmap.Node fromNode = link.getFrom();
                    String applicationName = fromNode.getApplicationName();
                    ServiceType serviceType = fromNode.getServiceType();
                    Application source = new Application(applicationName, serviceType);
                    // destnation이 자신을 가리킨다면 데이터를 머지함.
                    if (nodeApplication.equals(source)) {
                        ResponseHistogram linkHistogram = link.getHistogram();
//                        summary.addTotal(linkHistogram);
                        summary.addLinkHistogram(linkHistogram);
                    }
                }
                node.setResponseHistogramSummary(summary);
            } else {
                // 그냥 데미 데이터
                Application nodeApplication = new Application(node.getApplicationName(), node.getServiceType());
                ResponseHistogramSummary dummy = new ResponseHistogramSummary(nodeApplication);
                node.setResponseHistogramSummary(dummy);
            }

        }

    }

    private ResponseHistogramSummary createHistogramSummary(Application application, List<RawResponseTime> responseHistogram) {
        final ResponseHistogramSummary summary = new ResponseHistogramSummary(application);
        for (RawResponseTime rawResponseTime : responseHistogram) {
            final List<ResponseHistogram> responseHistogramList = rawResponseTime.getResponseHistogramList();
            for (ResponseHistogram histogram : responseHistogramList) {
                summary.addTotal(histogram);
            }
        }
        return summary;
    }

    @Override
	public LinkStatistics linkStatistics(Application sourceApplication, Application destinationApplication, long from, long to) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        if (destinationApplication == null) {
            throw new NullPointerException("destinationApplication must not be null");
        }

        List<Map<Long, Map<Short, Long>>> list;

		if (ServiceType.findServiceType(sourceApplication.getServiceTypeCode()).isUser()) {
			logger.debug("Find 'client -> any' link statistics");
			// client는 applicatinname + servicetype.client로 기록된다.
			// 그래서 src, dest가 둘 다 dest로 같음.
			list = applicationMapStatisticsCalleeDao.selectCalleeStatistics(destinationApplication.getName(), sourceApplication.getServiceTypeCode(), destinationApplication.getName(), destinationApplication.getServiceTypeCode(), from, to);
		} else if (destinationApplication.getServiceType().isWas()) {
			logger.debug("Find 'any -> was' link statistics");
			// destination이 was인 경우에는 중간에 client event가 끼어있기 때문에 callee에서
			// caller가
			// 같은녀석을 찾아야 한다.
			list = applicationMapStatisticsCallerDao.selectCallerStatistics(sourceApplication.getName(), sourceApplication.getServiceTypeCode(), destinationApplication.getName(), destinationApplication.getServiceTypeCode(), from, to);
		} else {
			logger.debug("Find 'was -> terminal' link statistics");
			// 일반적으로 was -> terminal 간의 통계정보 조회.
			list = applicationMapStatisticsCalleeDao.selectCalleeStatistics(sourceApplication.getName(), sourceApplication.getServiceTypeCode(), destinationApplication.getName(), destinationApplication.getServiceTypeCode(), from, to);
		}

		LinkStatistics statistics = new LinkStatistics(from, to);

		// 조회가 안되는 histogram slot이 있으면 UI에 모두 보이지 않기 때문에 미리 정의된 slot을 모두 할당한다.
		statistics.setDefaultHistogramSlotList(destinationApplication.getServiceType().getHistogramSchema().getHistogramSlotList());

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
