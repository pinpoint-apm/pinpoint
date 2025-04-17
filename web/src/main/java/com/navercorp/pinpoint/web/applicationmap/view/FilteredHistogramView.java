package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.view.histogram.HistogramView;
import com.navercorp.pinpoint.web.view.histogram.ServerHistogramView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilteredHistogramView {

    private final ApplicationMap applicationMap;
    private final HyperLinkFactory hyperLinkFactory;

    public FilteredHistogramView(ApplicationMap applicationMap, HyperLinkFactory hyperLinkFactory) {
        this.applicationMap = applicationMap;
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    public List<ServerHistogramView> getNodeServerHistogramData() {
        final List<ServerHistogramView> result = new ArrayList<>();
        for (Node node : applicationMap.getNodes()) {
            ServerHistogramView view = newServerHistogramView(node);
            result.add(view);
        }
        return result;
    }

    private ServerHistogramView newServerHistogramView(Node node) {
        ServerGroupListView serverGroupListView = new ServerGroupListView(node.getServerGroupList(), hyperLinkFactory);
        return new ServerHistogramView(node.getNodeName(), node.getNodeHistogram(), serverGroupListView);
    }

    public List<HistogramView> getNodeHistogramData() {
        final List<HistogramView> result = new ArrayList<>();
        for (Node node : applicationMap.getNodes()) {
            HistogramView view = getNodeHistogramView(node);
            result.add(view);
        }
        return result;
    }

    private HistogramView getNodeHistogramView(Node node) {
        String nodeName = node.getNodeName().getName();
        NodeHistogram nodeHistogram = node.getNodeHistogram();
        Histogram histogram = nodeHistogram.getApplicationHistogram();
        List<TimeHistogram> histogramList = nodeHistogram.getApplicationTimeHistogram().getHistogramList();
        return new HistogramView(nodeName, histogram, histogramList);
    }

    public List<HistogramView> getLinkHistogramData() {
        final List<HistogramView> result = new ArrayList<>();
        for (Link link : applicationMap.getLinks()) {
            HistogramView view = getLinkHistogramView(link);
            result.add(view);
        }
        return result;
    }

    private HistogramView getLinkHistogramView(Link link) {
        String linkName = link.getLinkName().getName();
        Histogram histogram = link.getHistogram();
        List<TimeHistogram> histogramList = link.getLinkApplicationTimeHistogram().getHistogramList();
        return new HistogramView(linkName, histogram, histogramList);
    }

}
