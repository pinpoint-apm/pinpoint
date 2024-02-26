/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.service;

import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.MapResponseNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.DefaultServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.StatisticsServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.dao.MapResponseDao;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkDirection;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.link.LinkListFactory;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelector;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelectorFactory;
import com.navercorp.pinpoint.web.applicationmap.map.LinkSelectorType;
import com.navercorp.pinpoint.web.applicationmap.map.processor.DestinationApplicationFilter;
import com.navercorp.pinpoint.web.applicationmap.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.applicationmap.map.processor.SourceApplicationFilter;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeListFactory;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.service.ServerInstanceDatasourceService;
import com.navercorp.pinpoint.web.view.ApplicationTimeHistogramViewModel;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 * @author jaehong.kim
 */
@Service
public class ResponseTimeHistogramServiceImpl implements ResponseTimeHistogramService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final LinkSelectorFactory linkSelectorFactory;

    private final ServerInstanceDatasourceService serverInstanceDatasourceService;

    private final MapResponseDao mapResponseDao;


    public ResponseTimeHistogramServiceImpl(LinkSelectorFactory linkSelectorFactory,
                                            ServerInstanceDatasourceService serverInstanceDatasourceService,
                                            MapResponseDao mapResponseDao) {
        this.linkSelectorFactory = Objects.requireNonNull(linkSelectorFactory, "linkSelectorFactory");
        this.serverInstanceDatasourceService = Objects.requireNonNull(serverInstanceDatasourceService, "serverInstanceDatasourceService");
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
    }

    private ServerGroupListFactory createServerGroupListFactory(ResponseTimeHistogramServiceOption option) {
        ServerGroupListDataSource serverGroupListDataSource = serverInstanceDatasourceService.getServerGroupListDataSource();
        if (option.isUseStatisticsAgentState()) {
            return new StatisticsServerGroupListFactory(serverGroupListDataSource);
        }
        return new DefaultServerGroupListFactory(serverGroupListDataSource);
    }

    private NodeHistogramFactory createNodeHistogramFactory() {
        WasNodeHistogramDataSource wasNodeHistogramDataSource = new MapResponseNodeHistogramDataSource(mapResponseDao);
        return new DefaultNodeHistogramFactory(wasNodeHistogramDataSource);
    }

    @Override
    public ApplicationTimeHistogramViewModel selectResponseTimeHistogramData(Application application, Range range) {
        List<ResponseTime> responseTimes = mapResponseDao.selectResponseTime(application, range);
        return new ApplicationTimeHistogramViewModel(application, range, new AgentHistogramList(application, responseTimes));
    }

    @Override
    public NodeHistogramSummary selectNodeHistogramData(ResponseTimeHistogramServiceOption option) {
        Node node = new Node(option.getApplication());
        Application application = option.getApplication();
        ServiceType applicationServiceType = application.getServiceType();

        List<Application> sourceApplications = option.getFromApplications();
        List<Application> destinationApplications = option.getToApplications();

        final NodeHistogramFactory nodeHistogramFactory = createNodeHistogramFactory();
        final ServerGroupListFactory serverGroupListFactory = createServerGroupListFactory(option);

        if (applicationServiceType.isWas()) {
            NodeHistogram nodeHistogram = nodeHistogramFactory.createWasNodeHistogram(option.getApplication(), option.getRange());
            node.setNodeHistogram(nodeHistogram);
            ServerGroupList serverGroupList = serverGroupListFactory.createWasNodeInstanceList(node, option.getRange().getToInstant());
            return new NodeHistogramSummary(application, serverGroupList, nodeHistogram);
        } else if (isTerminal(applicationServiceType)) {
            if (sourceApplications.isEmpty()) {
                return createEmptyNodeHistogramSummary(serverGroupListFactory, option.getApplication(), option.getRange());
            }
            LinkDataMapProcessor destinationApplicationFilter = new DestinationApplicationFilter(option.getApplication());
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, destinationApplicationFilter, LinkDataMapProcessor.NO_OP);
            LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(sourceApplications, option.getRange(), 1, 0);

            ServerGroupList serverGroupList = serverGroupListFactory.createEmptyNodeInstanceList();
            if (applicationServiceType.isTerminal() || applicationServiceType.isAlias()) {
                serverGroupList = serverGroupListFactory.createTerminalNodeInstanceList(node, linkDataDuplexMap);
            }

            NodeList nodeList = NodeListFactory.createNodeList(linkDataDuplexMap);
            LinkList linkList = LinkListFactory.createLinkList(nodeList, linkDataDuplexMap, option.getRange());
            NodeHistogram nodeHistogram = nodeHistogramFactory.createTerminalNodeHistogram(option.getApplication(), option.getRange(), linkList);
            return new NodeHistogramSummary(application, serverGroupList, nodeHistogram);
        } else if (applicationServiceType.isQueue()) {
            if (sourceApplications.isEmpty()) {
                //scan callee from queue node to find sourceApplication out of serverMap search bound is possible
                return createEmptyNodeHistogramSummary(serverGroupListFactory, option.getApplication(), option.getRange());
            }
            LinkDataMapProcessor destinationApplicationFilter = new DestinationApplicationFilter(option.getApplication());
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, destinationApplicationFilter, LinkDataMapProcessor.NO_OP);
            LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(sourceApplications, option.getRange(), 1, 0);

            ServerGroupList serverGroupList = serverGroupListFactory.createQueueNodeInstanceList(node, linkDataDuplexMap);

            NodeList nodeList = NodeListFactory.createNodeList(linkDataDuplexMap);
            LinkList linkList = LinkListFactory.createLinkList(nodeList, linkDataDuplexMap, option.getRange());
            NodeHistogram nodeHistogram = nodeHistogramFactory.createQueueNodeHistogram(option.getApplication(), option.getRange(), linkList);
            return new NodeHistogramSummary(application, serverGroupList, nodeHistogram);
        } else if (applicationServiceType.isUser()) {
            if (destinationApplications.isEmpty()) {
                return createEmptyNodeHistogramSummary(serverGroupListFactory, option.getApplication(), option.getRange());
            }
            LinkDataMapProcessor sourceApplicationFilter = new SourceApplicationFilter(option.getApplication());
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, LinkDataMapProcessor.NO_OP, sourceApplicationFilter);
            LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(destinationApplications, option.getRange(), 0, 1);

            ServerGroupList serverGroupList = serverGroupListFactory.createUserNodeInstanceList();

            NodeList nodeList = NodeListFactory.createNodeList(linkDataDuplexMap);
            LinkList linkList = LinkListFactory.createLinkList(nodeList, linkDataDuplexMap, option.getRange());
            NodeHistogram nodeHistogram = nodeHistogramFactory.createUserNodeHistogram(option.getApplication(), option.getRange(), linkList);
            return new NodeHistogramSummary(application, serverGroupList, nodeHistogram);
        } else {
            return createEmptyNodeHistogramSummary(serverGroupListFactory, option.getApplication(), option.getRange());
        }
    }

    private boolean isTerminal(ServiceType applicationServiceType) {
        return applicationServiceType.isTerminal() || applicationServiceType.isUnknown() || applicationServiceType.isAlias();
    }

    private NodeHistogramSummary createEmptyNodeHistogramSummary(ServerGroupListFactory serverGroupListFactory, Application application, Range range) {
        ServerGroupList serverGroupList = serverGroupListFactory.createEmptyNodeInstanceList();
        NodeHistogram emptyNodeHistogram = new NodeHistogram(application, range);
        return new NodeHistogramSummary(application, serverGroupList, emptyNodeHistogram);
    }

    @Override
    public LinkHistogramSummary selectLinkHistogramData(Application fromApplication, Application toApplication, Range range) {
        Objects.requireNonNull(fromApplication, "fromApplication");
        Objects.requireNonNull(toApplication, "toApplication");
        Objects.requireNonNull(range, "range");


        LinkDataDuplexMap linkDataDuplexMap;
        ServiceType fromApplicationServiceType = fromApplication.getServiceType();
        LinkDirection linkDirection = LinkDirection.OUT_LINK;
        if (fromApplicationServiceType.isUser()) {
            //scan using toApplication to distinguish same applicationName with different serviceType
            linkDirection = LinkDirection.IN_LINK;
            LinkDataMapProcessor sourceApplicationFilter = new SourceApplicationFilter(fromApplication);
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, LinkDataMapProcessor.NO_OP, sourceApplicationFilter);
            linkDataDuplexMap = linkSelector.select(Collections.singletonList(toApplication), range, 0, 1);
        } else {
            LinkDataMapProcessor destinationApplication = new DestinationApplicationFilter(toApplication);
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, destinationApplication, LinkDataMapProcessor.NO_OP);
            linkDataDuplexMap = linkSelector.select(Collections.singletonList(fromApplication), range, 1, 0);
        }

        NodeList nodeList = NodeListFactory.createNodeList(linkDataDuplexMap);
        LinkList linkList = LinkListFactory.createLinkList(nodeList, linkDataDuplexMap, range);
        LinkKey linkKey = new LinkKey(fromApplication, toApplication);
        Link link = linkList.getLink(linkKey);
        if (link == null) {
            return createEmptyLinkHistogramSummary(linkDirection, fromApplication, toApplication, range);
        }
        return new LinkHistogramSummary(link);
    }

    private LinkHistogramSummary createEmptyLinkHistogramSummary(LinkDirection direction, Application fromApplication, Application toApplication, Range range) {
        Node fromNode = new Node(fromApplication);
        Node toNode = new Node(toApplication);
        Link emptyLink = new Link(direction, fromNode, toNode, range);
        return new LinkHistogramSummary(emptyLink);
    }
}
