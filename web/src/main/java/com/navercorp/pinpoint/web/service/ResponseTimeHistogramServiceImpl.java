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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Service
public class ResponseTimeHistogramServiceImpl implements ResponseTimeHistogramService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LinkSelectorFactory linkSelectorFactory;

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private MapResponseDao mapResponseDao;

    @Autowired
    private ApplicationFactory applicationFactory;

    private ServerInstanceListFactory serverInstanceListFactory;

    private NodeHistogramFactory nodeHistogramFactory;

    @PostConstruct
    public void init() {
        ServerInstanceListDataSource serverInstanceListDataSource = new AgentInfoServerInstanceListDataSource(agentInfoService);
        serverInstanceListFactory = new DefaultServerInstanceListFactory(serverInstanceListDataSource);

        WasNodeHistogramDataSource wasNodeHistogramDataSource = new MapResponseNodeHistogramDataSource(mapResponseDao);
        nodeHistogramFactory = new DefaultNodeHistogramFactory(wasNodeHistogramDataSource);
    }

    @Override
    public ApplicationTimeHistogramViewModel selectResponseTimeHistogramData(Application application, Range range) {
        List<ResponseTime> responseTimes = mapResponseDao.selectResponseTime(application, range);
        return new ApplicationTimeHistogramViewModel(application, range, new AgentHistogramList(application, responseTimes));
    }

    @Override
    public NodeHistogramSummary selectNodeHistogramData(Application application, Range range, List<Application> fromApplications, List<Application> toApplications) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }
        if (fromApplications == null) {
            throw new NullPointerException("fromApplications must not be null");
        }
        if (toApplications == null) {
            throw new NullPointerException("toApplications must not be null");
        }

        Node node = new Node(application);
        ServiceType applicationServiceType = application.getServiceType();

        List<Application> sourceApplications = fromApplications;
        List<Application> destinationApplications = toApplications;

        if (applicationServiceType.isWas()) {
            NodeHistogram nodeHistogram = nodeHistogramFactory.createWasNodeHistogram(application, range);
            node.setNodeHistogram(nodeHistogram);
            ServerInstanceList serverInstanceList = serverInstanceListFactory.createWasNodeInstanceList(node, range.getTo());
            return new NodeHistogramSummary(serverInstanceList, nodeHistogram);
        } else if (applicationServiceType.isTerminal() || applicationServiceType.isUnknown()) {
            if (sourceApplications.isEmpty()) {
                return createEmptyNodeHistogramSummary(application, range);
            }
            LinkDataMapProcessor destinationApplicationFilter = new DestinationApplicationFilter(application);
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, destinationApplicationFilter, LinkDataMapProcessor.NO_OP);
            LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(sourceApplications, range, 1, 0);

            ServerInstanceList serverInstanceList = serverInstanceListFactory.createEmptyNodeInstanceList();
            if (applicationServiceType.isTerminal()) {
                serverInstanceList = serverInstanceListFactory.createTerminalNodeInstanceList(node, linkDataDuplexMap);
            }

            NodeList nodeList = NodeListFactory.createNodeList(NodeType.DETAILED, linkDataDuplexMap);
            LinkList linkList = LinkListFactory.createLinkList(LinkType.DETAILED, nodeList, linkDataDuplexMap, range);
            NodeHistogram nodeHistogram = nodeHistogramFactory.createTerminalNodeHistogram(application, range, linkList);
            return new NodeHistogramSummary(serverInstanceList, nodeHistogram);
        } else if (applicationServiceType.isQueue()) {
            LinkDataMapProcessor destinationApplicationFilter = new DestinationApplicationFilter(application);
            if (sourceApplications.isEmpty()) {
                if (destinationApplications.isEmpty()) {
                    return createEmptyNodeHistogramSummary(application, range);
                }
                // Retrieve callers
                LinkDataMapProcessor applicationFilter = new ApplicationFilter(application);
                LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, LinkDataMapProcessor.NO_OP, applicationFilter);
                LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(sourceApplications, range, 0, 2);

                LinkDataMap linkDataMap = destinationApplicationFilter.processLinkDataMap(linkDataDuplexMap.getTargetLinkDataMap(), range);
                for (LinkData linkData : linkDataMap.getLinkDataList()) {
                    sourceApplications.add(linkData.getFromApplication());
                }
            }
            // Check using from applications first for caller's link data
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, destinationApplicationFilter, LinkDataMapProcessor.NO_OP);
            LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(sourceApplications, range, 1, 0);

            ServerInstanceList serverInstanceList = serverInstanceListFactory.createQueueNodeInstanceList(node, linkDataDuplexMap);

            NodeList nodeList = NodeListFactory.createNodeList(NodeType.DETAILED, linkDataDuplexMap);
            LinkList linkList = LinkListFactory.createLinkList(LinkType.DETAILED, nodeList, linkDataDuplexMap, range);
            NodeHistogram nodeHistogram = nodeHistogramFactory.createQueueNodeHistogram(application, range, linkList);
            return new NodeHistogramSummary(serverInstanceList, nodeHistogram);
        } else if (applicationServiceType.isUser()) {
            if (destinationApplications.isEmpty()) {
                return createEmptyNodeHistogramSummary(application, range);
            }
            LinkDataMapProcessor sourceApplicationFilter = new SourceApplicationFilter(application);
            LinkSelector linkSelector = linkSelectorFactory.createLinkSelector(LinkSelectorType.UNIDIRECTIONAL, LinkDataMapProcessor.NO_OP, sourceApplicationFilter);
            LinkDataDuplexMap linkDataDuplexMap = linkSelector.select(destinationApplications, range, 0, 1);

            ServerInstanceList serverInstanceList = serverInstanceListFactory.createUserNodeInstanceList();

            NodeList nodeList = NodeListFactory.createNodeList(NodeType.DETAILED, linkDataDuplexMap);
            LinkList linkList = LinkListFactory.createLinkList(LinkType.DETAILED, nodeList, linkDataDuplexMap, range);
            NodeHistogram nodeHistogram = nodeHistogramFactory.createUserNodeHistogram(application, range, linkList);
            return new NodeHistogramSummary(serverInstanceList, nodeHistogram);
        } else {
            return createEmptyNodeHistogramSummary(application, range);
        }
    }

    private NodeHistogramSummary createEmptyNodeHistogramSummary(Application application, Range range) {
        ServerInstanceList serverInstanceList = serverInstanceListFactory.createEmptyNodeInstanceList();
        NodeHistogram emptyNodeHistogram = new NodeHistogram(application, range);
        return new NodeHistogramSummary(serverInstanceList, emptyNodeHistogram);
    }

    @Override
    public LinkHistogramSummary selectLinkHistogramData(Application fromApplication, Application toApplication, Range range) {
        if (fromApplication == null) {
            throw new NullPointerException("fromApplication must not be null");
        }
        if (toApplication == null) {
            throw new NullPointerException("toApplication must not be null");
        }
        if (range == null) {
            throw new NullPointerException("range must not be null");
        }

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
