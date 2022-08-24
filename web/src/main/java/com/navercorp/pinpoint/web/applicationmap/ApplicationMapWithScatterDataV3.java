package com.navercorp.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.applicationmap.histogram.Histogram;
import com.navercorp.pinpoint.web.applicationmap.link.Link;
import com.navercorp.pinpoint.web.applicationmap.nodes.Node;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApplicationMapWithScatterDataV3 implements ApplicationMap {

    private final ApplicationMap applicationMap;
    private final Map<Application, ScatterData> applicationScatterDataMap;
    private final Map<Application, Histogram> applicationHistogramDataMap;

    public ApplicationMapWithScatterDataV3(ApplicationMap applicationMap, Map<Application, ScatterData> applicationScatterDataMap, Map<Application, Histogram> applicationMapHistogramData) {
        this.applicationMap = Objects.requireNonNull(applicationMap, "applicationMap");
        this.applicationScatterDataMap = Objects.requireNonNull(applicationScatterDataMap, "applicationScatterDataMap");
        this.applicationHistogramDataMap = Objects.requireNonNull(applicationMapHistogramData, "applicationHistogramDataMap");
    }

    @Override
    public Collection<Node> getNodes() {
        return applicationMap.getNodes();
    }

    @Override
    public Collection<Link> getLinks() {
        return applicationMap.getLinks();
    }

    @JsonValue
    public ApplicationMap getApplicationMap() {
        return applicationMap;
    }

    @JsonIgnore
    public Map<Application, ScatterData> getApplicationScatterDataMap() {
        return applicationScatterDataMap;
    }

    @JsonIgnore
    public Map<Application, Histogram> getApplicationHistogramDataMap() {
        return applicationHistogramDataMap;
    }

    @JsonIgnore
    public List<valueSet> getApplicationApdexScoreDataView() {
        final List<valueSet> result = new ArrayList<>();

        for (Map.Entry<Application, Histogram> e : applicationHistogramDataMap.entrySet()) {
            String nodeName = Node.createNodeName(e.getKey());
            result.add(new valueSet(nodeName, ApdexScore.newApdexScore(e.getValue())));
        }
        return result;
    }

    private static class valueSet {
        String name;
        ApdexScore value;

        public valueSet(String name, ApdexScore value) {
            this.name = name;
            this.value = value;
        }

        @JsonProperty("key")
        public String getName() {
            return name;
        }

        @JsonProperty("value")
        public ApdexScore getValue() {
            return value;
        }
    }
}
