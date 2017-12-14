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
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.link.LinkType;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeType;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.service.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.service.map.LinkSelector;
import com.navercorp.pinpoint.web.service.map.LinkSelectorFactory;
import com.navercorp.pinpoint.web.service.map.LinkSelectorType;
import com.navercorp.pinpoint.web.service.map.processor.WasOnlyProcessor;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.SearchOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.Collections;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@Service
public class MapServiceImpl implements MapService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LinkSelectorFactory linkSelectorFactory;

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private MapResponseDao mapResponseDao;

    @Autowired
    private ApplicationFactory applicationFactory;
    
    @Autowired(required=false)
    private ServerMapDataFilter serverMapDataFilter;

    @Autowired
    private ApplicationMapBuilderFactory applicationMapBuilderFactory;

    /**
     * Used in the main UI - draws the server map by querying the timeslot by time.
     */
    @Override
    public ApplicationMap selectApplicationMap(Application sourceApplication, Range range, SearchOption searchOption, NodeType nodeType, LinkType linkType) {
        if (sourceApplication == null) {
            throw new NullPointerException("sourceApplication must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        logger.debug("SelectApplicationMap");

        StopWatch watch = new StopWatch("ApplicationMap");
        watch.start("ApplicationMap Hbase Io Fetch(Caller,Callee) Time");

        LinkSelectorType linkSelectorType = searchOption.getLinkSelectorType();
        int callerSearchDepth = searchOption.getCallerSearchDepth();
        int calleeSearchDepth = searchOption.getCalleeSearchDepth();

        LinkDataMapProcessor callerLinkDataMapProcessor = LinkDataMapProcessor.NO_OP;
        if (searchOption.isWasOnly()) {
            callerLinkDataMapProcessor = new WasOnlyProcessor();
        }
        LinkDataMapProcessor calleeLinkDataMapProcessor = LinkDataMapProcessor.NO_OP;
        LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(linkSelectorType, callerLinkDataMapProcessor, calleeLinkDataMapProcessor);
        LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(Collections.singletonList(sourceApplication), range, callerSearchDepth, calleeSearchDepth);
        watch.stop();

        watch.start("ApplicationMap MapBuilding(Response) Time");

        ApplicationMapBuilder builder = createApplicationMapBuilder(range, nodeType, linkType);
        ApplicationMap map = builder.build(linkDataDuplexMap);
        if (map.getNodes().isEmpty()) {
            map = builder.build(sourceApplication);
        }
        watch.stop();
        if (logger.isInfoEnabled()) {
            logger.info("ApplicationMap BuildTime: {}", watch.prettyPrint());
        }
        if(serverMapDataFilter != null) {
            map = serverMapDataFilter.dataFiltering(map);
        }
        return map;
    }

    private ApplicationMapBuilder createApplicationMapBuilder(Range range, NodeType nodeType, LinkType linkType) {
        ApplicationMapBuilder builder = applicationMapBuilderFactory.createApplicationMapBuilder(range);
        builder.nodeType(nodeType);
        builder.linkType(linkType);

        WasNodeHistogramDataSource wasNodeHistogramDataSource = new MapResponseNodeHistogramDataSource(mapResponseDao);
        NodeHistogramFactory nodeHistogramFactory = new DefaultNodeHistogramFactory(wasNodeHistogramDataSource);
        builder.includeNodeHistogram(nodeHistogramFactory);

        ServerInstanceListDataSource serverInstanceListDataSource = new AgentInfoServerInstanceListDataSource(agentInfoService);
        ServerInstanceListFactory serverInstanceListFactory = new DefaultServerInstanceListFactory(serverInstanceListDataSource);
        builder.includeServerInfo(serverInstanceListFactory);
        return builder;
    }
}
