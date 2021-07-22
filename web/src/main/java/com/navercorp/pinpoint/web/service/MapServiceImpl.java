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

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilderFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.MapResponseNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.DefaultServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.StatisticsServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.service.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.service.map.LinkSelector;
import com.navercorp.pinpoint.web.service.map.LinkSelectorFactory;
import com.navercorp.pinpoint.web.service.map.LinkSelectorType;
import com.navercorp.pinpoint.web.service.map.processor.WasOnlyProcessor;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@Service
public class MapServiceImpl implements MapService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkSelectorFactory linkSelectorFactory;

    private final AgentInfoService agentInfoService;

    private final MapResponseDao mapResponseDao;

    private final ServerMapDataFilter serverMapDataFilter;

    private final ApplicationMapBuilderFactory applicationMapBuilderFactory;

    private final LinkDataLimiter linkDataLimiter;

    @Value("${web.servermap.build.timeout:600000}")
    private long buildTimeoutMillis;

    public MapServiceImpl(LinkSelectorFactory linkSelectorFactory, AgentInfoService agentInfoService,
                          MapResponseDao mapResponseDao,
                          Optional<ServerMapDataFilter> serverMapDataFilter, ApplicationMapBuilderFactory applicationMapBuilderFactory, LinkDataLimiter linkDataLimiter) {
        this.linkSelectorFactory = Objects.requireNonNull(linkSelectorFactory, "linkSelectorFactory");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
        this.serverMapDataFilter = Objects.requireNonNull(serverMapDataFilter, "serverMapDataFilter").orElse(null);
        this.applicationMapBuilderFactory = Objects.requireNonNull(applicationMapBuilderFactory, "applicationMapBuilderFactory");
        this.linkDataLimiter = linkDataLimiter;
    }

    /**
     * Used in the main UI - draws the server map by querying the timeslot by time.
     */
    @Override
    public ApplicationMap selectApplicationMap(MapServiceOption option) {
        logger.debug("SelectApplicationMap");

        StopWatch watch = new StopWatch("ApplicationMap");
        watch.start("ApplicationMap Hbase Io Fetch(Caller,Callee) Time");

        final SearchOption searchOption = option.getSearchOption();
        LinkSelectorType linkSelectorType = searchOption.getLinkSelectorType();
        int callerSearchDepth = searchOption.getCallerSearchDepth();
        int calleeSearchDepth = searchOption.getCalleeSearchDepth();

        LinkDataMapProcessor callerLinkDataMapProcessor = LinkDataMapProcessor.NO_OP;
        if (searchOption.isWasOnly()) {
            callerLinkDataMapProcessor = new WasOnlyProcessor();
        }
        LinkDataMapProcessor calleeLinkDataMapProcessor = LinkDataMapProcessor.NO_OP;
        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(linkSelectorType, callerLinkDataMapProcessor, calleeLinkDataMapProcessor);
        LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(Collections.singletonList(option.getSourceApplication()), option.getRange(), callerSearchDepth, calleeSearchDepth);
        watch.stop();

        if (linkDataLimiter.excess(linkDataDuplexMap.getTotalCount())) {
            throw new RuntimeException("Too many link data. Reduce the values of the Inbound/outbound or do not select the bidirectional option. limiter=" + linkDataLimiter.toString(linkDataDuplexMap.getTotalCount()));
        }

        watch.start("ApplicationMap MapBuilding(Response) Time");

        ApplicationMapBuilder builder = createApplicationMapBuilder(option);
        ApplicationMap map = builder.build(linkDataDuplexMap, buildTimeoutMillis);
        if (map.getNodes().isEmpty()) {
            map = builder.build(option.getSourceApplication(), buildTimeoutMillis);
        }
        watch.stop();
        if (logger.isInfoEnabled()) {
            logger.info("ApplicationMap BuildTime: {}", watch.prettyPrint());
        }
        if (serverMapDataFilter != null) {
            map = serverMapDataFilter.dataFiltering(map);
        }
        return map;
    }

    private ApplicationMapBuilder createApplicationMapBuilder(MapServiceOption option) {
        ApplicationMapBuilder builder = applicationMapBuilderFactory.createApplicationMapBuilder(option.getRange());
        builder.nodeType(option.getNodeType());
        builder.linkType(option.getLinkType());

        WasNodeHistogramDataSource wasNodeHistogramDataSource = new MapResponseNodeHistogramDataSource(mapResponseDao);
        NodeHistogramFactory nodeHistogramFactory = new DefaultNodeHistogramFactory(wasNodeHistogramDataSource);
        builder.includeNodeHistogram(nodeHistogramFactory);

        if (option.isUseStatisticsServerInstanceList()) {
            builder.includeServerInfo(new StatisticsServerInstanceListFactory());
        } else {
            ServerInstanceListDataSource serverInstanceListDataSource = new AgentInfoServerInstanceListDataSource(agentInfoService);
            builder.includeServerInfo(new DefaultServerInstanceListFactory(serverInstanceListDataSource));
        }

        return builder;
    }
}