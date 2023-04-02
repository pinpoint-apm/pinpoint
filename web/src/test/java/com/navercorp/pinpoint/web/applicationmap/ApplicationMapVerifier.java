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
import com.navercorp.pinpoint.web.applicationmap.link.LinkKey;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroup;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerGroupList;
import com.navercorp.pinpoint.web.applicationmap.nodes.ServerInstance;
import com.navercorp.pinpoint.web.vo.Application;
import org.junit.jupiter.api.Assertions;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

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
            Application nodeNameToFind = otherNode.getApplication();
            Node thisNode = findNode(thisNodes, nodeNameToFind);
            if (thisNode == null) {
                Assertions.fail(otherNode + " not in " + thisNodes);
            }
            verifyNode(thisNode, otherNode);
        }
    }

    private Node findNode(Collection<Node> nodes, Application nodeNameToFind) {
        for (Node node : nodes) {
            Application nodeName = node.getApplication();
            if (nodeName.equals(nodeNameToFind)) {
                return node;
            }
        }
        return null;
    }

    private void verifyNode(Node node1, Node node2) {
        Assertions.assertEquals(node1.getApplication(), node2.getApplication());
        Assertions.assertEquals(node1.getApplicationTextName(), node2.getApplicationTextName());
        Assertions.assertEquals(node1.getServiceType(), node2.getServiceType());

        verifyServerGroupList(node1.getServerGroupList(), node2.getServerGroupList());
        verifyNodeHistogram(node1.getNodeHistogram(), node2.getNodeHistogram());
    }

    private void verifyServerGroupList(ServerGroupList serverGroupList1, ServerGroupList serverGroupList2) {
        verifyNullable(serverGroupList1, serverGroupList2);
        if (serverGroupList1 == null && serverGroupList2 == null) {
            return;
        }

        Assertions.assertEquals(serverGroupList1.getInstanceCount(), serverGroupList2.getInstanceCount());

        Assertions.assertTrue(serverGroupList1.getAgentIdList().containsAll(serverGroupList2.getAgentIdList()));
        Assertions.assertEquals(serverGroupList1.getAgentIdList().size(), serverGroupList2.getAgentIdList().size());

        List<ServerGroup> serverGroup1 = serverGroupList1.getServerGroupList();
        List<ServerGroup> serverGroup2 = serverGroupList2.getServerGroupList();

        assertThat(serverGroup1).hasSameSizeAs(serverGroup2);
        for (ServerGroup serverGroup : serverGroup1) {
            String hostName = serverGroup.getHostName();
            List<ServerInstance> serverInstances1 = serverGroup.getInstanceList();
            List<ServerInstance> serverInstances2 = serverGroup2.stream()
                    .filter(group -> group.getHostName().equals(hostName))
                    .findFirst().orElseThrow().getInstanceList();
            Assertions.assertNotNull(serverInstances2);
            assertThat(serverInstances1)
                    .containsAll(serverInstances2)
                    .hasSameSizeAs(serverInstances2);
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
        Assertions.assertEquals(agentHistogramMap1.size(), agentHistogramMap2.size());
        for (Map.Entry<String, Histogram> e : agentHistogramMap1.entrySet()) {
            String agentId = e.getKey();
            Histogram agentHistogram1 = e.getValue();
            Histogram agentHistogram2 = agentHistogramMap2.get(agentId);
            Assertions.assertNotNull(agentHistogram2);
            verifyHistogram(agentHistogram1, agentHistogram2);
        }
    }

    private void verifyHistogram(Histogram histogram1, Histogram histogram2) {
        verifyNullable(histogram1, histogram2);
        if (histogram1 == null && histogram2 == null) {
            return;
        }
        Assertions.assertEquals(histogram1.getHistogramSchema(), histogram2.getHistogramSchema());
        Assertions.assertEquals(histogram1.getTotalCount(), histogram2.getTotalCount());
        Assertions.assertEquals(histogram1.getSuccessCount(), histogram2.getSuccessCount());
        Assertions.assertEquals(histogram1.getErrorCount(), histogram2.getErrorCount());
        Assertions.assertEquals(histogram1.getFastCount(), histogram2.getFastCount());
        Assertions.assertEquals(histogram1.getFastErrorCount(), histogram2.getFastErrorCount());
        Assertions.assertEquals(histogram1.getNormalCount(), histogram2.getNormalCount());
        Assertions.assertEquals(histogram1.getNormalErrorCount(), histogram2.getNormalErrorCount());
        Assertions.assertEquals(histogram1.getSlowCount(), histogram2.getSlowCount());
        Assertions.assertEquals(histogram1.getSlowErrorCount(), histogram2.getSlowErrorCount());
        Assertions.assertEquals(histogram1.getVerySlowCount(), histogram2.getVerySlowCount());
        Assertions.assertEquals(histogram1.getVerySlowErrorCount(), histogram2.getVerySlowErrorCount());
    }

    private void verifyLinks(Collection<Link> otherLinks) {
        Collection<Link> thisLinks = applicationMap.getLinks();
        verifySize(thisLinks, otherLinks);
        for (Link otherLink : otherLinks) {
            LinkKey linkKeyToFind = otherLink.getLinkKey();
            Link thisLink = findLink(thisLinks, linkKeyToFind);
            if (thisLink == null) {
                Assertions.fail(otherLink + " not in " + thisLinks);
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
        assertThat(collection1).hasSameSizeAs(collection2);
    }

    private <T> void verifyNullable(T nullable1, T nullable2) {
        if (nullable1 == null && nullable2 != null) {
            Assertions.fail();
        }
        if (nullable1 != null && nullable2 == null) {
            Assertions.fail();
        }
    }
}
