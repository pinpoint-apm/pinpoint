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

package com.navercorp.pinpoint.web.applicationmap.appender.histogram;

import com.navercorp.pinpoint.common.trace.HistogramSlot;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.trace.ServiceTypeFactory;
import com.navercorp.pinpoint.common.trace.ServiceTypeProperty;
import com.navercorp.pinpoint.web.applicationmap.link.CreateType;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.rawdata.LinkCallDataMap;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author HyunGil Jeong
 */
public abstract class NodeHistogramAppenderTestBase {

    private WasNodeHistogramDataSource wasNodeHistogramDataSource;

    private NodeHistogramAppender nodeHistogramAppender;

    protected abstract NodeHistogramAppenderFactory createNodeHistogramAppenderFactory();

    @Before
    public void setUp() {
        wasNodeHistogramDataSource = mock(WasNodeHistogramDataSource.class);
        NodeHistogramFactory nodeHistogramFactory = new DefaultNodeHistogramFactory(wasNodeHistogramDataSource);
        NodeHistogramAppenderFactory nodeHistogramAppenderFactory = createNodeHistogramAppenderFactory();
        nodeHistogramAppender = nodeHistogramAppenderFactory.create(nodeHistogramFactory);
    }

    @Test
    public void emptyNodeList() {
        // Given
        Range range = new Range(0, 60 * 1000);
        NodeList nodeList = new NodeList();
        LinkList linkList = new LinkList();
        // When
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, linkList);
        // Then
        Assert.assertTrue(nodeList.getNodeList().isEmpty());
        verifyZeroInteractions(wasNodeHistogramDataSource);
    }

    /**
     * Checks histograms for was node.
     */
    @Test
    public void wasNode() {
        // Given
        Range range = new Range(0, 60 * 1000);
        NodeList nodeList = new NodeList();
        LinkList linkList = new LinkList();
        Node node = createNode("testApp", ServiceTypeFactory.of(1000, "WAS"));
        nodeList.addNode(node);

        NodeHistogram nodeHistogram = new NodeHistogram(node.getApplication(), range);
        when(wasNodeHistogramDataSource.createNodeHistogram(node.getApplication(), range)).thenReturn(nodeHistogram);
        // When
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, linkList);
        // Then
        Node actualNode = nodeList.getNodeList().iterator().next();
        Assert.assertSame(nodeHistogram, actualNode.getNodeHistogram());
    }

    /**
     * Checks histograms for a single terminal node.
     * <pre>
     *     fromNode ---> databaseNode
     * </pre>
     */
    @Test
    public void terminalNode() {
        // Given
        Range range = new Range(0, 60 * 1000);
        NodeList nodeList = new NodeList();
        LinkList linkList = new LinkList();

        // fromNode : [testApp] test-app
        Node fromNode = createNode("testApp", ServiceTypeFactory.of(1000, "WAS"));
        String fromNodeAgent = "test-app";
        // toNode : [testDatabase] test-database
        Node toNode = createNode("testDatabase", ServiceTypeFactory.of(2000, "RDB", ServiceTypeProperty.TERMINAL));
        String toNodeAgent = "test-database";
        nodeList.addNode(toNode);

        Link link = new Link(CreateType.Source, fromNode, toNode, range);
        HistogramSlot fastSlot = toNode.getServiceType().getHistogramSchema().getFastSlot();
        HistogramSlot normalSlot = toNode.getServiceType().getHistogramSchema().getNormalSlot();
        HistogramSlot slowSlot = toNode.getServiceType().getHistogramSchema().getSlowSlot();
        // [testApp] test-app -> [testDatabase] test-database
        long fastCallCount = 200L;
        long normalCallCount = 100L;
        long slowCallCount = 75L;
        link.addSource(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), toNodeAgent, toNode.getServiceType(), fastSlot, fastCallCount));
        link.addSource(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), toNodeAgent, toNode.getServiceType(), normalSlot, normalCallCount));
        link.addSource(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), toNodeAgent, toNode.getServiceType(), slowSlot, slowCallCount));
        linkList.addLink(link);

        // When
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, linkList);

        // Then
        Node actualNode = nodeList.getNodeList().iterator().next();
        NodeHistogram nodeHistogram = actualNode.getNodeHistogram();
        // verify application-level histogram
        Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
        Assert.assertEquals(fastCallCount, applicationHistogram.getFastCount());
        Assert.assertEquals(normalCallCount, applicationHistogram.getNormalCount());
        Assert.assertEquals(slowCallCount, applicationHistogram.getSlowCount());
        Assert.assertEquals(fastCallCount + normalCallCount + slowCallCount, applicationHistogram.getTotalCount());
        // verify agent-level histogram
        Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
        Histogram agentHistogram = agentHistogramMap.get(toNodeAgent);
        Assert.assertEquals(fastCallCount, agentHistogram.getFastCount());
        Assert.assertEquals(normalCallCount, agentHistogram.getNormalCount());
        Assert.assertEquals(slowCallCount, agentHistogram.getSlowCount());
        Assert.assertEquals(fastCallCount + normalCallCount + slowCallCount, agentHistogram.getTotalCount());
    }

    /**
     * Checks histograms for a single terminal node with multiple agents(destinations).
     * <pre>
     *     fromNode ---> databaseNode (2 agents)
     * </pre>
     */
    @Test
    public void terminalNode_multiple() {
        // Given
        Range range = new Range(0, 60 * 1000);
        NodeList nodeList = new NodeList();
        LinkList linkList = new LinkList();

        // fromNode : [testApp] test-app
        Node fromNode = createNode("testApp", ServiceTypeFactory.of(1000, "WAS"));
        String fromNodeAgent = "test-app";
        // toNode : [testDatabase] test-database1, test-database2
        Node toNode = createNode("testDatabase", ServiceTypeFactory.of(2000, "RDB", ServiceTypeProperty.TERMINAL));
        String toNodeAgent1 = "test-database1";
        String toNodeAgent2 = "test-database2";
        nodeList.addNode(toNode);

        Link link = new Link(CreateType.Source, fromNode, toNode, range);
        HistogramSlot fastSlot = toNode.getServiceType().getHistogramSchema().getFastSlot();
        HistogramSlot normalSlot = toNode.getServiceType().getHistogramSchema().getNormalSlot();
        // [testApp] test-app -> [testDatabase] test-database1
        long callCount1 = 100L;
        link.addSource(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), toNodeAgent1, toNode.getServiceType(), fastSlot, callCount1));
        // [testApp] test-app -> [testDatabase] test-database2
        long callCount2 = 50L;
        link.addSource(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), toNodeAgent2, toNode.getServiceType(), normalSlot, callCount2));
        linkList.addLink(link);

        // When
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, linkList);

        // Then
        Node actualNode = nodeList.getNodeList().iterator().next();
        NodeHistogram nodeHistogram = actualNode.getNodeHistogram();
        // verify application-level histogram
        Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
        Assert.assertEquals(callCount1, applicationHistogram.getFastCount());
        Assert.assertEquals(callCount2, applicationHistogram.getNormalCount());
        Assert.assertEquals(callCount1 + callCount2, applicationHistogram.getTotalCount());
        // verify agent-level histogram
        Map<String, Histogram> agentHistogramMap = nodeHistogram.getAgentHistogramMap();
        Histogram agent1Histogram = agentHistogramMap.get(toNodeAgent1);
        Assert.assertEquals(callCount1, agent1Histogram.getFastCount());
        Assert.assertEquals(callCount1, agent1Histogram.getTotalCount());
        Histogram agent2Histogram = agentHistogramMap.get(toNodeAgent2);
        Assert.assertEquals(callCount2, agent2Histogram.getNormalCount());
        Assert.assertEquals(callCount2, agent2Histogram.getTotalCount());
    }

    /**
     * Checks histograms for multiple terminal nodes called from a single node.
     * <pre>
     *     fromNode ---> databaseNode
     *               |-> cacheNode
     * </pre>
     */
    @Test
    public void terminalNodes() {
        // Given
        Range range = new Range(0, 60 * 1000);
        NodeList nodeList = new NodeList();
        LinkList linkList = new LinkList();

        // fromNode : [testApp] test-app
        Node fromNode = createNode("testApp", ServiceTypeFactory.of(1000, "WAS"));
        String fromNodeAgent = "test-app";
        // databaseNode : [testDatabase] test-database
        Node databaseNode = createNode("testDatabase", ServiceTypeFactory.of(2000, "RDB", ServiceTypeProperty.TERMINAL));
        String databaseNodeAgent = "test-database";
        nodeList.addNode(databaseNode);
        // cacheNode : [testCache] test-cache
        Node cacheNode = createNode("testCache", ServiceTypeFactory.of(8000, "Cache", ServiceTypeProperty.TERMINAL));
        String cacheNodeAgent = "test-cache";
        nodeList.addNode(cacheNode);

        Link databaseLink = new Link(CreateType.Source, fromNode, databaseNode, range);
        HistogramSlot databaseSlowSlot = databaseNode.getServiceType().getHistogramSchema().getSlowSlot();
        long databaseCallSlowCount = 50L;
        databaseLink.addSource(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), databaseNodeAgent, databaseNode.getServiceType(), databaseSlowSlot, databaseCallSlowCount));
        linkList.addLink(databaseLink);

        Link cacheLink = new Link(CreateType.Source, fromNode, cacheNode, range);
        HistogramSlot cacheFastSlot = cacheNode.getServiceType().getHistogramSchema().getFastSlot();
        HistogramSlot cacheSlowSlot = cacheNode.getServiceType().getHistogramSchema().getSlowSlot();
        long cacheCallFastCount = 199L;
        long cacheCallSlowCount = 99L;
        cacheLink.addSource(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), cacheNodeAgent, cacheNode.getServiceType(), cacheFastSlot, cacheCallFastCount));
        cacheLink.addSource(createLinkCallDataMap(fromNodeAgent, fromNode.getServiceType(), cacheNodeAgent, cacheNode.getServiceType(), cacheSlowSlot, cacheCallSlowCount));
        linkList.addLink(cacheLink);

        // When
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, linkList);

        // Then
        // Database node
        Node actualDatabaseNode = nodeList.findNode(databaseNode.getApplication());
        NodeHistogram databaseNodeHistogram = actualDatabaseNode.getNodeHistogram();
        // verify application-level histogram
        Histogram databaseApplicationHistogram = databaseNodeHistogram.getApplicationHistogram();
        Assert.assertEquals(databaseCallSlowCount, databaseApplicationHistogram.getSlowCount());
        Assert.assertEquals(databaseCallSlowCount, databaseApplicationHistogram.getTotalCount());
        // verify agent-level histogram
        Map<String, Histogram> databaseAgentHistogramMap = databaseNodeHistogram.getAgentHistogramMap();
        Histogram databaseAgentHistogram = databaseAgentHistogramMap.get(databaseNodeAgent);
        Assert.assertEquals(databaseCallSlowCount, databaseAgentHistogram.getSlowCount());
        Assert.assertEquals(databaseCallSlowCount, databaseAgentHistogram.getTotalCount());
        // Cache node
        Node actualCacheNode = nodeList.findNode(cacheNode.getApplication());
        NodeHistogram cacheNodeHistogram = actualCacheNode.getNodeHistogram();
        // verify application-level histogram
        Histogram cacheApplicationHistogram = cacheNodeHistogram.getApplicationHistogram();
        Assert.assertEquals(cacheCallFastCount, cacheApplicationHistogram.getFastCount());
        Assert.assertEquals(cacheCallSlowCount, cacheApplicationHistogram.getSlowCount());
        Assert.assertEquals(cacheCallFastCount + cacheCallSlowCount, cacheApplicationHistogram.getTotalCount());
        // verify agent-level histogram
        Map<String, Histogram> cacheAgentHistogramMap = cacheNodeHistogram.getAgentHistogramMap();
        Histogram cacheAgentHistogram = cacheAgentHistogramMap.get(cacheNodeAgent);
        Assert.assertEquals(cacheCallFastCount, cacheAgentHistogram.getFastCount());
        Assert.assertEquals(cacheCallSlowCount, cacheAgentHistogram.getSlowCount());
        Assert.assertEquals(cacheCallFastCount + cacheCallSlowCount, cacheAgentHistogram.getTotalCount());
    }

    /**
     * Checks histograms for user node.
     * <pre>
     *     userNode ---> wasNode (2 agents)
     * </pre>
     */
    @Test
    public void userNode() {
        // Given
        Range range = new Range(0, 60 * 1000);
        NodeList nodeList = new NodeList();
        LinkList linkList = new LinkList();
        // userNode : [userNode] user
        Node userNode = createNode("userNode", ServiceType.USER);
        String userNodeAgent = "user";
        nodeList.addNode(userNode);
        // wasNode : [wasNode] was-1, was-2
        Node wasNode = createNode("wasNode", ServiceTypeFactory.of(1000, "WAS"));
        String wasNodeAgent1 = "was-1";
        String wasNodeAgent2 = "was-2";
        nodeList.addNode(wasNode);

        Link link = new Link(CreateType.Target, userNode, wasNode, range);
        HistogramSlot fastSlot = wasNode.getServiceType().getHistogramSchema().getFastSlot();
        HistogramSlot normalSlot = wasNode.getServiceType().getHistogramSchema().getNormalSlot();
        // [userNode] user -> [wasNode] was-1
        long fastCallCount = 100L;
        link.addTarget(createLinkCallDataMap(userNodeAgent, userNode.getServiceType(), wasNodeAgent1, wasNode.getServiceType(), fastSlot, fastCallCount));
        // [userNode] user -> [wasNode] was-2
        long normalCallCount = 50L;
        link.addTarget(createLinkCallDataMap(userNodeAgent, userNode.getServiceType(), wasNodeAgent2, wasNode.getServiceType(), normalSlot, normalCallCount));
        linkList.addLink(link);

        // When
        nodeHistogramAppender.appendNodeHistogram(range, nodeList, linkList);

        NodeHistogram nodeHistogram = userNode.getNodeHistogram();
        // verify application-level histogram
        Histogram applicationHistogram = nodeHistogram.getApplicationHistogram();
        Assert.assertEquals(fastCallCount, applicationHistogram.getFastCount());
        Assert.assertEquals(normalCallCount, applicationHistogram.getNormalCount());
        Assert.assertEquals(fastCallCount + normalCallCount, applicationHistogram.getTotalCount());
        // verify agent-level histogram - there is none for user node
        Map<String, Histogram> databaseAgentHistogramMap = nodeHistogram.getAgentHistogramMap();
        Assert.assertTrue(databaseAgentHistogramMap.isEmpty());
    }

    private Node createNode(String applicationName, ServiceType serviceType) {
        Application application = new Application(applicationName, serviceType);
        return new Node(application);
    }

    private LinkCallDataMap createLinkCallDataMap(String fromAgentId, ServiceType fromAgentServiceType, String toAgentId, ServiceType toAgentServiceType, HistogramSlot slot, long callCount) {
        long currentTimestamp = System.currentTimeMillis();
        LinkCallDataMap linkCallDataMap = new LinkCallDataMap();
        linkCallDataMap.addCallData(fromAgentId, fromAgentServiceType, toAgentId, toAgentServiceType, currentTimestamp, slot.getSlotTime(), callCount);
        return linkCallDataMap;
    }
}
