package com.navercorp.pinpoint.web.applicationmap.view;

import com.google.common.collect.Iterators;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.view.histogram.HistogramView;
import com.navercorp.pinpoint.web.view.histogram.ServerHistogramView;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FilteredHistogramView {

    private final ApplicationMap applicationMap;
    private final HyperLinkFactory hyperLinkFactory;

    public FilteredHistogramView(ApplicationMap applicationMap, HyperLinkFactory hyperLinkFactory) {
        this.applicationMap = applicationMap;
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    public Iterator<ServerHistogramView> getNodeServerHistogramData() {
        Collection<Node> nodes = applicationMap.getNodes();
        return Iterators.transform(nodes.iterator(), this::newServerHistogramView);
    }

    private ServerHistogramView newServerHistogramView(Node node) {
        ServerGroupListView serverGroupListView = new ServerGroupListView(node.getServerGroupList(), hyperLinkFactory);
        String name = node.getNodeName().getName();
        List<HistogramView> agentHistogramViewList = node.getNodeHistogram().createAgentHistogramViewList();
        return new ServerHistogramView(name, agentHistogramViewList, serverGroupListView);
    }

    public Iterator<HistogramView> getNodeHistogramData() {
        Collection<Node> nodes = applicationMap.getNodes();
        return Iterators.transform(nodes.iterator(), this::getNodeHistogramView);
    }

    private HistogramView getNodeHistogramView(Node node) {
        String nodeName = node.getNodeName().getName();
        NodeHistogram nodeHistogram = node.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        List<TimeHistogram> histogramList = nodeHistogram.getApplicationTimeHistogram().getHistogramList();
        return new HistogramView(nodeName, histogram, histogramList);
    }

    public Iterator<HistogramView> getLinkHistogramData() {
        Collection<Link> links = applicationMap.getLinks();
        return Iterators.transform(links.iterator(), this::getLinkHistogramView);
    }

    private HistogramView getLinkHistogramView(Link link) {
        String linkName = link.getLinkName().getName();
        Histogram histogram = link.getHistogram();
        List<TimeHistogram> histogramList = link.getLinkApplicationTimeHistogram().getHistogramList();
        return new HistogramView(linkName, histogram, histogramList);
    }

}
