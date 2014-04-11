package com.nhn.pinpoint.web.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.HistogramSlot;
import com.nhn.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.nhn.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.nhn.pinpoint.web.util.TimeWindow;
import com.nhn.pinpoint.web.util.TimeWindowOneMinuteSampler;
import com.nhn.pinpoint.web.dao.*;
import com.nhn.pinpoint.web.vo.*;
import com.nhn.pinpoint.web.vo.scatter.ApplicationScatterScanResult;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.nhn.pinpoint.common.ServiceType;
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
    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Autowired
    private AgentInfoService agentInfoService;


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
    public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, SelectedScatterArea area, int limit) {
    	if (applicationName == null) {
    		throw new NullPointerException("applicationName must not be null");
    	}
    	if (area == null) {
    		throw new NullPointerException("area must not be null");
    	}
    	if (logger.isTraceEnabled()) {
    		logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}", applicationName, area);
    	}
    	
    	return this.applicationTraceIndexDao.scanTraceIndex(applicationName, area, limit);
    }

    @Override
    @Deprecated
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
        return selectApplicationMap(transactionIdList, range, range, Filter.NONE);
    }

    /**
     * filtered application map
     */
    @Override
    public ApplicationMap selectApplicationMap(List<TransactionId> transactionIdList, Range originalRange, Range scanRange, Filter filter) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        StopWatch watch = new StopWatch();
        watch.start();

        final List<List<SpanBo>> filterList = selectFilteredSpan(transactionIdList, filter);

        ApplicationMap map = createMap(originalRange, scanRange, filterList);

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

    private ApplicationMap createMap(Range range, Range scanRange, List<List<SpanBo>> filterList) {

        // Window의 설정은 따로 inject받던지 해야 될듯함.
        final TimeWindow window = new TimeWindow(range, TimeWindowOneMinuteSampler.SAMPLER);


        final LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();

        final DotExtractor dotExtractor = new DotExtractor(scanRange);
        final ResponseHistogramBuilder mapHistogramSummary = new ResponseHistogramBuilder(range);
        /**
         * 통계정보로 변환한다.
         */
        for (List<SpanBo> transaction : filterList) {
            final Map<Long, SpanBo> transactionSpanMap = checkDuplicatedSpanId(transaction);

            for (SpanBo span : transaction) {
                final Application parentApplication = createParentApplication(span, transactionSpanMap);
                final Application spanApplication = new Application(span.getApplicationId(), span.getServiceType());

                // SPAN의 respoinseTime의 통계를 저장한다.
                recordSpanResponseTime(spanApplication, span, mapHistogramSummary, span.getCollectorAcceptTime());

                // 사실상 여기서 걸리는것은 span의 serviceType이 잘못되었다고 할수 있음.
                if (!spanApplication.getServiceType().isRecordStatistics() || spanApplication.getServiceType().isRpcClient()) {
                    logger.warn("invalid span application:{}", spanApplication);
                    continue;
                }

                final short slotTime = getHistogramSlotTime(span, spanApplication.getServiceType());
                // link의 통계값에 collector acceptor time을 넣는것이 맞는것인지는 다시 생각해볼 필요가 있음.
                // 통계값의 window의 time으로 전환해야함. 안그러면 slot이 맞지 않아 oom이 발생할수 있음.
                long timestamp = window.refineTimestamp(span.getCollectorAcceptTime());

                if (parentApplication.getServiceType() == ServiceType.USER) {
                    // 정방향 데이터
                    if (logger.isTraceEnabled()) {
                        logger.trace("span user:{} {} -> span:{} {}", parentApplication, span.getAgentId(), spanApplication, span.getAgentId());
                    }
                    final LinkDataMap sourceLinkData = linkDataDuplexMap.getSourceLinkDataMap();
                    sourceLinkData.addLinkData(parentApplication, span.getAgentId(), spanApplication,  span.getAgentId(), timestamp, slotTime, 1);

                    if (logger.isTraceEnabled()) {
                        logger.trace("span target user:{} {} -> span:{} {}", parentApplication, span.getAgentId(), spanApplication, span.getAgentId());
                    }
                    // 역관계 데이터
                    final LinkDataMap targetLinkDataMap = linkDataDuplexMap.getTargetLinkDataMap();
                    targetLinkDataMap.addLinkData(parentApplication, span.getAgentId(), spanApplication, span.getAgentId(), timestamp, slotTime, 1);
                } else {
                    // 역관계 데이터
                    if (logger.isTraceEnabled()) {
                        logger.trace("span target parent:{} {} -> span:{} {}", parentApplication, span.getAgentId(), spanApplication, span.getAgentId());
                    }
                    final LinkDataMap targetLinkDataMap = linkDataDuplexMap.getTargetLinkDataMap();
                    targetLinkDataMap.addLinkData(parentApplication, span.getAgentId(), spanApplication, span.getAgentId(), timestamp, slotTime, 1);
                }


                addNodeFromSpanEvent(span, window, linkDataDuplexMap, transactionSpanMap);
                dotExtractor.addDot(span);
            }
        }
        List<ApplicationScatterScanResult> applicationScatterScanResult = dotExtractor.getApplicationScatterScanResult();

        ApplicationMapBuilder applicationMapBuilder = new ApplicationMapBuilder(range);
        mapHistogramSummary.build();
        ApplicationMap map = applicationMapBuilder.build(linkDataDuplexMap, agentInfoService, mapHistogramSummary);

        map.setApplicationScatterScanResult(applicationScatterScanResult);

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

    private void recordSpanResponseTime(Application application, SpanBo span, ResponseHistogramBuilder responseHistogramBuilder, long timeStamp) {
        responseHistogramBuilder.addHistogram(application, span, timeStamp);
    }


    private void addNodeFromSpanEvent(SpanBo span, TimeWindow window, LinkDataDuplexMap linkDataDuplexMap, Map<Long, SpanBo> transactionSpanMap) {
        /**
         * span event의 statistics추가.
         */
        final List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }
        final Application srcApplication = new Application(span.getApplicationId(), span.getServiceType());

        LinkDataMap sourceLinkDataMap = linkDataDuplexMap.getSourceLinkDataMap();
        for (SpanEventBo spanEvent : spanEventBoList) {

            ServiceType destServiceType = spanEvent.getServiceType();
            if (!destServiceType.isRecordStatistics()) {
                // internal 메소드
                continue;
            }
            // rpc client이면서 acceptor가 없으면 unknown으로 변환시킨다.
            // 내가 아는 next spanid를 spanid로 가진 span이 있으면 acceptor가 존재하는 셈.
            // acceptor check로직
            if (destServiceType.isRpcClient()) {
                if (!transactionSpanMap.containsKey(spanEvent.getNextSpanId())) {
                    destServiceType = ServiceType.UNKNOWN;
                }
            }

            final String dest = spanEvent.getDestinationId();
            final Application destApplication = new Application(dest, destServiceType);

            final short slotTime = getHistogramSlotTime(spanEvent, destServiceType);

            // FIXME
            final long spanEventTimeStamp = window.refineTimestamp(span.getStartTime() + spanEvent.getStartElapsed());
            if (logger.isTraceEnabled()) {
                logger.trace("spanEvent  src:{} {} -> dest:{} {}", srcApplication, span.getAgentId(), destApplication, spanEvent.getEndPoint());
            }
            sourceLinkDataMap.addLinkData(srcApplication, span.getAgentId(), destApplication, spanEvent.getEndPoint(), spanEventTimeStamp, slotTime, 1);
        }
    }

    private Application createParentApplication(SpanBo span, Map<Long, SpanBo> transactionSpanMap) {
        final SpanBo parentSpan = transactionSpanMap.get(span.getParentSpanId());
        if (span.isRoot() || parentSpan == null) {
            String applicationName = span.getApplicationId();
            ServiceType serviceType = ServiceType.USER;
            return new Application(applicationName, serviceType);
        } else {
            String parentApplicationName = parentSpan.getApplicationId();
            ServiceType serviceType = parentSpan.getServiceType();
            return new Application(parentApplicationName, serviceType);
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


}
