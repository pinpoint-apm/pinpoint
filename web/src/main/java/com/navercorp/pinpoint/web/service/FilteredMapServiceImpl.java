/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service;

import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanEventBo;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilderFactory;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterData;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterScanResult;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.ResponseHistogramBuilderNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.DefaultServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.link.LinkType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowDownSampler;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.LoadFactor;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseHistogramBuilder;
import com.navercorp.pinpoint.web.vo.SelectedScatterArea;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 */
@Service
public class FilteredMapServiceImpl implements FilteredMapService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    @Qualifier("hbaseTraceDaoFactory")
    private TraceDao traceDao;

    @Autowired
    private ApplicationTraceIndexDao applicationTraceIndexDao;

    @Autowired
    private ServiceTypeRegistryService registry;

    @Autowired
    private ApplicationFactory applicationFactory;
    
    @Autowired(required=false)
    private ServerMapDataFilter serverMapDataFilter;

    @Autowired
    private ApplicationMapBuilderFactory applicationMapBuilderFactory;

    private static final Object V = new Object();

    @Override
    public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit) {
        return selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit, true);
    }

    @Override
    public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit, boolean backwardDirection) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}", applicationName, range);
        }

        return this.applicationTraceIndexDao.scanTraceIndex(applicationName, range, limit, backwardDirection);
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

        // TODO need to handle these separately by node type (like fromToFilter)

        // scan transaction list
        for (SpanBo span : filteredTransactionList) {
            if (sourceApplication.equals(span.getApplicationId(), registry.findServiceType(span.getApplicationServiceType()))) {
                List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
                if (spanEventBoList == null) {
                    continue;
                }

                // find dest elapsed time
                for (SpanEventBo spanEventBo : spanEventBoList) {
                    if (destinationApplication.equals(spanEventBo.getDestinationId(), registry.findServiceType(spanEventBo.getServiceType()))) {
                        // find exception
                        boolean hasException = spanEventBo.hasException();
                        // add sample
                        // TODO : need timeslot value instead of the actual value
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
        final List<SpanBo> filteredResult = new ArrayList<>();
        for (List<SpanBo> transaction : transactionList) {
            if (filter.include(transaction)) {
                filteredResult.addAll(transaction);
            }
        }
        return filteredResult;
    }

    private List<List<SpanBo>> filterList2(List<List<SpanBo>> transactionList, Filter filter) {
        final List<List<SpanBo>> filteredResult = new ArrayList<>();
        for (List<SpanBo> transaction : transactionList) {
            if (filter.include(transaction)) {
                filteredResult.add(transaction);
            }
        }
        return filteredResult;
    }

    @Override
    public ApplicationMap selectApplicationMap(TransactionId transactionId, int version) {
        if (transactionId == null) {
            throw new NullPointerException("transactionId must not be null");
        }
        List<TransactionId> transactionIdList = new ArrayList<>();
        transactionIdList.add(transactionId);
        // FIXME from,to -1
        Range range = new Range(-1, -1);
        return selectApplicationMap(transactionIdList, range, range, Filter.NONE, version);
    }

    /**
     * filtered application map
     */
    @Override
    public ApplicationMap selectApplicationMap(List<TransactionId> transactionIdList, Range originalRange, Range scanRange, Filter filter, int version) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        StopWatch watch = new StopWatch();
        watch.start();

        final List<List<SpanBo>> filterList = selectFilteredSpan(transactionIdList, filter);

        DotExtractor dotExtractor = createDotExtractor(scanRange, filterList);
        ApplicationMap map = createMap(originalRange, scanRange, filterList, version);

        ApplicationMapWithScatterScanResult applicationMapWithScatterScanResult = new ApplicationMapWithScatterScanResult(map, dotExtractor.getApplicationScatterScanResult());

        watch.stop();
        logger.debug("Select filtered application map elapsed. {}ms", watch.getTotalTimeMillis());

        return applicationMapWithScatterScanResult;
    }

    @Override
    public ApplicationMap selectApplicationMapWithScatterData(List<TransactionId> transactionIdList, Range originalRange, Range scanRange, int xGroupUnit, int yGroupUnit, Filter filter, int version) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter must not be null");
        }

        StopWatch watch = new StopWatch();
        watch.start();

        final List<List<SpanBo>> filterList = selectFilteredSpan(transactionIdList, filter);

        DotExtractor dotExtractor = createDotExtractor(scanRange, filterList);
        ApplicationMap map = createMap(originalRange, scanRange, filterList, version);

        ApplicationMapWithScatterData applicationMapWithScatterData = new ApplicationMapWithScatterData(map, dotExtractor.getApplicationScatterData(originalRange.getFrom(), originalRange.getTo(), xGroupUnit, yGroupUnit));

        watch.stop();
        logger.debug("Select filtered application map elapsed. {}ms", watch.getTotalTimeMillis());

        return applicationMapWithScatterData;
    }

    private List<List<SpanBo>> selectFilteredSpan(List<TransactionId> transactionIdList, Filter filter) {
        // filters out recursive calls by looking at each objects
        // do not filter here if we change to a tree-based collision check in the future. 
        final List<TransactionId> recursiveFilterList = recursiveCallFilter(transactionIdList);

        // FIXME might be better to simply traverse the List<Span> and create a process chain for execution
        final List<List<SpanBo>> originalList = this.traceDao.selectAllSpans(recursiveFilterList);

        return filterList2(originalList, filter);
    }

    private DotExtractor createDotExtractor(Range scanRange, List<List<SpanBo>> filterList) {
        final DotExtractor dotExtractor = new DotExtractor(scanRange, applicationFactory);

        for (List<SpanBo> transaction : filterList) {
            for (SpanBo span : transaction) {
                final Application spanApplication = this.applicationFactory.createApplication(span.getApplicationId(), span.getApplicationServiceType());
                if (!spanApplication.getServiceType().isRecordStatistics() || spanApplication.getServiceType().isRpcClient()) {
                    continue;
                }

                dotExtractor.addDot(span);
            }
        }

        return dotExtractor;
    }

    private ApplicationMap createMap(Range range, Range scanRange, List<List<SpanBo>> filterList, int version) {
        // TODO inject TimeWindow from elsewhere
        final TimeWindow window = new TimeWindow(range, TimeWindowDownSampler.SAMPLER);


        final LinkDataDuplexMap linkDataDuplexMap = new LinkDataDuplexMap();

        final ResponseHistogramBuilder mapHistogramSummary = new ResponseHistogramBuilder(range);
        /*
         * Convert to statistical data
         */
        for (List<SpanBo> transaction : filterList) {
            final Map<Long, SpanBo> transactionSpanMap = checkDuplicatedSpanId(transaction);

            for (SpanBo span : transaction) {
                final Application parentApplication = createParentApplication(span, transactionSpanMap, version);
                final Application spanApplication = this.applicationFactory.createApplication(span.getApplicationId(), span.getApplicationServiceType());

                // records the Span's response time statistics
                recordSpanResponseTime(spanApplication, span, mapHistogramSummary, span.getCollectorAcceptTime());

                if (!spanApplication.getServiceType().isRecordStatistics() || spanApplication.getServiceType().isRpcClient()) {
                    // span's serviceType is probably not set correctly
                    logger.warn("invalid span application:{}", spanApplication);
                    continue;
                }

                final short slotTime = getHistogramSlotTime(span, spanApplication.getServiceType());
                // might need to reconsider using collector's accept time for link statistics.
                // we need to convert to time window's timestamp. If not, it may lead to OOM due to mismatch in timeslots. 
                long timestamp = window.refineTimestamp(span.getCollectorAcceptTime());

                if (parentApplication.getServiceType() == ServiceType.USER) {
                    // Outbound data
                    if (logger.isTraceEnabled()) {
                        logger.trace("span user:{} {} -> span:{} {}", parentApplication, span.getAgentId(), spanApplication, span.getAgentId());
                    }
                    final LinkDataMap sourceLinkData = linkDataDuplexMap.getSourceLinkDataMap();
                    sourceLinkData.addLinkData(parentApplication, span.getAgentId(), spanApplication,  span.getAgentId(), timestamp, slotTime, 1);

                    if (logger.isTraceEnabled()) {
                        logger.trace("span target user:{} {} -> span:{} {}", parentApplication, span.getAgentId(), spanApplication, span.getAgentId());
                    }
                    // Inbound data
                    final LinkDataMap targetLinkDataMap = linkDataDuplexMap.getTargetLinkDataMap();
                    targetLinkDataMap.addLinkData(parentApplication, span.getAgentId(), spanApplication, span.getAgentId(), timestamp, slotTime, 1);
                } else {
                    // Inbound data
                    if (logger.isTraceEnabled()) {
                        logger.trace("span target parent:{} {} -> span:{} {}", parentApplication, span.getAgentId(), spanApplication, span.getAgentId());
                    }
                    final LinkDataMap targetLinkDataMap = linkDataDuplexMap.getTargetLinkDataMap();
                    targetLinkDataMap.addLinkData(parentApplication, span.getAgentId(), spanApplication, span.getAgentId(), timestamp, slotTime, 1);
                }

                if (serverMapDataFilter != null && serverMapDataFilter.filter(spanApplication)) {
                    continue;
                }
                
                addNodeFromSpanEvent(span, window, linkDataDuplexMap, transactionSpanMap);
            }
        }

        mapHistogramSummary.build();

        WasNodeHistogramDataSource wasNodeHistogramDataSource = new ResponseHistogramBuilderNodeHistogramDataSource(mapHistogramSummary);
        NodeHistogramFactory nodeHistogramFactory = new DefaultNodeHistogramFactory(wasNodeHistogramDataSource);

        ServerInstanceListDataSource serverInstanceListDataSource = new AgentInfoServerInstanceListDataSource(agentInfoService);
        ServerInstanceListFactory serverInstanceListFactory = new DefaultServerInstanceListFactory(serverInstanceListDataSource);

        ApplicationMapBuilder applicationMapBuilder = applicationMapBuilderFactory.createApplicationMapBuilder(range);
        applicationMapBuilder.linkType(LinkType.DETAILED);
        applicationMapBuilder.includeNodeHistogram(nodeHistogramFactory);
        applicationMapBuilder.includeServerInfo(serverInstanceListFactory);
        ApplicationMap map = applicationMapBuilder.build(linkDataDuplexMap);

        if(serverMapDataFilter != null) {
            map = serverMapDataFilter.dataFiltering(map);
        }
        
        return map;
    }

    private Map<Long, SpanBo> checkDuplicatedSpanId(List<SpanBo> transaction) {
        final Map<Long, SpanBo> transactionSpanMap = new HashMap<>();
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
        /*
         * add span event statistics
         */
        final List<SpanEventBo> spanEventBoList = span.getSpanEventBoList();
        if (CollectionUtils.isEmpty(spanEventBoList)) {
            return;
        }
        final Application srcApplication = applicationFactory.createApplication(span.getApplicationId(), span.getApplicationServiceType());

        LinkDataMap sourceLinkDataMap = linkDataDuplexMap.getSourceLinkDataMap();
        for (SpanEventBo spanEvent : spanEventBoList) {

            ServiceType destServiceType = registry.findServiceType(spanEvent.getServiceType());
            if (!destServiceType.isRecordStatistics()) {
                // internal method
                continue;
            }
            // convert to Unknown if destServiceType is a rpc client and there is no acceptor.
            // acceptor exists if there is a span with spanId identical to the current spanEvent's next spanId.
            // logic for checking acceptor
            if (destServiceType.isRpcClient()) {
                if (!transactionSpanMap.containsKey(spanEvent.getNextSpanId())) {
                    destServiceType = ServiceType.UNKNOWN;
                }
            }

            String dest = spanEvent.getDestinationId();
            if (dest == null) {
                dest = "Unknown";
            }
            
            final Application destApplication = this.applicationFactory.createApplication(dest, destServiceType);

            final short slotTime = getHistogramSlotTime(spanEvent, destServiceType);

            // FIXME
            final long spanEventTimeStamp = window.refineTimestamp(span.getStartTime() + spanEvent.getStartElapsed());
            if (logger.isTraceEnabled()) {
                logger.trace("spanEvent  src:{} {} -> dest:{} {}", srcApplication, span.getAgentId(), destApplication, spanEvent.getEndPoint());
            }
            // endPoint may be null
            final String destinationAgentId = StringUtils.defaultString(spanEvent.getEndPoint());
            sourceLinkDataMap.addLinkData(srcApplication, span.getAgentId(), destApplication, destinationAgentId, spanEventTimeStamp, slotTime, 1);
        }
    }

    private Application createParentApplication(SpanBo span, Map<Long, SpanBo> transactionSpanMap, int version) {
        final SpanBo parentSpan = transactionSpanMap.get(span.getParentSpanId());
        if (span.isRoot() || parentSpan == null) {
            ServiceType spanServiceType = this.registry.findServiceType(span.getServiceType());
            if (spanServiceType.isQueue()) {
                String applicationName = span.getAcceptorHost();
                ServiceType serviceType = spanServiceType;
                return this.applicationFactory.createApplication(applicationName, serviceType);
            } else {
                String applicationName;
                // FIXME magic number, remove after front end UI changes and simply use the newer one
                if (version >= 4) {
                    ServiceType applicationServiceType = this.registry.findServiceType(span.getApplicationServiceType());
                    applicationName = span.getApplicationId() + "_" + applicationServiceType;
                } else {
                    applicationName = span.getApplicationId();
                }
                ServiceType serviceType = ServiceType.USER;
                return this.applicationFactory.createApplication(applicationName, serviceType);
            }
        } else {
            // create virtual queue node if current' span's service type is a queue AND :
            // 1. parent node's application service type is not a queue (it may have come from a queue that is traced)
            // 2. current node's application service type is not a queue (current node may be a queue that is traced)
            ServiceType spanServiceType = this.registry.findServiceType(span.getServiceType());
            if (spanServiceType.isQueue()) {
                ServiceType parentApplicationServiceType = this.registry.findServiceType(parentSpan.getApplicationServiceType());
                ServiceType spanApplicationServiceType = this.registry.findServiceType(span.getApplicationServiceType());
                if (!parentApplicationServiceType.isQueue() && !spanApplicationServiceType.isQueue()) {
                    String parentApplicationName = span.getAcceptorHost();
                    if (parentApplicationName == null) {
                        parentApplicationName = span.getRemoteAddr();
                    }
                    short parentServiceType = span.getServiceType();
                    return this.applicationFactory.createApplication(parentApplicationName, parentServiceType);
                }
            }
            String parentApplicationName = parentSpan.getApplicationId();
            short parentServiceType = parentSpan.getApplicationServiceType();
            return this.applicationFactory.createApplication(parentApplicationName, parentServiceType);
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
        final HistogramSchema schema = serviceType.getHistogramSchema();
        final HistogramSlot histogramSlot = schema.findHistogramSlot(elapsedTime, hasException);
        return histogramSlot.getSlotTime();
    }

    private List<TransactionId> recursiveCallFilter(List<TransactionId> transactionIdList) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList must not be null");
        }

        List<TransactionId> crashKey = new ArrayList<>();
        Map<TransactionId, Object> filterMap = new LinkedHashMap<>(transactionIdList.size());
        for (TransactionId transactionId : transactionIdList) {
            Object old = filterMap.put(transactionId, V);
            if (old != null) {
                crashKey.add(transactionId);
            }
        }
        if (!crashKey.isEmpty()) {
            Set<TransactionId> filteredTransactionId = filterMap.keySet();
            logger.info("transactionId crash found. original:{} filter:{} crashKey:{}", transactionIdList.size(), filteredTransactionId.size(), crashKey);
            return Lists.newArrayList(filteredTransactionId);
        }
        return transactionIdList;
    }
}
