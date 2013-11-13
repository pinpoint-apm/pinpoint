package com.nhn.pinpoint.web.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.pinpoint.common.Histogram;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;
import com.nhn.pinpoint.web.applicationmap.ApplicationMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.TransactionFlowStatistics;
import com.nhn.pinpoint.web.dao.AgentInfoDao;
import com.nhn.pinpoint.web.dao.ApplicationIndexDao;
import com.nhn.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.nhn.pinpoint.web.dao.TraceDao;
import com.nhn.pinpoint.web.filter.Filter;
import com.nhn.pinpoint.web.vo.LimitedScanResult;
import com.nhn.pinpoint.web.vo.LinkStatistics;
import com.nhn.pinpoint.web.vo.TimeSeriesStore;
import com.nhn.pinpoint.web.vo.TimeSeriesStoreImpl2;
import com.nhn.pinpoint.web.vo.TransactionId;

/**
 * @author netspider
 * @author emeroad
 */
@Service
public class FilteredApplicationMapServiceImpl implements FilteredApplicationMapService {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TraceDao traceDao;

	@Autowired
	private ApplicationIndexDao applicationIndexDao;

	@Autowired
	private ApplicationTraceIndexDao applicationTraceIndexDao;

	@Autowired
	private AgentInfoDao agentInfoDao;

	private static final Object V = new Object();

