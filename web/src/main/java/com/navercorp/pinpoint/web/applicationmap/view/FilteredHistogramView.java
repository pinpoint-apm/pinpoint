package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.NodeHistogram;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.view.histogram.HistogramView;
import com.navercorp.pinpoint.web.view.histogram.ServerHistogramView;

import java.util.ArrayList;
import java.util.List;

public class FilteredHistogramView {

    private final ApplicationMap applicationMap;

    public FilteredHistogramView(ApplicationMap applicationMap) {
        this.applicationMap = applicationMap;
    }

    public List<ServerHistogramView> getNodeServerHistogramData() {
        final List<ServerHistogramView> result = new ArrayList<>();
        for (Node node : applicationMap.getNodes()) {
            result.add(new ServerHistogramView(node.getNodeName(), node.getNodeHistogram(), node.getServerGroupList()));
        }
        return result;
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
