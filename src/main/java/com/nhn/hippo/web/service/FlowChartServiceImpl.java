package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.hippo.web.calltree.server.AgentIdNodeSelector;
import com.nhn.hippo.web.calltree.server.ApplicationIdNodeSelector;
import com.nhn.hippo.web.calltree.server.NodeSelector;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.dao.ApplicationIndexDao;
import com.nhn.hippo.web.dao.ApplicationTraceIndexDao;
import com.nhn.hippo.web.dao.TraceDao;
import com.nhn.hippo.web.filter.Filter;
import com.nhn.hippo.web.vo.Application;
import com.nhn.hippo.web.vo.BusinessTransactions;
import com.nhn.hippo.web.vo.ClientStatistics;
import com.nhn.hippo.web.vo.TraceId;
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
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

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
			for (SpanBo span : t) {
				if (filter.include(span)) {
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
	public BusinessTransactions selectBusinessTransactions(Set<TraceId> traceIds, String applicationName, long from, long to) {
		List<List<SpanBo>> traceList = this.traceDao.selectSpans(traceIds);

		BusinessTransactions businessTransactions = new BusinessTransactions();
		for (List<SpanBo> trace : traceList) {
			for (SpanBo spanBo : trace) {
				// 해당 application으로 인입된 요청만 보여준다.
				if (applicationName.equals(spanBo.getApplicationId())) {
					businessTransactions.add(spanBo);
				}
			}
		}

		return businessTransactions;
	}
}
