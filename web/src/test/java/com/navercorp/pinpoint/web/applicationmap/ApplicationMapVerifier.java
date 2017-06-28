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

package com.navercorp.pinpoint.web.applicationmap;

import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstanceList;
import com.navercorp.pinpoint.web.vo.LinkKey;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class ApplicationMapVerifier {

    private final ApplicationMap applicationMap;

    public ApplicationMapVerifier(ApplicationMap applicationMap) {
        this.applicationMap = applicationMap;
    }

    public void verify(ApplicationMap otherApplicationMap) {
        verifyNodes(otherApplicationMap.getNodes());
        verifyLinks(otherApplicationMap.getLinks());
    }

    private void verifyNodes(Collection<Node> otherNodes) {
        Collection<Node> thisNodes = applicationMap.getNodes();
        verifySize(thisNodes, otherNodes);
        for (Node otherNode : otherNodes) {
            String nodeNameToFind = otherNode.getNodeName();
            Node thisNode = findNode(thisNodes, nodeNameToFind);
            if (thisNode == null) {
                Assert.fail(otherNode + " not in " + thisNodes);
            }
            verifyNode(thisNode, otherNode);
        }
    }

    private Node findNode(Collection<Node> nodes, String nodeNameToFind) {
        for (Node node : nodes) {
            String nodeName = node.getNodeName();
            if (nodeName.equals(nodeNameToFind)) {
                return node;
            }
        }
        return null;
    }

    private void verifyNode(Node node1, Node node2) {
        Assert.assertEquals(node1.getApplication(), node2.getApplication());
        Assert.assertEquals(node1.getApplicationTextName(), node2.getApplicationTextName());
        Assert.assertEquals(node1.getServiceType(), node2.getServiceType());

        verifyServerInstanceList(node1.getServerInstanceList(), node2.getServerInstanceList());
        verifyNodeHistogram(node1.getNodeHistogram(), node2.getNodeHistogram());
    }

    private void verifyServerInstanceList(ServerInstanceList serverInstanceList1, ServerInstanceList serverInstanceList2) {
        verifyNullable(serverInstanceList1, serverInstanceList2);
        if (serverInstanceList1 == null && serverInstanceList2 == null) {
            return;
        }

        Assert.assertEquals(serverInstanceList1.getInstanceCount(), serverInstanceList2.getInstanceCount());

        Assert.assertTrue(serverInstanceList1.getAgentIdList().containsAll(serverInstanceList2.getAgentIdList()));
        Assert.assertEquals(serverInstanceList1.getAgentIdList().size(), serverInstanceList2.getAgentIdList().size());

        Map<String, List<ServerInstance>> serverInstancesMap1 = serverInstanceList1.getServerInstanceList();
        Map<String, List<ServerInstance>> serverInstancesMap2 = serverInstanceList2.getServerInstanceList();
        Assert.assertEquals(serverInstancesMap1.size(), serverInstancesMap2.size());
        for (Map.Entry<String, List<ServerInstance>> e : serverInstancesMap1.entrySet()) {
            String hostName = e.getKey();
            List<ServerInstance> serverInstances1 = e.getValue();
            List<ServerInstance> serverInstances2 = serverInstancesMap2.get(hostName);
            Assert.assertNotNull(serverInstances2);
            Assert.assertTrue(serverInstances1.containsAll(serverInstances2));
            Assert.assertEquals(serverInstances1.size(), serverInstances2.size());
        }
    }

    private void verifyNodeHistogram(NodeHistogram nodeHistogram1, NodeHistogram nodeHistogram2) {
        verifyNullable(nodeHistogram1, nodeHistogram2);
        if (nodeHistogram1 == null && nodeHistogram2 == null) {
            return;
        }

        verifyHistogram(nodeHistogram1.getApplicationHistogram(), nodeHistogram2.getApplicationHistogram());

        Map<String, Histogram> agentHistogramMap1 = nodeHistogram1.getAgentHistogramMap();
        Map<String, Histogram> agentHistogramMap2 = nodeHistogram2.getAgentHistogramMap();
        Assert.assertEquals(agentHistogramMap1.size(), agentHistogramMap2.size());
        for (Map.Entry<String, Histogram> e : agentHistogramMap1.entrySet()) {
            String agentId = e.getKey();
            Histogram agentHistogram1 = e.getValue();
            Histogram agentHistogram2 = agentHistogramMap2.get(agentId);
            Assert.assertNotNull(agentHistogram2);
            verifyHistogram(agentHistogram1, agentHistogram2);
        }
    }

    private void verifyHistogram(Histogram histogram1, Histogram histogram2) {
        verifyNullable(histogram1, histogram2);
        if (histogram1 == null && histogram2 == null) {
            return;
        }
        Assert.assertEquals(histogram1.getHistogramSchema(), histogram2.getHistogramSchema());
        Assert.assertEquals(histogram1.getTotalCount(), histogram2.getTotalCount());
        Assert.assertEquals(histogram1.getSuccessCount(), histogram2.getSuccessCount());
        Assert.assertEquals(histogram1.getErrorCount(), histogram2.getErrorCount());
        Assert.assertEquals(histogram1.getFastCount(), histogram2.getFastCount());
        Assert.assertEquals(histogram1.getFastErrorCount(), histogram2.getFastErrorCount());
        Assert.assertEquals(histogram1.getNormalCount(), histogram2.getNormalCount());
        Assert.assertEquals(histogram1.getNormalErrorCount(), histogram2.getNormalErrorCount());
        Assert.assertEquals(histogram1.getSlowCount(), histogram2.getSlowCount());
        Assert.assertEquals(histogram1.getSlowErrorCount(), histogram2.getSlowErrorCount());
        Assert.assertEquals(histogram1.getVerySlowCount(), histogram2.getVerySlowCount());
        Assert.assertEquals(histogram1.getVerySlowErrorCount(), histogram2.getVerySlowErrorCount());
    }

    private void verifyLinks(Collection<Link> otherLinks) {
        Collection<Link> thisLinks = applicationMap.getLinks();
        verifySize(thisLinks, otherLinks);
        for (Link otherLink : otherLinks) {
            LinkKey linkKeyToFind = otherLink.getLinkKey();
            Link thisLink = findLink(thisLinks, linkKeyToFind);
            if (thisLink == null) {
                Assert.fail(otherLink + " not in " + thisLinks);
            }
            verifyLink(thisLink, otherLink);
        }
    }

    private Link findLink(Collection<Link> links, LinkKey linkKeyToFind) {
        for (Link link : links) {
            LinkKey linkKey = link.getLinkKey();
            if (linkKey.equals(linkKeyToFind)) {
                return link;
            }
        }
        return null;
    }

    private void verifyLink(Link thisLink, Link otherLink) {
        verifyNode(thisLink.getFrom(), otherLink.getFrom());
        verifyNode(thisLink.getTo(), otherLink.getTo());

        Histogram thisLinkHistogram = thisLink.getHistogram();
        Histogram otherLinkHistogram = otherLink.getHistogram();
        verifyHistogram(thisLinkHistogram, otherLinkHistogram);
    }

    private <T> void verifySize(Collection<T> collection1, Collection<T> collection2) {
        Assert.assertEquals(new ArrayList<>(collection1).size(), new ArrayList<>(collection2).size());
    }

    private <T> void verifyNullable(T nullable1, T nullable2) {
        if (nullable1 == null && nullable2 != null) {
            Assert.fail();
        }
        if (nullable1 != null && nullable2 == null) {
            Assert.fail();
        }
    }
}
