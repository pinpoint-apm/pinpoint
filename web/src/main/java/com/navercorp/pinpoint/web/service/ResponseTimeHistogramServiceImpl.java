/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.NodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.MapResponseNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.DefaultServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.ServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.StatisticsServerInstanceListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.AgentInfoServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerInstanceListDataSource;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.CreateType;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkHistogramSummary;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.link.LinkListFactory;
import com.navercorp.pinpoint.web.applicationmap.link.LinkType;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeListFactory;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeType;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.AgentHistogramList;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkData;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataDuplexMap;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkDataMap;
import com.navercorp.pinpoint.web.dao.MapResponseDao;
import com.navercorp.pinpoint.web.service.map.processor.ApplicationFilter;
import com.navercorp.pinpoint.web.service.map.processor.DestinationApplicationFilter;
import com.navercorp.pinpoint.web.service.map.processor.LinkDataMapProcessor;
import com.navercorp.pinpoint.web.service.map.LinkSelector;
import com.navercorp.pinpoint.web.service.map.LinkSelectorFactory;
import com.navercorp.pinpoint.web.service.map.LinkSelectorType;
import com.navercorp.pinpoint.web.service.map.processor.SourceApplicationFilter;
import com.navercorp.pinpoint.web.view.ApplicationTimeHistogramViewModel;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeHistogramSummary;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LinkKey;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.ResponseTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkSelectorFactory linkSelectorFactory;

    private final AgentInfoService agentInfoService;

    private final MapResponseDao mapResponseDao;

    private final ApplicationFactory applicationFactory;

    public ResponseTimeHistogramServiceImpl(LinkSelectorFactory linkSelectorFactory, AgentInfoService agentInfoService, MapResponseDao mapResponseDao, ApplicationFactory applicationFactory) {
        this.linkSelectorFactory = Objects.requireNonNull(linkSelectorFactory, "linkSelectorFactory");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.mapResponseDao = Objects.requireNonNull(mapResponseDao, "mapResponseDao");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
    }

    private ServerInstanceListFactory createServerInstanceListFactory(ResponseTimeHistogramServiceOption option) {
        if (option.isUseStatisticsAgentState()) {
            return new StatisticsServerInstanceListFactory();
        }
        ServerInstanceListDataSource serverInstanceListDataSource = new AgentInfoServerInstanceListDataSource(agentInfoService);
        return new DefaultServerInstanceListFactory(serverInstanceListDataSource);
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
        ServiceType applicationServiceType = option.getApplication().getServiceType();

        List<Application> sourceApplications = option.getFromApplications();
        List<Application> destinationApplications = option.getToApplications();

        final NodeHistogramFactory nodeHistogramFactory = createNodeHistogramFactory();
        final ServerInstanceListFactory serverInstanceListFactory = createServerInstanceListFactory(option);

        if (applicationServiceType.isWas()) {
            NodeHistogram nodeHistogram = nodeHistogramFactory.createWasNodeHistogram(option.getApplication(), option.getRange());
            node.setNodeHistogram(nodeHistogram);
            ServerInstanceList serverInstanceList = serverInstanceListFactory.createWasNodeInstanceList(node, option.getRange().getTo());
            return new NodeHistogramSummary(serverInstanceList, nodeHistogram);
        } else if (applicationServiceType.isTerminal() || applicationServiceType.isUnknown() || applicationServiceType.isAlias()) {
            if (sourceApplications.isEmpty()) {
                return createEmptyNodeHistogramSummary(serverInstanceListFactory, option.getApplication(), option.getRange());
            }
            LinkDataMapProcessor destinationApplicationFilter = new DestinationApplicationFilter(option.getApplication());
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, destinationApplicationFilter, LinkDataMapProcessor.NO_OP);
            LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(sourceApplications, option.getRange(), 1, 0);

            ServerInstanceList serverInstanceList = serverInstanceListFactory.createEmptyNodeInstanceList();
            if (applicationServiceType.isTerminal() || applicationServiceType.isAlias()) {
                serverInstanceList = serverInstanceListFactory.createTerminalNodeInstanceList(node, linkDataDuplexMap);
            }

            NodeList nodeList = NodeListFactory.createNodeList(NodeType.DETAILED, linkDataDuplexMap);
            LinkList linkList = LinkListFactory.createLinkList(LinkType.DETAILED, nodeList, linkDataDuplexMap, option.getRange());
            NodeHistogram nodeHistogram = nodeHistogramFactory.createTerminalNodeHistogram(option.getApplication(), option.getRange(), linkList);
            return new NodeHistogramSummary(serverInstanceList, nodeHistogram);
        } else if (applicationServiceType.isQueue()) {
            LinkDataMapProcessor destinationApplicationFilter = new DestinationApplicationFilter(option.getApplication());
            if (sourceApplications.isEmpty()) {
                if (destinationApplications.isEmpty()) {
                    return createEmptyNodeHistogramSummary(serverInstanceListFactory, option.getApplication(), option.getRange());
                }
                // Retrieve callers
                LinkDataMapProcessor applicationFilter = new ApplicationFilter(option.getApplication());
                LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, LinkDataMapProcessor.NO_OP, applicationFilter);
                LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(sourceApplications, option.getRange(), 0, 2);

                LinkDataMap linkDataMap = destinationApplicationFilter.processLinkDataMap(linkDataDuplexMap.getTargetLinkDataMap(), option.getRange());
                for (LinkData linkData : linkDataMap.getLinkDataList()) {
                    sourceApplications.add(linkData.getFromApplication());
                }
            }
            // Check using from applications first for caller's link data
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, destinationApplicationFilter, LinkDataMapProcessor.NO_OP);
            LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(sourceApplications, option.getRange(), 1, 0);

            ServerInstanceList serverInstanceList = serverInstanceListFactory.createQueueNodeInstanceList(node, linkDataDuplexMap);

            NodeList nodeList = NodeListFactory.createNodeList(NodeType.DETAILED, linkDataDuplexMap);
            LinkList linkList = LinkListFactory.createLinkList(LinkType.DETAILED, nodeList, linkDataDuplexMap, option.getRange());
            NodeHistogram nodeHistogram = nodeHistogramFactory.createQueueNodeHistogram(option.getApplication(), option.getRange(), linkList);
            return new NodeHistogramSummary(serverInstanceList, nodeHistogram);
        } else if (applicationServiceType.isUser()) {
            if (destinationApplications.isEmpty()) {
                return createEmptyNodeHistogramSummary(serverInstanceListFactory, option.getApplication(), option.getRange());
            }
            LinkDataMapProcessor sourceApplicationFilter = new SourceApplicationFilter(option.getApplication());
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, LinkDataMapProcessor.NO_OP, sourceApplicationFilter);
            LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(destinationApplications, option.getRange(), 0, 1);

            ServerInstanceList serverInstanceList = serverInstanceListFactory.createUserNodeInstanceList();

            NodeList nodeList = NodeListFactory.createNodeList(NodeType.DETAILED, linkDataDuplexMap);
            LinkList linkList = LinkListFactory.createLinkList(LinkType.DETAILED, nodeList, linkDataDuplexMap, option.getRange());
            NodeHistogram nodeHistogram = nodeHistogramFactory.createUserNodeHistogram(option.getApplication(), option.getRange(), linkList);
            return new NodeHistogramSummary(serverInstanceList, nodeHistogram);
        } else {
            return createEmptyNodeHistogramSummary(serverInstanceListFactory, option.getApplication(), option.getRange());
        }
    }

    private NodeHistogramSummary createEmptyNodeHistogramSummary(ServerInstanceListFactory serverInstanceListFactory, Application application, Range range) {
        ServerInstanceList serverInstanceList = serverInstanceListFactory.createEmptyNodeInstanceList();
        NodeHistogram emptyNodeHistogram = new NodeHistogram(application, range);
        return new NodeHistogramSummary(serverInstanceList, emptyNodeHistogram);
    }

    @Override
    public LinkHistogramSummary selectLinkHistogramData(Application fromApplication, Application toApplication, Range range) {
        Objects.requireNonNull(fromApplication, "fromApplication");
        Objects.requireNonNull(toApplication, "toApplication");
        Objects.requireNonNull(range, "range");


        LinkDataDuplexMap linkDataDuplexMap;
        ServiceType fromApplicationServiceType = fromApplication.getServiceType();
        CreateType createType = CreateType.Target;
        // For user or queue originating links, we must scan using to applications
        if (fromApplicationServiceType.isUser() || fromApplicationServiceType.isQueue()) {
            createType = CreateType.Source;
            LinkDataMapProcessor sourceApplicationFilter = new SourceApplicationFilter(fromApplication);
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, LinkDataMapProcessor.NO_OP, sourceApplicationFilter);
            linkDataDuplexMap = linkSelector.select(Collections.singletonList(toApplication), range, 0, 1);
        } else {
            LinkDataMapProcessor destinationApplication = new DestinationApplicationFilter(toApplication);
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, destinationApplication, LinkDataMapProcessor.NO_OP);
            linkDataDuplexMap = linkSelector.select(Collections.singletonList(fromApplication), range, 1, 0);
        }

        NodeList nodeList = NodeListFactory.createNodeList(NodeType.DETAILED, linkDataDuplexMap);
        LinkList linkList = LinkListFactory.createLinkList(LinkType.DETAILED, nodeList, linkDataDuplexMap, range);
        LinkKey linkKey = new LinkKey(fromApplication, toApplication);
        Link link = linkList.getLink(linkKey);
        if (link == null) {
            return createEmptyLinkHistogramSummary(createType, fromApplication, toApplication, range);
        }
        return new LinkHistogramSummary(link);
    }

    private LinkHistogramSummary createEmptyLinkHistogramSummary(CreateType createType, Application fromApplication, Application toApplication, Range range) {
        Node fromNode = new Node(fromApplication);
        Node toNode = new Node(toApplication);
        Link emptyLink = new Link(createType, fromNode, toNode, range);
        return new LinkHistogramSummary(emptyLink);
    }
}
