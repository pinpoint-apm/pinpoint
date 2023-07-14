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

import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCount;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilderFactory;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterData;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapWithScatterDataV3;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.ResponseHistogramsNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.DefaultServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.StatisticsServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerGroupListDataSource;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
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

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TraceDao traceDao;

    private final ApplicationTraceIndexDao applicationTraceIndexDao;

    private final ServiceTypeRegistryService registry;

    private final ApplicationFactory applicationFactory;

    private final ServerInstanceDatasourceService serverInstanceDatasourceService;

    private final ServerMapDataFilter serverMapDataFilter;

    private final ApplicationMapBuilderFactory applicationMapBuilderFactory;

    private static final Object V = new Object();

    @Value("${web.servermap.build.timeout:600000}")
    private long buildTimeoutMillis;

    public FilteredMapServiceImpl(TraceDao traceDao,
                                  ApplicationTraceIndexDao applicationTraceIndexDao,
                                  ServiceTypeRegistryService registry,
                                  ApplicationFactory applicationFactory,
                                  ServerInstanceDatasourceService serverInstanceDatasourceService,
                                  Optional<ServerMapDataFilter> serverMapDataFilter,
                                  ApplicationMapBuilderFactory applicationMapBuilderFactory) {
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.serverInstanceDatasourceService = Objects.requireNonNull(serverInstanceDatasourceService, "serverInstanceDatasourceService");
        this.serverMapDataFilter = Objects.requireNonNull(serverMapDataFilter, "serverMapDataFilter").orElse(null);
        this.applicationMapBuilderFactory = Objects.requireNonNull(applicationMapBuilderFactory, "applicationMapBuilderFactory");
    }

    @Override
    public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit) {
        return selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit, true);
    }

    @Override
    public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit, boolean backwardDirection) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");

        if (logger.isTraceEnabled()) {
            logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}", applicationName, range);
        }

        return this.applicationTraceIndexDao.scanTraceIndex(applicationName, range, limit, backwardDirection);
    }

    private List<List<SpanBo>> filterList2(List<List<SpanBo>> transactionList, Filter<List<SpanBo>> filter) {
        final List<List<SpanBo>> filteredResult = new ArrayList<>();
        for (List<SpanBo> transaction : transactionList) {
            if (filter.include(transaction)) {
                filteredResult.add(transaction);
            }
        }
        return filteredResult;
    }

    public ApplicationMap selectApplicationMap(FilteredMapServiceOption option) {
        final List<List<SpanBo>> filterList = selectFilteredSpan(option.getTransactionIdList(), option.getFilter(), option.getColumnGetCount());
        FilteredMapBuilder filteredMapBuilder = new FilteredMapBuilder(applicationFactory, registry, option.getOriginalRange(), option.getVersion());
        filteredMapBuilder.serverMapDataFilter(serverMapDataFilter);
        filteredMapBuilder.addTransactions(filterList);
        FilteredMap filteredMap = filteredMapBuilder.build();

        ApplicationMap map = createMap(option, filteredMap);
        return map;
    }

    public ApplicationMap selectApplicationMapWithScatterData(FilteredMapServiceOption option) {
        StopWatch watch = new StopWatch();
        watch.start();

        final List<List<SpanBo>> filterList = selectFilteredSpan(option.getTransactionIdList(), option.getFilter(), option.getColumnGetCount());
        FilteredMapBuilder filteredMapBuilder = new FilteredMapBuilder(applicationFactory, registry, option.getOriginalRange(), option.getVersion());
        filteredMapBuilder.serverMapDataFilter(serverMapDataFilter);
        filteredMapBuilder.addTransactions(filterList);
        FilteredMap filteredMap = filteredMapBuilder.build();

        ApplicationMap map = createMap(option, filteredMap);

        Map<Application, ScatterData> applicationScatterData = filteredMap.getApplicationScatterData(option.getOriginalRange().getFrom(), option.getOriginalRange().getTo(), option.getxGroupUnit(), option.getyGroupUnit());
        ApplicationMapWithScatterData applicationMapWithScatterData = new ApplicationMapWithScatterData(map, applicationScatterData);

        watch.stop();
        logger.debug("Select filtered application map elapsed. {}ms", watch.getTotalTimeMillis());

        return applicationMapWithScatterData;
    }

    @Override
    public ApplicationMap selectApplicationMapWithScatterDataV3(FilteredMapServiceOption option) {
        StopWatch watch = new StopWatch();
        watch.start();

        final List<List<SpanBo>> filterList = selectFilteredSpan(option.getTransactionIdList(), option.getFilter(), option.getColumnGetCount());
        FilteredMapBuilder filteredMapBuilder = new FilteredMapBuilder(applicationFactory, registry, option.getOriginalRange(), option.getVersion());
        filteredMapBuilder.serverMapDataFilter(serverMapDataFilter);
        filteredMapBuilder.addTransactions(filterList);
        FilteredMap filteredMap = filteredMapBuilder.build();

        ApplicationMap map = createMap(option, filteredMap);

        Map<Application, ScatterData> applicationScatterData = filteredMap.getApplicationScatterData(option.getOriginalRange().getFrom(), option.getOriginalRange().getTo(), option.getxGroupUnit(), option.getyGroupUnit());
        ApplicationMapWithScatterDataV3 applicationMapWithScatterDataV3 = new ApplicationMapWithScatterDataV3(map, applicationScatterData);

        watch.stop();
        logger.debug("Select filtered application map elapsed. {}ms", watch.getTotalTimeMillis());

        return applicationMapWithScatterDataV3;
    }

    private List<List<SpanBo>> selectFilteredSpan(List<TransactionId> transactionIdList, Filter<List<SpanBo>> filter, ColumnGetCount columnGetCount) {
        // filters out recursive calls by looking at each objects
        // do not filter here if we change to a tree-based collision check in the future.
        final List<TransactionId> recursiveFilterList = recursiveCallFilter(transactionIdList);

        // FIXME might be better to simply traverse the List<Span> and create a process chain for execution
        final List<List<SpanBo>> originalList = this.traceDao.selectAllSpans(recursiveFilterList, columnGetCount);

        return filterList2(originalList, filter);
    }

    private ApplicationMap createMap(FilteredMapServiceOption option, FilteredMap filteredMap) {
        final ApplicationMapBuilder applicationMapBuilder = applicationMapBuilderFactory.createApplicationMapBuilder(option.getOriginalRange());
        applicationMapBuilder.linkType(LinkType.DETAILED);
        final WasNodeHistogramDataSource wasNodeHistogramDataSource = new ResponseHistogramsNodeHistogramDataSource(filteredMap.getResponseHistograms());
        applicationMapBuilder.includeNodeHistogram(new DefaultNodeHistogramFactory(wasNodeHistogramDataSource));
        ServerGroupListDataSource serverGroupListDataSource = serverInstanceDatasourceService.getServerGroupListDataSource();
        if (option.isUseStatisticsAgentState()) {
            applicationMapBuilder.includeServerInfo(new StatisticsServerGroupListFactory(serverGroupListDataSource));
        } else {
            applicationMapBuilder.includeServerInfo(new DefaultServerGroupListFactory(serverGroupListDataSource));
        }
        ApplicationMap map = applicationMapBuilder.build(filteredMap.getLinkDataDuplexMap(), buildTimeoutMillis);
        if (serverMapDataFilter != null) {
            map = serverMapDataFilter.dataFiltering(map);
        }

        return map;
    }

    private List<TransactionId> recursiveCallFilter(List<TransactionId> transactionIdList) {
        Objects.requireNonNull(transactionIdList, "transactionIdList");

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
            return new ArrayList<>(filteredTransactionId);
        }
        return transactionIdList;
    }
}
