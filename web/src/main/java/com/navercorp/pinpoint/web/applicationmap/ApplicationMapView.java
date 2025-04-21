package com.navercorp.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Iterators;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.applicationmap.histogram.TimeHistogramFormat;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.map.MapViews;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.applicationmap.view.LinkView;
import com.navercorp.pinpoint.web.applicationmap.view.NodeView;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class ApplicationMapView {
    private final ApplicationMap applicationMap;
    private final MapViews activeView;
    private final HyperLinkFactory hyperLinkFactory;
    private final TimeHistogramFormat timeHistogramFormat;

    public ApplicationMapView(ApplicationMap applicationMap, MapViews activeView, HyperLinkFactory hyperLinkFactory, TimeHistogramFormat timeHistogramFormat) {
        this.applicationMap = Objects.requireNonNull(applicationMap, "applicationMap");
        this.activeView = Objects.requireNonNull(activeView, "activeView");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
        this.timeHistogramFormat = Objects.requireNonNull(timeHistogramFormat, "timeHistogramFormat");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Range getRange() {
        return applicationMap.getRange();
    }

    @JsonProperty("nodeDataArray")
    public Iterator<NodeView> getNodes() {
        Collection<Node> nodes = applicationMap.getNodes();

        return Iterators.transform(nodes.iterator(), node -> new NodeView(node, activeView, hyperLinkFactory, timeHistogramFormat));
    }

    @JsonProperty("linkDataArray")
    public Iterator<LinkView> getLinks() {
        Collection<Link> links = applicationMap.getLinks();

        return Iterators.transform(links.iterator(), link -> new LinkView(link, activeView, timeHistogramFormat));
    }

}
