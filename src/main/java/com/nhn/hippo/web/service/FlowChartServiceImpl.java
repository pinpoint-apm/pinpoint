package com.nhn.hippo.web.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.hippo.web.calltree.rpc.RPCCallTree;
import com.nhn.hippo.web.calltree.server.ServerCallTree;
import com.nhn.hippo.web.dao.ApplicationIndexDao;
import com.nhn.hippo.web.dao.ApplicationTraceIndexDao;
import com.nhn.hippo.web.dao.RootTraceIndexDao;
import com.nhn.hippo.web.dao.TerminalStatisticsDao;
import com.nhn.hippo.web.dao.TraceDao;
import com.nhn.hippo.web.dao.TraceIndexDao;
import com.nhn.hippo.web.vo.TerminalRequest;
import com.nhn.hippo.web.vo.TraceId;
import com.nhn.hippo.web.vo.scatter.Dot;
import com.profiler.common.ServiceType;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;
import com.profiler.common.hbase.HBaseClient;
import com.profiler.common.hbase.HBaseQuery;
import com.profiler.common.hbase.HBaseQuery.HbaseColumn;
import com.profiler.common.hbase.HBaseTables;

/**
 * @author netspider
 */
@Service
public class FlowChartServiceImpl implements FlowChartService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("hbaseClient")
	HBaseClient client;

	@Autowired
	private TraceDao traceDao;

	@Autowired
	private RootTraceIndexDao rootTraceIndexDao;

	@Autowired
	private TraceIndexDao traceIndexDao;

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

	@Autowired
	private TerminalStatisticsDao terminalStatisticsDao;

	@Override
	public List<String> selectAllApplicationNames() {
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
			List<List<byte[]>> bytes = this.traceIndexDao.scanTraceIndex(agentIds[0], from, to);
			Set<TraceId> result = new HashSet<TraceId>();
			for (List<byte[]> list : bytes) {
				for (byte[] traceId : list) {
					TraceId tid = new TraceId(traceId);
					result.add(tid);
					logger.trace("traceid:{}", tid);
				}
			}
			return result;
		} else {
			// multi scan 가능한 동일 open htable 에서 액세스함.
			List<List<List<byte[]>>> multiScan = this.traceIndexDao.multiScanTraceIndex(agentIds, from, to);
			Set<TraceId> result = new HashSet<TraceId>();
			for (List<List<byte[]>> list : multiScan) {
				for (List<byte[]> scan : list) {
					for (byte[] traceId : scan) {
						result.add(new TraceId(traceId));
					}
				}
			}
			return result;
		}
	}

	@Override
	public RPCCallTree selectRPCCallTree(Set<TraceId> traceIds) {
		final RPCCallTree tree = new RPCCallTree();
		List<List<SpanBo>> traces = this.traceDao.selectSpans(traceIds);
		for (List<SpanBo> transaction : traces) {
			for (SpanBo eachTransaction : transaction) {
				tree.addSpan(eachTransaction);
			}
		}
		return tree.build();
	}

	@Override
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIds) {
		final ServerCallTree tree = new ServerCallTree();

		List<List<SpanBo>> traces = this.traceDao.selectSpans(traceIds);

		for (List<SpanBo> transaction : traces) {
			// List<SpanBo> processed = refine(transaction);
			markRecursiveCall(transaction);
			for (SpanBo eachTransaction : transaction) {
				tree.addSpan(eachTransaction);
			}
		}
		return tree.build();
	}

	/**
	 * makes call tree of transaction detail view
	 */
	@Override
	public ServerCallTree selectServerCallTree(TraceId traceId) {
		final ServerCallTree tree = new ServerCallTree();

		List<SpanBo> transaction = this.traceDao.selectSpans(traceId);

		Set<String> endPoints = new HashSet<String>();

		// List<SpanBo> processed = refine(transaction);
		markRecursiveCall(transaction);
		for (SpanBo eachTransaction : transaction) {
			tree.addSpan(eachTransaction);
			endPoints.add(eachTransaction.getEndPoint());
		}

		for (SpanBo eachTransaction : transaction) {
			List<SubSpanBo> subSpanList = eachTransaction.getSubSpanList();

			if (subSpanList == null)
				continue;

			for (SubSpanBo subTransaction : subSpanList) {
				// skip internal method
				if (subTransaction.getServiceType() == ServiceType.INTERNAL_METHOD) {
					continue;
				}
				
				// remove subspan of the rpc client
				if (!endPoints.contains(subTransaction.getEndPoint())) {
					// this is unknown cloud
					tree.addSubSpan(subTransaction);
				}
			}
		}

		return tree.build();
	}

	/**
	 * makes call tree of main view
	 */
	@Override
	public ServerCallTree selectServerCallTree(Set<TraceId> traceIds, String applicationName, long from, long to) {
		final Map<String, ServiceType> terminalQueryParams = new HashMap<String, ServiceType>();
		final ServerCallTree tree = new ServerCallTree();

		StopWatch watch = new StopWatch();
		watch.start("scanNonTerminalSpans");

		// fetch non-terminal spans
		List<List<SpanBo>> traces = this.traceDao.selectSpans(traceIds);

		watch.stop();
		int totalNonTerminalSpansCount = 0;

		Set<String> endPoints = new HashSet<String>();

		// processing spans
		for (List<SpanBo> transaction : traces) {
			totalNonTerminalSpansCount += transaction.size();

			// List<SpanBo> processed = refine(transaction);
			markRecursiveCall(transaction);
			for (SpanBo eachTransaction : transaction) {
				tree.addSpan(eachTransaction);

				// make query param
				terminalQueryParams.put(eachTransaction.getServiceName(), eachTransaction.getServiceType());

				endPoints.add(eachTransaction.getEndPoint());
			}
		}

		if (logger.isInfoEnabled()) {
			logger.info("Fetch non-terminal spans elapsed : {}ms, {} traces, {} spans", new Object[] { watch.getLastTaskTimeMillis(), traces.size(), totalNonTerminalSpansCount });
		}

		watch.start("scanTerminalStatistics");

		// fetch terminal info
		for (Entry<String, ServiceType> param : terminalQueryParams.entrySet()) {
			ServiceType svcType = param.getValue();
			if (!svcType.isRpcClient() && !svcType.isUnknown() && !svcType.isTerminal()) {
				long start = System.currentTimeMillis();
				List<List<TerminalRequest>> terminals = terminalStatisticsDao.selectTerminal(param.getKey(), from, to);
				logger.info("	Fetch terminals of {} : {}ms", param.getKey(), System.currentTimeMillis() - start);

				for (List<TerminalRequest> terminal : terminals) {
					for (TerminalRequest t : terminal) {
						// TODO 임시방편
						if (!endPoints.contains(t.getTo())) {
							if (ServiceType.parse(t.getToServiceType()).isRpcClient()) {
								t.setToServiceType(ServiceType.UNKNOWN_CLOUD.getCode());
							}
							tree.addTerminal(t);
						}
					}
				}
			}
		}

		watch.stop();
		logger.info("Fetch terminal statistics elapsed : {}ms", watch.getLastTaskTimeMillis());

		return tree.build();
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

	@Deprecated
	private List<SpanBo> refine(final List<SpanBo> list) {
		for (int i = 0; i < list.size(); i++) {
			SpanBo span = list.get(i);

			if (span.getServiceType().isRpcClient()) {
				SpanBo child = findChildSpan(list, span);

				if (child != null) {
					child.setParentSpanId(span.getParentSpanId());
					child.getAnnotationBoList().addAll(span.getAnnotationBoList());
					list.remove(i);
					i--;
					continue;
				} else {
					// using as a terminal node.
					span.setServiceName(span.getEndPoint());
					span.setServiceType(ServiceType.UNKNOWN_CLOUD);
				}
			}
		}
		return list;
	}

	private void markRecursiveCall(final List<SpanBo> list) {
		for (int i = 0; i < list.size(); i++) {
			SpanBo a = list.get(i);
			for (int j = 0; j < list.size(); j++) {
				if (i == j)
					continue;
				SpanBo b = list.get(j);
				if (a.getServiceName().equals(b.getServiceName()) && a.getSpanId() == b.getParentSpanId()) {
					a.increaseRecursiveCallCount();
				}
			}
		}
	}

	@Override
	public Set<TraceId> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to) {
		if (applicationName == null) {
			throw new NullPointerException("applicationName");
		}

		if (logger.isTraceEnabled()) {
			logger.trace("scan {}, {}, {}", new Object[] { applicationName, from, to });
		}

		List<List<byte[]>> bytes = this.applicationTraceIndexDao.scanTraceIndex(applicationName, from, to);
		Set<TraceId> result = new HashSet<TraceId>();
		for (List<byte[]> list : bytes) {
			for (byte[] traceId : list) {
				TraceId tid = new TraceId(traceId);
				result.add(tid);
				logger.trace("traceid:{}", tid);
			}
		}
		return result;
	}

	@Override
	public String[] selectAgentIds(String[] hosts) {
		List<HbaseColumn> column = new ArrayList<HBaseQuery.HbaseColumn>();
		column.add(new HbaseColumn("Agents", "AgentID"));

		HBaseQuery query = new HBaseQuery(HBaseTables.APPLICATION_INDEX, null, null, column);
		Iterator<Map<String, byte[]>> iterator = client.getHBaseData(query);

		if (logger.isDebugEnabled()) {
			while (iterator.hasNext()) {
				logger.debug("selectedAgentId={}", iterator.next());
			}
			logger.debug("!!!==============WARNING==============!!!");
			logger.debug("!!! selectAgentIds IS NOT IMPLEMENTED !!!");
			logger.debug("!!!===================================!!!");
		}

		return hosts;
	}

	@Override
	public Iterator<Dot> selectScatterData(String applicationName, long from, long to) {
		List<List<Dot>> scanTrace = applicationTraceIndexDao.scanTraceScatter(applicationName, from, to);

		List<Dot> list = new ArrayList<Dot>();

		for (List<Dot> l : scanTrace) {
			for (Dot dot : l) {
				list.add(dot);
			}
		}

		return list.iterator();
	}
}
