package com.navercorp.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.link.LinkList;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.nodes.NodeList;
import com.navercorp.pinpoint.web.view.timeseries.TimeSeriesView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApplicationMapTimeData {
    private final NodeList nodeList;
    private final LinkList linkList;

    public ApplicationMapTimeData(NodeList nodeList, LinkList linkList) {
        this.nodeList = nodeList;
        this.linkList = linkList;
    }

    @JsonIgnore
    public Collection<Node> getNodes() {
        return nodeList.getNodeList();
    }

    @JsonIgnore
    public Collection<Link> getLinks() {
        return linkList.getLinkList();
    }

    @JsonProperty("nodeTimeData")
    public List<valueSet> getNodeTimeSeriesView() {
        List<valueSet> result = new ArrayList<>();
        for (Node node : nodeList.getNodeList()) {
            result.add(new valueSet(node.getNodeName(), node.getNodeHistogram().getApplicationTimeSeriesView()));
        }
        return result;
    }

    @JsonProperty("linkTimeData")
    public List<valueSet> getLinkTimeSeriesView() {
        List<valueSet> result = new ArrayList<>();
        for (Link link : linkList.getLinkList()) {
            result.add(new valueSet(link.getLinkName(), link.getLinkApplicationTimeSeriesView()));
        }
        return result;
    }

    private static class valueSet {
        String name;
        TimeSeriesView value;

        public valueSet(String name, TimeSeriesView value) {
            this.name = name;
            this.value = value;
        }

        @JsonProperty("key")
        public String getName() {
            return name;
        }

        @JsonProperty("value")
        public TimeSeriesView getValue() {
            return value;
        }
    }
}
