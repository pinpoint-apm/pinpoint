package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.nhn.hippo.web.calltree.server.AgentIdNodeSelector;
import com.nhn.hippo.web.calltree.server.ApplicationIdNodeSelector;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.dao.AgentInfoDao;
import com.nhn.hippo.web.dao.ApplicationIndexDao;
import com.nhn.hippo.web.dao.ApplicationTraceIndexDao;
import com.nhn.hippo.web.dao.ClientStatisticsDao;
import com.nhn.hippo.web.dao.TerminalStatisticsDao;
import com.nhn.hippo.web.dao.TraceDao;
import com.nhn.hippo.web.dao.TraceIndexDao;
import com.nhn.hippo.web.vo.Application;
import com.nhn.hippo.web.vo.BusinessTransactions;
import com.nhn.hippo.web.vo.ClientStatistics;
import com.nhn.hippo.web.vo.TerminalStatistics;
import com.nhn.hippo.web.vo.TraceId;
import com.nhn.hippo.web.vo.scatter.Dot;
import com.profiler.common.ServiceType;
import com.profiler.common.bo.AgentInfoBo;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEventBo;

/**
 * @author netspider
 */
@Service
public class FlowChartServiceImpl implements FlowChartService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TraceDao traceDao;

	@Autowired
	private TraceIndexDao traceIndexDao;

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

	@Autowired
	private TerminalStatisticsDao terminalStatisticsDao;

	@Autowired
	private ClientStatisticsDao clientStatisticsDao;

	@Autowired
	private AgentInfoDao agentInfoDao;
	
	@Override
	public List<Application> selectAllApplicationNames() {
		return applicationIndexDao.selectAllApplicationNames();
	}

	@Override
	public String[] selectAgentIdsFromApplicationName(String applicationName) {
		return applicationIndexDao.selectAgentIds(applicationName);
	}

	@Override
	public Set<TraceId> selectTraceIdsFromTraceIndex(String[] agentIds, long from, long to) {
		if (agentIds == null) {
			throw new NullPointerException("agentIds");
		}

		if (agentIds.length == 1) {
			// single scan
			if (logger.isTraceEnabled()) {
				logger.trace("scan {}, {}, {}", new Object[] { agentIds[0], from, to });
			}
			List<List<TraceId>> bytes = this.traceIndexDao.scanTraceIndex(agentIds[0], from, to);
			Set<TraceId> result = new HashSet<TraceId>();
			for (List<TraceId> list : bytes) {
				for (TraceId traceId : list) {
					result.add(traceId);
					logger.trace("traceid:{}", traceId);
				}
			}
			return result;
		} else {
			// multi scan 가능한 동일 open htable 에서 액세스함.
			List<List<List<TraceId>>> multiScan = this.traceIndexDao.multiScanTraceIndex(agentIds, from, to);
			Set<TraceId> result = new HashSet<TraceId>();
			for (List<List<TraceId>> list : multiScan) {
				for (List<TraceId> scan : list) {
					for (TraceId traceId : scan) {
						result.add(traceId);
					}
				}
			}
			return result;
		}
	}

	@Deprecated
	@Override
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIds) {
		final ServerCallTree tree = new ServerCallTree(new ApplicationIdNodeSelector());

		List<List<SpanBo>> traces = this.traceDao.selectSpans(traceIds);

		for (List<SpanBo> transaction : traces) {
			// List<SpanBo> processed = refine(transaction);
			// markRecursiveCall(transaction);
			for (SpanBo eachTransaction : transaction) {
				tree.addSpan(eachTransaction);
			}
		}
		return tree.build();
	}

	/**
	 * DetailView에서 사용함. 하나의 Span을 선택했을때 Draw되는 데이터를 생성하는 함수이다 makes call tree
	 * of transaction detail view
	 */
	@Override
	public ServerCallTree selectServerCallTree(TraceId traceId) {
		StopWatch watch = new StopWatch();
		watch.start();

		List<SpanBo> transaction = this.traceDao.selectSpans(traceId);

		Set<String> endPoints = createUniqueEndpoint(transaction);
		ServerCallTree tree = createServerCallTree(transaction);

		// subSpan에서 record할 데이터만 골라낸다.
		List<SpanEventBo> spanEventBoList = findRecordStatisticsSpanEventData(transaction, endPoints);
		tree.addSpanEventList(spanEventBoList);

		tree.build();
		
		watch.stop();
		logger.info("Fetch single transaction serverCallTree elapsed. {}ms", watch.getLastTaskTimeMillis());

		return tree;
	}

	private List<SpanEventBo> findRecordStatisticsSpanEventData(List<SpanBo> transaction, Set<String> endPoints) {
		List<SpanEventBo> filterSpanEventBo = new ArrayList<SpanEventBo>();
		for (SpanBo eachTransaction : transaction) {
			List<SpanEventBo> spanEventBoList = eachTransaction.getSpanEventBoList();

			if (spanEventBoList == null) {
				continue;
			}

			for (SpanEventBo spanEventBo : spanEventBoList) {
				// 통계정보로 잡지 않을 데이터는 스킵한다.
				if (!spanEventBo.getServiceType().isRecordStatistics()) {
					continue;
				}

				// remove subspan of the rpc client
				if (!endPoints.contains(spanEventBo.getEndPoint())) {
					// this is unknown cloud
					filterSpanEventBo.add(spanEventBo);
				}
			}
		}
		return filterSpanEventBo;
	}

	/**
	 * ServerCallTree를 생성하고 low SpanBo데이터를 tree에 추가한다.
	 * 
	 * @param transaction
	 * @return
	 */
	private ServerCallTree createServerCallTree(List<SpanBo> transaction) {
		ServerCallTree serverCallTree = new ServerCallTree(new AgentIdNodeSelector());
		serverCallTree.addSpanList(transaction);

		// TODO 이 메소드는 transaction하나만 조회하는 페이지에서 사용되기 때문에 이렇게 한다.
		// 나중에 방식을 변경할 필요가 있을지도...
		for (SpanBo span : transaction) {
			if (!span.isRoot()) {
				continue;
			}
			ClientStatistics stat = new ClientStatistics(span.getApplicationId(), span.getServiceType().getCode());
			stat.getHistogram().addSample(span.getElapsed());
			serverCallTree.addClientStatistics(stat);
		}

		return serverCallTree;
	}

	/**
	 * Trace uuid를 구성하는 SpanBo의 집합에서 endpoint 값을 유니크한 값으로 뽑아온다.
	 * 
	 * @param transaction
	 * @return
	 */
	private Set<String> createUniqueEndpoint(List<SpanBo> transaction) {
		// markRecursiveCall(transaction);
		Set<String> endPointSet = new HashSet<String>();
		for (SpanBo eachTransaction : transaction) {
			endPointSet.add(eachTransaction.getEndPoint());
		}
		return endPointSet;
	}

	private Set<String> selectApplicationHosts(String applicationId) {
		String[] agentIds = applicationIndexDao.selectAgentIds(applicationId);

		Set<String> hostnames = new HashSet<String>();

		for (String agentId : agentIds) {
			// TODO 조회 시간대에 따라서 agent info row timestamp를 변경하여 조회해야하는지는 모르겠음.
			AgentInfoBo info = agentInfoDao.findAgentInfoBeforeStartTime(agentId, System.currentTimeMillis());
			hostnames.add(info.getHostname());
		}

		return hostnames;
	}
	
	/**
	 * 메인화면에서 사용. 시간별로 TimeSlot을 조회하여 서버 맵을 그릴 때 사용한다. makes call tree of main
	 * view
	 */
	@Override
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIds, String applicationName, long from, long to) {
		StopWatch watch = new StopWatch();
		watch.start();
		
		final Map<String, ServiceType> terminalQueryParams = new HashMap<String, ServiceType>();
		final Map<String, ServiceType> clientQueryParams = new HashMap<String, ServiceType>();
		final Set<String> hostnameQueryParams = new HashSet<String>();
		final ServerCallTree tree = new ServerCallTree(new ApplicationIdNodeSelector());

		// fetch non-terminal spans
		List<List<SpanBo>> traces = this.traceDao.selectSpans(traceIds);

		int totalNonTerminalSpansCount = 0;

		Set<String> nonTerminalEndPoints = new HashSet<String>();

		// processing spans
		for (List<SpanBo> transaction : traces) {
			totalNonTerminalSpansCount += transaction.size();

			// List<SpanBo> processed = refine(transaction);
			// markRecursiveCall(transaction);
			for (SpanBo eachTransaction : transaction) {
				tree.addSpan(eachTransaction);
				
				// make hostname query params
				hostnameQueryParams.add(eachTransaction.getApplicationId());

				// make query param
				terminalQueryParams.put(eachTransaction.getApplicationId(), eachTransaction.getServiceType());

				// make client query param
				if (eachTransaction.isRoot()) {
					// TODO 여기에서 service type을 CLIENT로 지정해버려서 client유형별로 조회 불가능. 나중에 고쳐야함.
					clientQueryParams.put(eachTransaction.getApplicationId(), ServiceType.CLIENT);
				}

				nonTerminalEndPoints.add(eachTransaction.getEndPoint());
			}
		}

		// fetch terminal info
		for (Entry<String, ServiceType> param : terminalQueryParams.entrySet()) {
			ServiceType svcType = param.getValue();
			if (!svcType.isRpcClient() && !svcType.isUnknown() && !svcType.isTerminal()) {
				long start = System.currentTimeMillis();
				List<Map<String, TerminalStatistics>> terminals = terminalStatisticsDao.selectTerminal(param.getKey(), from, to);
				logger.info("	Fetch terminals of {} : {}ms", param.getKey(), System.currentTimeMillis() - start);

				for (Map<String, TerminalStatistics> terminal : terminals) {
					for (Entry<String, TerminalStatistics> entry : terminal.entrySet()) {
						// TODO 임시방편
						TerminalStatistics terminalStatistics = entry.getValue();

						// 이 요청의 destination이 수집된 trace정보에 없으면 unknown cloud로 처리한다.
						if (!nonTerminalEndPoints.contains(terminalStatistics.getTo())) {

							if (ServiceType.findServiceType(terminalStatistics.getToServiceType()).isRpcClient()) {
								terminalStatistics.setToServiceType(ServiceType.UNKNOWN_CLOUD.getCode());
							}

							tree.addTerminalStatistics(terminalStatistics);
						}
					}
				}
			}
		}

		logger.debug("client query params=" + clientQueryParams);
		
		// fetch client info
		for (Entry<String, ServiceType> param : clientQueryParams.entrySet()) {
			List<Map<String, ClientStatistics>> clients = clientStatisticsDao.selectClient(param.getKey(), param.getValue().getCode(), from, to);

			for (Map<String, ClientStatistics> client : clients) {
				for (Entry<String, ClientStatistics> clientEntry : client.entrySet()) {
					logger.debug("fetched client=" + clientEntry);
					tree.addClientStatistics(clientEntry.getValue());
				}
			}
		}
		
		logger.debug("hostname query params=" + hostnameQueryParams);

		// fetch hostnames
		for (String applicationId : hostnameQueryParams) {
			tree.addApplicationHosts(applicationId, selectApplicationHosts(applicationId));
		}

		tree.build();
		
		watch.stop();
		logger.info("Fetch serverCallTree elapsed. {}ms", watch.getLastTaskTimeMillis());
		
		return tree;
	}

	@Deprecated
	private SpanBo findChildSpan(final List<SpanBo> list, final SpanBo parent) {
		for (int i = 0; i < list.size(); i++) {
			SpanBo child = list.get(i);

			if (child.getParentSpanId() == parent.getSpanId()) {
				return child;
			}
		}
		return null;
	}

	/**
	 * server map이 recursive call을 표현할 수 있게 되어 필요 없음.
	 * 
	 * @param list
	 */
	@Deprecated
	private void markRecursiveCall(final List<SpanBo> list) {
		/*
		 * for (int i = 0; i < list.size(); i++) { SpanBo a = list.get(i); for
		 * (int j = 0; j < list.size(); j++) { if (i == j) continue; SpanBo b =
		 * list.get(j); if (a.getServiceName().equals(b.getServiceName()) &&
		 * a.getSpanId() == b.getParentSpanId()) {
		 * a.increaseRecursiveCallCount(); } } }
		 */
	}

	@Override
	public Set<TraceId> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to) {
		if (applicationName == null) {
			throw new NullPointerException("applicationName");
		}

		if (logger.isTraceEnabled()) {
			logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}, {}", new Object[] { applicationName, from, to });
		}

		List<List<TraceId>> traceIdList = this.applicationTraceIndexDao.scanTraceIndex(applicationName, from, to);
		Set<TraceId> result = new HashSet<TraceId>();
		for (List<TraceId> list : traceIdList) {
			for (TraceId traceId : list) {
				result.add(traceId);
				logger.trace("traceid:{}", traceId);
			}
		}
		return result;
	}

	@Deprecated
	@Override
	public String[] selectAgentIds(String[] hosts) {
		// List<HbaseColumn> column = new ArrayList<HBaseQuery.HbaseColumn>();
		// column.add(new HbaseColumn("Agents", "AgentID"));
		//
		// HBaseQuery query = new HBaseQuery(HBaseTables.APPLICATION_INDEX,
		// null, null, column);
		// Iterator<Map<String, byte[]>> iterator = client.getHBaseData(query);
		//
		// if (logger.isDebugEnabled()) {
		// while (iterator.hasNext()) {
		// logger.debug("selectedAgentId={}", iterator.next());
		// }
		// logger.debug("!!!==============WARNING==============!!!");
		// logger.debug("!!! selectAgentIds IS NOT IMPLEMENTED !!!");
		// logger.debug("!!!===================================!!!");
		// }

		return hosts;
	}

	@Override
	public List<Dot> selectScatterData(String applicationName, long from, long to) {
		List<List<Dot>> scanTrace = applicationTraceIndexDao.scanTraceScatter(applicationName, from, to);

		List<Dot> list = new ArrayList<Dot>();

		for (List<Dot> l : scanTrace) {
			for (Dot dot : l) {
				list.add(dot);
			}
		}

		return list;
	}

	@Override
	public List<Dot> selectScatterData(String applicationName, long from, long to, int limit) {
		return applicationTraceIndexDao.scanTraceScatter2(applicationName, from, to, limit);
	}

	@Override
	public BusinessTransactions selectBusinessTransactions(Set<TraceId> traceIds, String applicationName, long from, long to) {
		List<List<SpanBo>> traceList = this.traceDao.selectSpans(traceIds);

		BusinessTransactions businessTransactions = new BusinessTransactions();
		for (List<SpanBo> trace : traceList) {
			for (SpanBo spanBo : trace ) {
				// 해당 application으로 인입된 요청만 보여준다.
				if (applicationName.equals(spanBo.getApplicationId())) {
					businessTransactions.add(spanBo);
				}
			}
		}

		return businessTransactions;
	}
}
