package com.nhn.pinpoint.web.service;

import java.util.ArrayList;
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

import com.nhn.pinpoint.web.calltree.server.AgentIdNodeSelector;
import com.nhn.pinpoint.web.calltree.server.ApplicationIdNodeSelector;
import com.nhn.pinpoint.web.calltree.server.NodeSelector;
import com.nhn.pinpoint.web.calltree.server.ServerCallTree;
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
import com.nhn.pinpoint.web.vo.TraceId;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AnnotationBo;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

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
	
	@Override
	public List<Application> selectAllApplicationNames() {
		return applicationIndexDao.selectAllApplicationNames();
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
	@Override
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIdSet, Filter filter) {
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
		
		Set<String> endPoints = createUniqueEndpoint(transaction);
		ServerCallTree tree = createServerCallTree(transaction, new ApplicationIdNodeSelector());
		
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
	private ServerCallTree createServerCallTree(List<SpanBo> transaction, NodeSelector nodeSelector) {
		ServerCallTree serverCallTree = new ServerCallTree(nodeSelector);
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

	@Override
	public BusinessTransactions selectBusinessTransactions(Set<TraceId> traceIds, String applicationName, long from, long to, Filter filter) {
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
	public LinkStatistics linkStatisticsDetail(long from, long to, Set<TraceId> traceIdSet, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType, Filter filter) {
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
				for (SpanEventBo ev : spanEventBoList) {
					if (destServiceType == ev.getServiceType().getCode() && destApplicationName.equals(ev.getDestinationId())) {
						// find exception
						boolean hasException = false;
						List<AnnotationBo> annList = ev.getAnnotationBoList();
						for (AnnotationBo ann : annList) {
							if (ann.getKey() == AnnotationKey.EXCEPTION.getCode()) {
								hasException = true;
								break;
							}
						}

						// add sample
						// TODO : 실제값 대신 slot값을 넣어야 함.
						statistics.addSample(span.getStartTime() + ev.getStartElapsed(), ev.getEndElapsed(), 1, hasException);
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
}
