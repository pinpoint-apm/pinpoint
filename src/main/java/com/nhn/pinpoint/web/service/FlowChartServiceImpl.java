package com.nhn.pinpoint.web.service;

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatisticsUtils;
import com.nhn.pinpoint.web.calltree.server.AgentIdNodeSelector;
import com.nhn.pinpoint.web.calltree.server.ApplicationIdNodeSelector;
import com.nhn.pinpoint.web.calltree.server.NodeSelector;
import com.nhn.pinpoint.web.calltree.server.ServerCallTree;
import com.nhn.pinpoint.web.dao.AgentInfoDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.dao.ApplicationMapStatisticsCalleeDao;
import com.nhn.pinpoint.web.dao.ApplicationMapStatisticsCallerDao;
import com.nhn.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.web.dao.TraceDao;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.BusinessTransactions;
import com.nhn.pinpoint.web.vo.ClientStatistics;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.ResultWithMark;
import com.nhn.pinpoint.web.vo.TransactionId;

/**
 * @author netspider
 */
@Service
public class FlowChartServiceImpl implements FlowChartService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TraceDao traceDao;

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

	@Autowired
	private ApplicationMapStatisticsCalleeDao applicationMapStatisticsCalleeDao;
	
	@Autowired
	private ApplicationMapStatisticsCallerDao applicationMapStatisticsCallerDao;

	@Autowired
	private AgentInfoDao agentInfoDao;

    private static final Object V = new Object();
	
	@Override
	public List<Application> selectAllApplicationNames() {
		return applicationIndexDao.selectAllApplicationNames();
	}

	/**
	 * DetailView에서 사용함. 하나의 Span을 선택했을때 Draw되는 데이터를 생성하는 함수이다 makes call tree
	 * of transaction detail view
	 */
	@Override
	public ServerCallTree selectServerCallTree(TransactionId traceId) {
		StopWatch watch = new StopWatch();
		watch.start();

		List<SpanBo> transaction = this.traceDao.selectSpans(traceId);

		Set<String> endPoints = createUniqueEndpoint(transaction);
		ServerCallTree tree = createServerCallTree(transaction, new AgentIdNodeSelector());

		// subSpan에서 record할 데이터만 골라낸다.
		List<SpanEventBo> spanEventBoList = findRecordStatisticsSpanEventData(transaction, endPoints);
		tree.addSpanEventList(spanEventBoList);

		tree.build();

		watch.stop();
		logger.info("Fetch single transaction serverCallTree elapsed. {}ms", watch.getLastTaskTimeMillis());

		return tree;
	}
	
	/**
	 * filtered application map
	 */
	@Deprecated
	@Override
	public ServerCallTree selectServerCallTree(Set<TransactionId> traceIdSet, Filter filter) {
		StopWatch watch = new StopWatch();
		watch.start();
		
		List<List<SpanBo>> transactionList = this.traceDao.selectAllSpans(traceIdSet);
		List<SpanBo> filteredTransaction = new ArrayList<SpanBo>();
		for (List<SpanBo> t : transactionList) {
			if (filter.include(t)) {
				for (SpanBo span : t) {
					filteredTransaction.add(span);
				}
			}
		}
		
		Set<String> endPoints = createUniqueEndpoint(filteredTransaction);
		ServerCallTree tree = createServerCallTree(filteredTransaction, new ApplicationIdNodeSelector());
		
		// subSpan에서 record할 데이터만 골라낸다.
		List<SpanEventBo> spanEventBoList = findRecordStatisticsSpanEventData(filteredTransaction, endPoints);
		
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
	private ServerCallTree createServerCallTree(List<SpanBo> transaction, NodeSelector nodeSelector) {
		ServerCallTree serverCallTree = new ServerCallTree(nodeSelector);
		serverCallTree.addSpanList(transaction);

		// TODO 이 메소드는 transaction하나만 조회하는 페이지에서 사용되기 때문에 이렇게 한다.
		// 나중에 방식을 변경할 필요가 있을지도...
		for (SpanBo span : transaction) {
			if (!span.isRoot()) {
				continue;
			}
			ClientStatistics stat = new ClientStatistics(span.getApplicationId(), ServiceType.USER.getCode());
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

	@Override
	public ResultWithMark<List<TransactionId>, Long> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to, int limit) {
		if (applicationName == null) {
			throw new NullPointerException("applicationName");
		}

		if (logger.isTraceEnabled()) {
			logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}, {}", applicationName, from, to);
		}

		return this.applicationTraceIndexDao.scanTraceIndex(applicationName, from, to, limit);
	}

	@Override
	public BusinessTransactions selectBusinessTransactions(List<TransactionId> traceIds, String applicationName, long from, long to, Filter filter) {
		List<List<SpanBo>> traceList;

		if (filter == Filter.NONE) {
			traceList = this.traceDao.selectSpans(traceIds);
		} else {
			traceList = this.traceDao.selectAllSpans(traceIds);
		}

		BusinessTransactions businessTransactions = new BusinessTransactions();
		for (List<SpanBo> trace : traceList) {
			if (!filter.include(trace)) {
				continue;
			}
			
			for (SpanBo spanBo : trace) {
				// 해당 application으로 인입된 요청만 보여준다.
				if (applicationName.equals(spanBo.getApplicationId())) {
					businessTransactions.add(spanBo);
				}
			}
		}

		return businessTransactions;
	}
	
	@Override
	public LinkStatistics linkStatisticsDetail(long from, long to, List<TransactionId> traceIdSet, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType, Filter filter) {
		StopWatch watch = new StopWatch();
		watch.start();

		List<List<SpanBo>> transactionList = this.traceDao.selectAllSpans(traceIdSet);
		List<SpanBo> transaction = new ArrayList<SpanBo>();
		for (List<SpanBo> t : transactionList) {
			if (filter.include(t)) {
				for (SpanBo span : t) {
					transaction.add(span);
				}
			}
		}

		LinkStatistics statistics = new LinkStatistics(from, to);

		// TODO fromToFilter처럼. node의 타입에 따른 처리 필요함.
		
		// scan transaction list
		for (SpanBo span : transaction) {
			if (srcApplicationName.equals(span.getApplicationId()) && srcServiceType == span.getServiceType().getCode()) {
				List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
				if (spanEventBoList == null) {
					continue;
				}

				// find dest elapsed time
				for (SpanEventBo spanEventBo : spanEventBoList) {
					if (destServiceType == spanEventBo.getServiceType().getCode() && destApplicationName.equals(spanEventBo.getDestinationId())) {
						// find exception
						boolean hasException = spanEventBo.hasException();
						// add sample
						// TODO : 실제값 대신 slot값을 넣어야 함.
						statistics.addSample(span.getStartTime() + spanEventBo.getStartElapsed(), spanEventBo.getEndElapsed(), 1, hasException);
						break;
					}
				}
			}
		}

		watch.stop();
		logger.info("Fetch link statistics elapsed. {}ms", watch.getLastTaskTimeMillis());

		return statistics;
	}

	@Override
	public LinkStatistics linkStatistics(long from, long to, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType) {
		List<Map<Long, Map<Short, Long>>> list;

		if (ServiceType.findServiceType(srcServiceType) == ServiceType.CLIENT) {
			logger.debug("Find 'client -> any' link statistics");
			// client는 applicatinname + servicetype.client로 기록된다.
			// 그래서 src, dest가 둘 다 dest로 같음.
			list = applicationMapStatisticsCalleeDao.selectCalleeStatistics(destApplicationName, srcServiceType, destApplicationName, destServiceType, from, to);
		} else if (ServiceType.findServiceType(destServiceType).isWas()) {
			logger.debug("Find 'any -> was' link statistics");
			// destination이 was인 경우에는 중간에 client event가 끼어있기 때문에 callee에서 caller가
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

		logger.debug("Fetched statistics data=" + list);
		
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
	
	/**
	 * filtered application map
	 */
	@Override
	public ApplicationMap selectApplicationMap(List<TransactionId> transactionIdList, long from, long to, Filter filter) {
		StopWatch watch = new StopWatch();
		watch.start();

        // 개별 객체를 각각 보고 재귀 내용을 삭제함.
        // 향후 tree base로 중복 구간을 점검하여 없앨 경우 filter를 치면 안됨.
        Collection<TransactionId> filterdList = filter(transactionIdList);

		List<List<SpanBo>> transactionList = this.traceDao.selectAllSpans(filterdList);

		Set<TransactionFlowStatistics> statisticsData = new HashSet<TransactionFlowStatistics>();
		Map<String, TransactionFlowStatistics> statisticsMap = new HashMap<String, TransactionFlowStatistics>();
		Map<Integer, SpanBo> transactionSpanMap = new HashMap<Integer, SpanBo>();

//		TimeseriesResponses tr = new TimeseriesResponses(from, to);

//		System.out.println("@transactionList.size=" + transactionList.size() + "\n");
		
		// 통계정보로 변환한다.
		for (List<SpanBo> transaction : transactionList) {
			if (!filter.include(transaction)) {
				continue;
			}
			
//			System.out.println("@transaction.size=" + transaction.size() + "\n");
			
			transactionSpanMap.clear();
			for (SpanBo span : transaction) {
				transactionSpanMap.put(span.getSpanId(), span);
			}

			for (SpanBo span : transaction) {
				String src, dest;
				ServiceType srcServiceType, destServiceType;
				SpanBo parentSpan = transactionSpanMap.get(span.getParentSpanId());

				if (span.isRoot() || parentSpan == null) {
					src = span.getApplicationId();
					srcServiceType = ServiceType.CLIENT;
				} else {
					src = parentSpan.getApplicationId();
					srcServiceType = parentSpan.getServiceType();
				}

				dest = span.getApplicationId();
				destServiceType = span.getServiceType();
				
				if(!destServiceType.isRecordStatistics() || destServiceType.isRpcClient()) {
					continue;
				}
				
				String statId = TransactionFlowStatisticsUtils.makeId(src, srcServiceType, dest, destServiceType);
				TransactionFlowStatistics stat = (statisticsMap.containsKey(statId) ? statisticsMap.get(statId) : new TransactionFlowStatistics(src, srcServiceType, dest, destServiceType));

//				// histogram
//				ResponseHistogram histogram = stat.getHistogram();
				int slot = destServiceType.getHistogram().findHistogramSlot(span.getElapsed()).getSlotTime();
//				histogram.addSample((short) slot, 1);
//				
//				// host 정보 추가.
//				stat.addToHost(span.getEndPoint());
//				
//				// agent 정보추가.
//				String agentId = span.getAgentId();
//				AgentInfoBo agentInfo = null;
//				if (agentInfoCache.containsKey(agentId)) {
//					agentInfo = agentInfoCache.get(agentId);
//				} else {
//					List<AgentInfoBo> agentInfoList = agentInfoDao.getAgentInfo(agentId, span.getAgentStartTime());
//					if (!agentInfoList.isEmpty()) {
//						agentInfo = agentInfoList.get(0);
//					}
//					agentInfoCache.put(agentId, agentInfo);
//				}
//				stat.addToAgent(agentInfo);

				stat.addSample(dest, destServiceType.getCode(), (short) slot, 1);
				
				statisticsData.add(stat);
				statisticsMap.put(statId, stat);
				
				// link timeseries statistics추가.
//				tr.add(statId, span.getCollectorAcceptTime(), span.getElapsed(), 1L);
				
				// application timeseries statistics
//				tr.add(span.getApplicationId(), span.getCollectorAcceptTime(), span.getElapsed(), 1L);

//				System.out.println("\n----------------------------------");
//				System.out.println("@src\t\t" + src);
//				System.out.println("@srcType\t\t" + srcServiceType);
//				System.out.println("@dest\t\t" + dest);
//				System.out.println("@destType\t" + destServiceType);
//				System.out.println("@span\t\t" + span);
//				System.out.println("@stat\t\t" + stat);
//				System.out.println("----------------------------------\n\n");
				
				/**
				 * span event의 statistics추가.
				 */
				List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
				if (spanEventBoList == null || spanEventBoList.isEmpty()) {
					continue;
				}
				src = span.getApplicationId();
				srcServiceType = span.getServiceType();
				
				for (SpanEventBo spanEvent : spanEventBoList) {
					dest = spanEvent.getDestinationId();
					destServiceType = spanEvent.getServiceType();
					
					if(!destServiceType.isRecordStatistics() /* || destServiceType.isRpcClient() */) {
						continue;
					}
					
					// rpc client이면서 acceptor가 없으면 unknown으로 변환시킨다.
					// 내가 아는 next spanid를 spanid로 가진 span이 있으면 acceptor가 존재하는 셈.
					if (destServiceType.isRpcClient()) {
						if (transactionSpanMap.containsKey(spanEvent.getNextSpanId())) {
							continue;
						} else {
							destServiceType = ServiceType.UNKNOWN_CLOUD;
						}
					}

					String statId2 = TransactionFlowStatisticsUtils.makeId(src, srcServiceType, dest, destServiceType);
					TransactionFlowStatistics stat2 = (statisticsMap.containsKey(statId2) ? statisticsMap.get(statId2) : new TransactionFlowStatistics(src, srcServiceType, dest, destServiceType));
					
//					ResponseHistogram histogram2 = stat2.getHistogram();
					int slot2 = destServiceType.getHistogram().findHistogramSlot(spanEvent.getEndElapsed()).getSlotTime();
//					histogram2.addSample((short) slot2, 1);
//					
//					// host 정보 추가.
//					stat2.addToHost(spanEvent.getEndPoint());
					
					// stat2.addSample((dest == null) ? spanEvent.getEndPoint() : dest, destServiceType.getCode(), (short) slot2, 1);
					stat2.addSample(spanEvent.getEndPoint(), destServiceType.getCode(), (short) slot2, 1);
					
					// agent 정보추가.
					// destination의 agent정보 알 수 없음.
					
					statisticsData.add(stat2);
					statisticsMap.put(statId2, stat2);
					
					// link timeseries statistics추가.
//					tr.add(statId2, span.getStartTime() + spanEvent.getStartElapsed(), spanEvent.getEndElapsed() , 1L);
					
					// application timeseries statistics
//					tr.add(spanEvent.getDestinationId(), span.getCollectorAcceptTime(), span.getElapsed(), 1L);
					
//					System.out.println("\n\t----------------------------------");
//					System.out.println("\t@src\t\t" + src);
//					System.out.println("\t@srcType\t\t" + srcServiceType);
//					System.out.println("\t@dest\t\t" + dest);
//					System.out.println("\t@destType\t\t" + destServiceType);
//					System.out.println("\t@spanEv\t\t" + spanEvent);
//					System.out.println("\t@stat\t\t" + stat2);
//					System.out.println("\t----------------------------------\n\n");
				}
			}
		}
		
		// mark agent info
		Iterator<TransactionFlowStatistics> iterator = statisticsData.iterator();
		while(iterator.hasNext()) {
			TransactionFlowStatistics stat = iterator.next();
			fillAdditionalInfo(stat, from, to);
		}
		
		ApplicationMap map = new ApplicationMap(statisticsData).build();

//		map.setTimeseriesResponses(tr);
		
		watch.stop();
		logger.debug("Select filtered application map elapsed. {}ms", watch.getTotalTimeMillis());

		return map;
	}

    private Collection<TransactionId> filter(List<TransactionId> transactionIdList) {
        List<TransactionId> crashKey = new ArrayList<TransactionId>();
        Map<TransactionId, Object> filterMap = new LinkedHashMap<TransactionId, Object>(transactionIdList.size());
        for (TransactionId transactionId: transactionIdList) {
            Object old = filterMap.put(transactionId, V);
            if (old != null) {
                crashKey.add(transactionId);
            }
        }
        if (transactionIdList.size() != 0) {
            Set<TransactionId> transactionIds = filterMap.keySet();
            logger.info("transactionId crash found. original:{} filer:{} crashKey:{}", transactionIdList.size(), filterMap.size(), crashKey);
            return transactionIds;
        }
        return transactionIdList;
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
}
