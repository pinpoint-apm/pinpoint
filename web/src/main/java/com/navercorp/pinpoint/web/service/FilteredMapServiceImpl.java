/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilderFactory;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterData;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.ResponseHistogramsNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.DefaultServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.link.LinkType;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.service.map.FilteredMap;
import com.navercorp.pinpoint.web.service.map.FilteredMapBuilder;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import com.navercorp.pinpoint.web.vo.LoadFactor;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SelectedScatterArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 */
@Service
public class FilteredMapServiceImpl implements FilteredMapService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentInfoService agentInfoService;

    private final TraceDao traceDao;

    private final ApplicationTraceIndexDao applicationTraceIndexDao;

    private final ServiceTypeRegistryService registry;

    private final ApplicationFactory applicationFactory;
    
    private final ServerMapDataFilter serverMapDataFilter;

    private final ApplicationMapBuilderFactory applicationMapBuilderFactory;

    private static final Object V = new Object();

    public FilteredMapServiceImpl(AgentInfoService agentInfoService,
                                  @Qualifier("hbaseTraceDaoFactory") TraceDao traceDao, ApplicationTraceIndexDao applicationTraceIndexDao,
                                  ServiceTypeRegistryService registry, ApplicationFactory applicationFactory,
                                  Optional<ServerMapDataFilter> serverMapDataFilter, ApplicationMapBuilderFactory applicationMapBuilderFactory) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.serverMapDataFilter = Objects.requireNonNull(serverMapDataFilter, "serverMapDataFilter").orElse(null);
        this.applicationMapBuilderFactory = Objects.requireNonNull(applicationMapBuilderFactory, "applicationMapBuilderFactory");
    }

    @Override
    public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit) {
        return selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit, true);
    }

    @Override
    public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit, boolean backwardDirection) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName");
        }
        if (range == null) {
            throw new NullPointerException("range");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}", applicationName, range);
        }

        return this.applicationTraceIndexDao.scanTraceIndex(applicationName, range, limit, backwardDirection);
    }


    @Override
    @Deprecated
    public LoadFactor linkStatistics(Range range, List<TransactionId> traceIdSet, Application sourceApplication, Application destinationApplication, Filter<SpanBo> filter) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication");
        }
        if (destinationApplication == null) {
            throw new NullPointerException("destApplicationName");
        }
        if (filter == null) {
            throw new NullPointerException("filter");
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
                if (CollectionUtils.isEmpty(spanEventBoList)) {
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

    private List<SpanBo> filterList(List<List<SpanBo>> transactionList, Filter<SpanBo> filter) {
        final List<SpanBo> filteredResult = new ArrayList<>();
        for (List<SpanBo> transaction : transactionList) {
            if (filter.include(transaction)) {
                filteredResult.addAll(transaction);
            }
        }
        return filteredResult;
    }

    private List<List<SpanBo>> filterList2(List<List<SpanBo>> transactionList, Filter<SpanBo> filter) {
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
            throw new NullPointerException("transactionId");
        }
        List<TransactionId> transactionIdList = Collections.singletonList(transactionId);
        // FIXME from,to -1
        Range range = new Range(-1, -1);

        final List<List<SpanBo>> filterList = selectFilteredSpan(transactionIdList, Filter.acceptAllFilter());
        FilteredMapBuilder filteredMapBuilder = new FilteredMapBuilder(applicationFactory, registry, range, version);
        filteredMapBuilder.serverMapDataFilter(serverMapDataFilter);
        filteredMapBuilder.addTransactions(filterList);
        FilteredMap filteredMap = filteredMapBuilder.build();

        ApplicationMap map = createMap(range, filteredMap);
        return map;
    }

    @Override
    public ApplicationMap selectApplicationMapWithScatterData(List<TransactionId> transactionIdList, Range originalRange, Range scanRange, int xGroupUnit, int yGroupUnit, Filter<SpanBo> filter, int version) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList");
        }
        if (filter == null) {
            throw new NullPointerException("filter");
        }

        StopWatch watch = new StopWatch();
        watch.start();

        final List<List<SpanBo>> filterList = selectFilteredSpan(transactionIdList, filter);
        FilteredMapBuilder filteredMapBuilder = new FilteredMapBuilder(applicationFactory, registry, originalRange, version);
        filteredMapBuilder.serverMapDataFilter(serverMapDataFilter);
        filteredMapBuilder.addTransactions(filterList);
        FilteredMap filteredMap = filteredMapBuilder.build();

        ApplicationMap map = createMap(originalRange, filteredMap);

        Map<Application, ScatterData> applicationScatterData = filteredMap.getApplicationScatterData(originalRange.getFrom(), originalRange.getTo(), xGroupUnit, yGroupUnit);
        ApplicationMapWithScatterData applicationMapWithScatterData = new ApplicationMapWithScatterData(map, applicationScatterData);

        watch.stop();
        logger.debug("Select filtered application map elapsed. {}ms", watch.getTotalTimeMillis());

        return applicationMapWithScatterData;
    }

    private List<List<SpanBo>> selectFilteredSpan(List<TransactionId> transactionIdList, Filter<SpanBo> filter) {
        // filters out recursive calls by looking at each objects
        // do not filter here if we change to a tree-based collision check in the future. 
        final List<TransactionId> recursiveFilterList = recursiveCallFilter(transactionIdList);

        // FIXME might be better to simply traverse the List<Span> and create a process chain for execution
        final List<List<SpanBo>> originalList = this.traceDao.selectAllSpans(recursiveFilterList);

        return filterList2(originalList, filter);
    }

    private ApplicationMap createMap(Range range, FilteredMap filteredMap) {
        WasNodeHistogramDataSource wasNodeHistogramDataSource = new ResponseHistogramsNodeHistogramDataSource(filteredMap.getResponseHistograms());
        NodeHistogramFactory nodeHistogramFactory = new DefaultNodeHistogramFactory(wasNodeHistogramDataSource);

        ServerInstanceListDataSource serverInstanceListDataSource = new AgentInfoServerInstanceListDataSource(agentInfoService);
        ServerInstanceListFactory serverInstanceListFactory = new DefaultServerInstanceListFactory(serverInstanceListDataSource);

        ApplicationMapBuilder applicationMapBuilder = applicationMapBuilderFactory.createApplicationMapBuilder(range);
        applicationMapBuilder.linkType(LinkType.DETAILED);
        applicationMapBuilder.includeNodeHistogram(nodeHistogramFactory);
        applicationMapBuilder.includeServerInfo(serverInstanceListFactory);
        ApplicationMap map = applicationMapBuilder.build(filteredMap.getLinkDataDuplexMap());

        if(serverMapDataFilter != null) {
            map = serverMapDataFilter.dataFiltering(map);
        }

        return map;
    }

    private List<TransactionId> recursiveCallFilter(List<TransactionId> transactionIdList) {
        if (transactionIdList == null) {
            throw new NullPointerException("transactionIdList");
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