	@Override
	public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, long from, long to, int limit) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }

        if (logger.isTraceEnabled()) {
			logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}, {}", applicationName, from, to);
		}

		return this.applicationTraceIndexDao.scanTraceIndex(applicationName, from, to, limit);
	}

	@Override
	public LinkStatistics linkStatistics(long from, long to, List<TransactionId> traceIdSet, String srcApplicationName, short srcServiceType, String destApplicationName, short destServiceType, Filter filter) {
        if (srcApplicationName == null) {
            throw new NullPointerException("srcApplicationName must not be null");
        }
        if (destApplicationName == null) {
            throw new NullPointerException("destApplicationName must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        StopWatch watch = new StopWatch();
		watch.start();

		List<List<SpanBo>> originalList = this.traceDao.selectAllSpans(traceIdSet);
        List<SpanBo> filteredTransactionList = filterList(originalList, filter);

		LinkStatistics statistics = new LinkStatistics(from, to);

		// TODO fromToFilter처럼. node의 타입에 따른 처리 필요함.

		// scan transaction list
		for (SpanBo span : filteredTransactionList) {
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

    private List<SpanBo> filterList(List<List<SpanBo>> transactionList, Filter filter) {
        final List<SpanBo> filteredResult = new ArrayList<SpanBo>();
        for (List<SpanBo> transaction : transactionList) {
            if (filter.include(transaction)) {
                filteredResult.addAll(transaction);
            }
        }
        return filteredResult;
    }

    private List<List<SpanBo>> filterList2(List<List<SpanBo>> transactionList, Filter filter) {
        final List<List<SpanBo>> filteredResult = new ArrayList<List<SpanBo>>();
        for (List<SpanBo> transaction : transactionList) {
            if (filter.include(transaction)) {
                filteredResult.add(transaction);
            }
        }
        return filteredResult;
    }

    @Override
	public ApplicationMap selectApplicationMap(TransactionId transactionId) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        List<TransactionId> transactionIdList = new ArrayList<TransactionId>();
		transactionIdList.add(transactionId);
		// FIXME from,to -1 땜방임. 
		return selectApplicationMap(transactionIdList, -1L, -1L, Filter.NONE);
	}

	/**
	 * filtered application map
	 */
	@Override
	public ApplicationMap selectApplicationMap(List<TransactionId> transactionIdList, long from, long to, Filter filter) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        StopWatch watch = new StopWatch();
		watch.start();

		// 개별 객체를 각각 보고 재귀 내용을 삭제함.
		// 향후 tree base로 충돌구간을 점검하여 없앨 경우 여기서 filter를 치면 안됨.
		final Collection<TransactionId> recursiveFilterList = recursiveCallFilter(transactionIdList);
		// FIXME 나중에 List<Span>을 순회하면서 실행할 process chain을 두는것도 괜찮을듯.
		final List<List<SpanBo>> originalList = this.traceDao.selectAllSpans(recursiveFilterList);
        final List<List<SpanBo>> filterList = filterList2(originalList, filter);

        Set<TransactionFlowStatistics> statisticsData = new HashSet<TransactionFlowStatistics>();
		Map<NodeId, TransactionFlowStatistics> statisticsMap = new HashMap<NodeId, TransactionFlowStatistics>();


		final TimeSeriesStore timeSeriesStore = new TimeSeriesStoreImpl2(from, to);
		/**
		 * 통계정보로 변환한다.
		 */
		for (List<SpanBo> transaction : filterList) {
            final Map<Long, SpanBo> transactionSpanMap = new HashMap<Long, SpanBo>(transactionIdList.size());
			for (SpanBo span : transaction) {
                final SpanBo old = transactionSpanMap.put(span.getSpanId(), span);
                logger.warn("duplicated span found:{}", old);
            }

			for (SpanBo span : transaction) {
                final Node srcNode = createNode(span, transactionSpanMap);
                final Node destNode = new Node(span.getApplicationId(), span.getServiceType());
                // TODO API의 의미가 link라고 하기는 좀 애매함. 변경필요.
				if (destNode.isLink()) {
					continue;
				}

                final ComplexNodeId statId = new ComplexNodeId(srcNode, destNode);
				TransactionFlowStatistics stat;
                if (statisticsMap.containsKey(statId))  {
                    stat = statisticsMap.get(statId);
                }
                else {
                    stat = new TransactionFlowStatistics(srcNode.getName(), srcNode.getServiceType(), destNode.getName(), destNode.getServiceType());
                }

                final int slot = getHistogramSlot(span, destNode.getServiceType());

                stat.addSample(destNode.getName(), destNode.getServiceType().getCode(), (short) slot, 1);

				statisticsData.add(stat);
				statisticsMap.put(statId, stat);

				// link timeseries statistics추가.
				timeSeriesStore.add(statId, span.getCollectorAcceptTime(), slot, 1L, span.hasException());
				
				// application timeseries statistics
                NodeId key = new ComplexNodeId(Node.EMPTY, new Node(span.getApplicationId(), span.getServiceType()));
				timeSeriesStore.add(key, span.getCollectorAcceptTime(), slot, 1L, span.hasException());

                addNodeFromSpanEvent(statisticsData, statisticsMap, timeSeriesStore, transactionSpanMap, span);
            }
		}

		// mark agent info
		for (TransactionFlowStatistics stat : statisticsData) {
			fillAdditionalInfo(stat);
		}

		ApplicationMap map = new ApplicationMap(statisticsData).build();
		map.setTimeSeriesStore(timeSeriesStore);

		watch.stop();
		logger.debug("Select filtered application map elapsed. {}ms", watch.getTotalTimeMillis());

		return map;
	}




    private void addNodeFromSpanEvent(Set<TransactionFlowStatistics> statisticsData, Map<NodeId, TransactionFlowStatistics> statisticsMap, TimeSeriesStore timeSeriesStore, Map<Long, SpanBo> transactionSpanMap, SpanBo span) {
        /**
         * span event의 statistics추가.
         */
        final List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }
        final Node srcNode = new Node(span.getApplicationId(), span.getServiceType());

        for (SpanEventBo spanEvent : spanEventBoList) {
            final String dest = spanEvent.getDestinationId();
            ServiceType destServiceType = spanEvent.getServiceType();

            if (!destServiceType.isRecordStatistics() /*|| destServiceType.isRpcClient()*/) {
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

            final NodeId spanEventStatId = new ComplexNodeId(srcNode, new Node(dest, destServiceType));
            TransactionFlowStatistics stat2;
            if (statisticsMap.containsKey(spanEventStatId)) {
                stat2 = statisticsMap.get(spanEventStatId);
            } else {
                stat2 = new TransactionFlowStatistics(srcNode.getName(), srcNode.getServiceType(), dest, destServiceType);
            }

            final int slot2 = getHistogramSlot(spanEvent, destServiceType);

            // FIXME
            // stat2.addSample((dest == null) ? spanEvent.getEndPoint() : dest, destServiceType.getCode(), (short) slot2, 1);
            stat2.addSample(spanEvent.getEndPoint(), destServiceType.getCode(), (short) slot2, 1);

            // agent 정보추가. destination의 agent정보 알 수 없음.
            statisticsData.add(stat2);
            statisticsMap.put(spanEventStatId, stat2);

            // link timeseries statistics추가.
            timeSeriesStore.add(spanEventStatId, span.getStartTime() + spanEvent.getStartElapsed(), slot2, 1L, spanEvent.hasException());

            // application timeseries statistics
            NodeId key = new ComplexNodeId(Node.EMPTY, new Node(spanEvent.getDestinationId(), spanEvent.getServiceType()));
            timeSeriesStore.add(key, span.getCollectorAcceptTime(), slot2, 1L, spanEvent.hasException());
        }
    }

    private Node createNode(SpanBo span, Map<Long, SpanBo> transactionSpanMap) {
        SpanBo parentSpan = transactionSpanMap.get(span.getParentSpanId());
        if (span.isRoot() || parentSpan == null) {
            String src = span.getApplicationId();
            ServiceType srcServiceType = ServiceType.CLIENT;
            return new Node(src, srcServiceType);
        } else {
            String src = parentSpan.getApplicationId();
            ServiceType serviceType = parentSpan.getServiceType();
            return new Node(src, serviceType);
        }
    }

    private int getHistogramSlot(SpanEventBo spanEvent, ServiceType serviceType) {
        return getHistogramSlot(spanEvent.hasException(), spanEvent.getEndElapsed(), serviceType);
    }

    private int getHistogramSlot(SpanBo span, ServiceType serviceType) {
        return getHistogramSlot(span.hasException(), span.getElapsed(), serviceType);
    }

    private int getHistogramSlot(boolean hasException, int elapsedTime, ServiceType serviceType) {
        if (hasException) {
            return Histogram.ERROR_SLOT.getSlotTime();
        } else {
            return serviceType.getHistogram().findHistogramSlot(elapsedTime).getSlotTime();
        }
    }

    private Collection<TransactionId> recursiveCallFilter(List<TransactionId> transactionIdList) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }

        List<TransactionId> crashKey = new ArrayList<TransactionId>();
		Map<TransactionId, Object> filterMap = new LinkedHashMap<TransactionId, Object>(transactionIdList.size());
		for (TransactionId transactionId : transactionIdList) {
			Object old = filterMap.put(transactionId, V);
			if (old != null) {
				crashKey.add(transactionId);
			}
		}
		if (crashKey.size() != 0) {
			Set<TransactionId> filteredTrasnactionId = filterMap.keySet();
			logger.info("transactionId crash found. original:{} filter:{} crashKey:{}", transactionIdList.size(), filteredTrasnactionId.size(), crashKey);
			return filteredTrasnactionId;
		}
		return transactionIdList;
	}

	private void fillAdditionalInfo(TransactionFlowStatistics stat) {
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
