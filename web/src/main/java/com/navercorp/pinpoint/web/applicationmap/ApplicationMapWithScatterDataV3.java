package com.navercorp.pinpoint.web.applicationmap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.applicationmap.histogram.ApdexScore;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.vo.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApplicationMapWithScatterDataV3 extends ApplicationMapWithScatterData {

    private final Map<String, ApdexScore> applicationApdexScoreMap;

    public ApplicationMapWithScatterDataV3(ApplicationMap applicationMap, Map<Application, ScatterData> applicationScatterDataMap, Map<String, ApdexScore> applicationApdexScoreMap) {
        super(applicationMap, applicationScatterDataMap);
        this.applicationApdexScoreMap = Objects.requireNonNull(applicationApdexScoreMap, "applicationApdexScoreMap");
    }

    @JsonIgnore
    public List<valueSet> getApplicationApdexScoreList() {
        List<valueSet> result = new ArrayList<>();
        for (Map.Entry<String, ApdexScore> e : applicationApdexScoreMap.entrySet()) {
            result.add(new valueSet(e.getKey(), e.getValue()));
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
