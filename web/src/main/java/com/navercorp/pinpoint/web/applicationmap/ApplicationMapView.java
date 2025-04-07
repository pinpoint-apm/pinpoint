package com.navercorp.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterators;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.view.LinkView;
import com.navercorp.pinpoint.web.applicationmap.view.NodeView;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ApplicationMapView {
    private final ApplicationMap applicationMap;
    private final TimeWindow timeWindow;
    private final Class<?> activeView;
    private final TimeHistogramFormat timeHistogramFormat;

    public ApplicationMapView(ApplicationMap applicationMap, Class<?> activeView, TimeHistogramFormat timeHistogramFormat) {
        this.applicationMap = Objects.requireNonNull(applicationMap, "applicationMap");
        this.timeWindow = null;
        this.activeView = Objects.requireNonNull(activeView, "activeView");
        this.timeHistogramFormat = Objects.requireNonNull(timeHistogramFormat, "timeHistogramFormat");
    }

    public ApplicationMapView(ApplicationMap applicationMap, TimeWindow timeWindow, Class<?> activeView, TimeHistogramFormat timeHistogramFormat) {
        this.applicationMap = Objects.requireNonNull(applicationMap, "applicationMap");
        this.timeWindow = Objects.requireNonNull(timeWindow, "timeWindow");
        this.activeView = Objects.requireNonNull(activeView, "activeView");
        this.timeHistogramFormat = Objects.requireNonNull(timeHistogramFormat, "timeHistogramFormat");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Range getRange() {
        return applicationMap.getRange();
    }

    @JsonProperty("nodeDataArray")
    public Iterator<NodeView> getNodes() {
        Collection<Node> nodes = applicationMap.getNodes();

        return Iterators.transform(nodes.iterator(), node -> new NodeView(node, activeView, timeHistogramFormat));
    }

    @JsonProperty("ts")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<Long> getTimestamp() {
        if (timeWindow == null) {
            return null;
        }
        return timeWindow.getTimeseriesWindows();
    }

    @JsonProperty("linkDataArray")
    public Iterator<LinkView> getLinks() {
        Collection<Link> links = applicationMap.getLinks();

        return Iterators.transform(links.iterator(), link -> new LinkView(link, activeView, timeHistogramFormat));
    }

}
