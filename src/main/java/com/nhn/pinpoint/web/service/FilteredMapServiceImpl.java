package com.nhn.pinpoint.web.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatisticsData;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowOneMinuteSampler;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkStatistics;
import com.nhn.pinpoint.web.dao.*;
import com.nhn.pinpoint.web.vo.*;
import org.apache.commons.collections.CollectionUtils;
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
import com.nhn.pinpoint.web.filter.Filter;

/**
 * @author netspider
 * @author emeroad
 */
@Service
public class FilteredMapServiceImpl implements FilteredMapService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TraceDao traceDao;

    @Autowired
    private ApplicationIndexDao applicationIndexDao;

    @Autowired
    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Autowired
    private AgentInfoDao agentInfoDao;

    @Autowired
    private MapResponseDao mapResponseDao;

    private static final Object V = new Object();

    @Override
    public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}", applicationName, range);
        }

        return this.applicationTraceIndexDao.scanTraceIndex(applicationName, range, limit);
    }

    @Override
    public LoadFactor linkStatistics(Range range, List<TransactionId> traceIdSet, Application sourceApplication, Application destinationApplication, Filter filter) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        if (destinationApplication == null) {
            throw new NullPointerException("destApplicationName must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        StopWatch watch = new StopWatch();
        watch.start();

        List<List<SpanBo>> originalList = this.traceDao.selectAllSpans(traceIdSet);
        List<SpanBo> filteredTransactionList = filterList(originalList, filter);

        LoadFactor statistics = new LoadFactor(range);

        // TODO fromToFilter처럼. node의 타입에 따른 처리 필요함.

        // scan transaction list
        for (SpanBo span : filteredTransactionList) {
            if (sourceApplication.equals(span.getApplicationId(), span.getServiceType())) {
                List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
                if (spanEventBoList == null) {
                    continue;
                }

                // find dest elapsed time
                for (SpanEventBo spanEventBo : spanEventBoList) {
                    if (destinationApplication.equals(spanEventBo.getDestinationId(), spanEventBo.getServiceType())) {
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
        Range range = new Range(-1, -1);
        return selectApplicationMap(transactionIdList, range, Filter.NONE);
    }

    /**
     * filtered application map
     */
    @Override
    public ApplicationMap selectApplicationMap(List<TransactionId> transactionIdList, Range range, Filter filter) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        StopWatch watch = new StopWatch();
        watch.start();

        final List<List<SpanBo>> filterList = selectFilteredSpan(transactionIdList, filter);

        ApplicationMap map = createMap(range, filterList);

        watch.stop();
        logger.debug("Select filtered application map elapsed. {}ms", watch.getTotalTimeMillis());

        return map;
    }

    private List<List<SpanBo>> selectFilteredSpan(List<TransactionId> transactionIdList, Filter filter) {
        // 개별 객체를 각각 보고 재귀 내용을 삭제함.
        // 향후 tree base로 충돌구간을 점검하여 없앨 경우 여기서 filter를 치면 안됨.
        final Collection<TransactionId> recursiveFilterList = recursiveCallFilter(transactionIdList);

        // FIXME 나중에 List<Span>을 순회하면서 실행할 process chain을 두는것도 괜찮을듯.
        final List<List<SpanBo>> originalList = this.traceDao.selectAllSpans(recursiveFilterList);

        return filterList2(originalList, filter);
    }

    private ApplicationMap createMap(Range range, List<List<SpanBo>> filterList) {
        // Window의 설정은 따로 inject받던지 해야 될듯함.
        final TimeWindow window = new TimeWindow(range, TimeWindowOneMinuteSampler.SAMPLER);

        final LinkStatisticsData linkStatisticsData = new LinkStatisticsData();
        final MapResponseHistogramSummary mapHistogramSummary = new MapResponseHistogramSummary(range);
        /**
         * 통계정보로 변환한다.
         */
        for (List<SpanBo> transaction : filterList) {
            final Map<Long, SpanBo> transactionSpanMap = checkDuplicatedSpanId(transaction);

            for (SpanBo span : transaction) {
                // SPAN의 respoinseTime의 통계를 저장한다.
                final Application srcApplication = createSourceApplication(span, transactionSpanMap);
                final Application destApplication = new Application(span.getApplicationId(), span.getServiceType());

                recordSpanResponseTime(destApplication, span, mapHistogramSummary, span.getCollectorAcceptTime());

                // record해야 되거나. rpc콜은 링크이다.
                if (!destApplication.getServiceType().isRecordStatistics() || destApplication.getServiceType().isRpcClient()) {
                    continue;
                }

                final short slotTime = getHistogramSlotTime(span, destApplication.getServiceType());
                // link의 통계값에 collector acceptor time을 넣는것이 맞는것인지는 다시 생각해볼 필요가 있음.
                // 통계값의 window의 time으로 전환해야함. 안그러면 slot이 맞지 않아 oom이 발생할수 있음.
                long timestamp = window.refineTimestamp(span.getCollectorAcceptTime());

                linkStatisticsData.addLinkData(srcApplication, span.getAgentId(), destApplication, destApplication.getName(), timestamp, slotTime, 1);


                addNodeFromSpanEvent(span, window, linkStatisticsData, transactionSpanMap);
            }
        }

        // mark agent info
        for (LinkStatistics stat : linkStatisticsData.getLinkStatData()) {
            fillAdditionalInfo(stat);
        }

        ApplicationMapBuilder applicationMapBuilder = new ApplicationMapBuilder(range);
        ApplicationMap map = applicationMapBuilder.build(linkStatisticsData);

        mapHistogramSummary.build();
        map.appendResponseTime(mapHistogramSummary);


        return map;
    }

    private Map<Long, SpanBo> checkDuplicatedSpanId(List<SpanBo> transaction) {
        final Map<Long, SpanBo> transactionSpanMap = new HashMap<Long, SpanBo>();
        for (SpanBo span : transaction) {
            final SpanBo old = transactionSpanMap.put(span.getSpanId(), span);
            if (old != null) {
                logger.warn("duplicated span found:{}", old);
            }
        }
        return transactionSpanMap;
    }

    private void recordSpanResponseTime(Application application, SpanBo span, MapResponseHistogramSummary mapResponseHistogramSummary, long timeStamp) {
        mapResponseHistogramSummary.addHistogram(application, span, timeStamp);
    }


    private void addNodeFromSpanEvent(SpanBo span, TimeWindow window, LinkStatisticsData linkStatMap, Map<Long, SpanBo> transactionSpanMap) {
        /**
         * span event의 statistics추가.
         */
        final List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }
        final Application srcApplication = new Application(span.getApplicationId(), span.getServiceType());

        for (SpanEventBo spanEvent : spanEventBoList) {

            ServiceType destServiceType = spanEvent.getServiceType();
            if (!destServiceType.isRecordStatistics() /*|| destServiceType.isRpcClient()*/) {
                continue;
            }
            // rpc client이면서 acceptor가 없으면 unknown으로 변환시킨다.
            // 내가 아는 next spanid를 spanid로 가진 span이 있으면 acceptor가 존재하는 셈.
            // acceptor check로직
            if (destServiceType.isRpcClient()) {
                if (transactionSpanMap.containsKey(spanEvent.getNextSpanId())) {
                    continue;
                } else {
                    destServiceType = ServiceType.UNKNOWN;
                }
            }

            final String dest = spanEvent.getDestinationId();
            final Application destApplication = new Application(dest, destServiceType);

            final short slotTime = getHistogramSlotTime(spanEvent, destServiceType);

            // FIXME
            // stat2.addCallHistogram((dest == null) ? spanEvent.getEndPoint() : dest, destServiceType.getCode(), (short) slot2, 1);
            final long spanEventTimeStamp = window.refineTimestamp(span.getStartTime() + spanEvent.getStartElapsed());
            linkStatMap.addLinkData(srcApplication, span.getAgentId(), destApplication, spanEvent.getEndPoint(), spanEventTimeStamp, slotTime, 1);
        }
    }

    private Application createSourceApplication(SpanBo span, Map<Long, SpanBo> transactionSpanMap) {
        final SpanBo parentSpan = transactionSpanMap.get(span.getParentSpanId());
        if (span.isRoot() || parentSpan == null) {
            String src = span.getApplicationId();
            ServiceType srcServiceType = ServiceType.USER; // ServiceType.CLIENT;
            return new Application(src, srcServiceType);
        } else {
            String src = parentSpan.getApplicationId();
            ServiceType serviceType = parentSpan.getServiceType();
            return new Application(src, serviceType);
        }
    }

    private short getHistogramSlotTime(SpanEventBo spanEvent, ServiceType serviceType) {
        return getHistogramSlotTime(spanEvent.hasException(), spanEvent.getEndElapsed(), serviceType);
    }

    private short getHistogramSlotTime(SpanBo span, ServiceType serviceType) {
        boolean allException = span.getErrCode() != 0;
        return getHistogramSlotTime(allException, span.getElapsed(), serviceType);
    }

    private short getHistogramSlotTime(boolean hasException, int elapsedTime, ServiceType serviceType) {
        if (hasException) {
            return serviceType.getHistogramSchema().getErrorSlot().getSlotTime();
        } else {
            final HistogramSchema schema = serviceType.getHistogramSchema();
            final HistogramSlot histogramSlot = schema.findHistogramSlot(elapsedTime);
            return histogramSlot.getSlotTime();
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

    private void fillAdditionalInfo(LinkStatistics stat) {
        final Application toApplication = stat.getToApplication();
        if (toApplication.getServiceType().isTerminal() || toApplication.getServiceType().isUnknown()) {
            return;
        }
        Set<AgentInfoBo> agentSet = selectAgents(toApplication.getName());
        if (agentSet.isEmpty()) {
            return;
        }
        // destination이 WAS이고 agent가 설치되어있으면 agentSet이 존재한다.
        stat.addToAgentSet(agentSet);
        logger.debug("fill agent info. {}, {}", toApplication, agentSet);
    }

    private Set<AgentInfoBo> selectAgents(String applicationId) {
        List<String> agentIds = applicationIndexDao.selectAgentIds(applicationId);
        Set<AgentInfoBo> agentSet = new HashSet<AgentInfoBo>();
        for (String agentId : agentIds) {
            // TODO 조회 시간대에 따라서 agent info row timestamp를 변경하여 조회해야하는지는 모르겠음.
            AgentInfoBo info = agentInfoDao.findAgentInfoBeforeStartTime(agentId, System.currentTimeMillis());
            agentSet.add(info);
        }
        return agentSet;
    }

}
