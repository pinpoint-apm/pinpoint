package com.navercorp.pinpoint.web.applicationmap.view;

import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
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
            result.add(new HistogramView(node.getNodeName(), node.getNodeHistogram()));
        }
        return result;
    }

    public List<HistogramView> getLinkHistogramData() {
        final List<HistogramView> result = new ArrayList<>();
        for (Link link : applicationMap.getLinks()) {
            result.add(new HistogramView(link.getLinkName(), link.getHistogram(), link.getLinkApplicationTimeHistogram()));
        }
        return result;
    }
}
